import pandas as pd
import numpy as np
import mysql.connector
from pymongo import MongoClient
from scipy import stats
from sklearn.preprocessing import MinMaxScaler
import json
from datetime import datetime, timedelta
import logging

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 数据库连接配置
MYSQL_CONFIG = {
    'host': 'localhost',
    'user': 'root',
    'password': 'password',
    'database': 'mall_db'
}

MONGODB_CONFIG = {
    'host': 'localhost',
    'port': 27017,
    'database': 'mall_history'
}

class DatabaseManager:
    def __init__(self):
        self.mysql_conn = None
        self.mongo_client = None
        self.mongo_db = None
        
    def get_mysql_connection(self):
        """获取MySQL连接"""
        if not self.mysql_conn or not self.mysql_conn.is_connected():
            self.mysql_conn = mysql.connector.connect(**MYSQL_CONFIG)
        return self.mysql_conn
    
    def get_mongo_connection(self):
        """获取MongoDB连接"""
        if not self.mongo_client:
            self.mongo_client = MongoClient(MONGODB_CONFIG['host'], MONGODB_CONFIG['port'])
            self.mongo_db = self.mongo_client[MONGODB_CONFIG['database']]
        return self.mongo_db

# 全局数据库管理器
db_manager = DatabaseManager()

# 从数据库获取数据
def get_datas(table_name: str = "products") -> tuple:
    """从MySQL获取数据"""
    try:
        conn = db_manager.get_mysql_connection()
        df = pd.read_sql(f"SELECT * FROM {table_name}", con=conn)
        return df, df.columns.tolist(), df.shape
    except Exception as e:
        logger.error(f"获取数据失败: {e}")
        return pd.DataFrame(), [], (0, 0)

def get_user_behavior_data(user_id: int) -> dict:
    """获取用户行为数据"""
    try:
        conn = db_manager.get_mysql_connection()
        
        # 获取订单数据
        orders_df = pd.read_sql("""
            SELECT o.*, oi.product_id, oi.quantity, oi.price, p.category, p.brand, p.rating
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            JOIN products p ON oi.product_id = p.product_id
            WHERE o.user_id = %s
        """, con=conn, params=[user_id])
        
        # 获取购物车数据
        cart_df = pd.read_sql("""
            SELECT c.*, p.category, p.brand, p.price, p.rating
            FROM cart c
            JOIN products p ON c.product_id = p.product_id
            WHERE c.user_id = %s
        """, con=conn, params=[user_id])
        
        # 获取退货数据
        returns_df = pd.read_sql("""
            SELECT r.*, p.category, p.brand, p.price, p.rating
            FROM returns r
            JOIN products p ON r.product_id = p.product_id
            WHERE r.user_id = %s
        """, con=conn, params=[user_id])
        
        return {
            'orders': orders_df,
            'cart': cart_df,
            'returns': returns_df
        }
    except Exception as e:
        logger.error(f"获取用户行为数据失败: {e}")
        return {'orders': pd.DataFrame(), 'cart': pd.DataFrame(), 'returns': pd.DataFrame()}

def save_to_history_db(data: dict, collection_name: str):
    """保存数据到MongoDB历史数据库"""
    try:
        mongo_db = db_manager.get_mongo_connection()
        if mongo_db is None:
            logger.error("MongoDB连接失败，无法保存历史数据。")
            return
        collection = mongo_db[collection_name]
        data['timestamp'] = datetime.now()
        collection.insert_one(data)
        logger.info(f"数据已保存到历史数据库: {collection_name}")
    except Exception as e:
        logger.error(f"保存历史数据失败: {e}")