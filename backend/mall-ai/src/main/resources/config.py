"""
数据库配置文件
根据您的实际数据库配置修改此文件
"""
import os

# 目前python不需要连接数据库，只负责计算
# MySQL数据库配置
MYSQL_CONFIG = {
    'host': os.getenv('DB_HOST', 'localhost'),
    'user': os.getenv('DB_USER', 'root'),
    'password': os.getenv('DB_PASSWORD', ''),
    'database': os.getenv('DB_NAME', 'mall'),
    'port': int(os.getenv('DB_PORT', '3306'))
}

# MongoDB配置
MONGODB_CONFIG = {
    'host': os.getenv('MONGODB_HOST', 'localhost'),
    'port': 27017,
    'database': 'mall_port'   # 历史数据存储数据库
}

# 日志配置
LOGGING_CONFIG = {
    'default_level': 'INFO',  # 默认日志级别
    'file_level': 'INFO',     # 文件日志级别
    'console_level': 'INFO',  # 控制台日志级别
    'detailed_product_scoring': False,  # 是否记录详细的商品评分过程
    'max_log_file_size': 10 * 1024 * 1024,  # 10MB
    'backup_count': 5,        # 保留的日志文件数量
    'log_format': '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
}

# 推荐系统参数配置
RECOMMENDATION_CONFIG = {
    # 行为权重
    'behavior_weights': {
        'purchase': 4.0,     # 购买权重
        'cart': 2.0,         # 购物车权重
        'return': -2.0,      # 退货权重（负权重）
        'click': 1.0,        # 点击权重
    },
    
    # Softmax温度参数（控制权重分布的锐度）
    'softmax_temperature': 1.0,
    
    # 短期动量衰减系数
    'momentum_decay': 0.8,
    
    # 短期动量强度
    'momentum_strength': 0.3,
    
    # 推荐数量
    'default_recommendation_count': 10,
    
    # 默认类别偏好配置（根据实际商城类别体系修改）
    'default_category_preferences': {
        # 主要类别 - 根据数据库中的实际类别名称
        '手机通讯': 0.15,           # 手机通讯
        '笔记本': 0.15,            # 笔记本
        '家用电器': 0.1,           # 家用电器
        '服装': 0.1,              # 服装
        '休闲裤': 0.05,            # 休闲裤
        'T恤': 0.05,              # T恤
        '洗衣机': 0.05,            # 洗衣机
        '冰箱': 0.05,             # 冰箱
        '空调': 0.05,             # 空调
        '厨房小电': 0.05,          # 厨房小电
        '食品': 0.05,             # 食品
        '图书': 0.05,             # 图书
        '美妆': 0.05,             # 美妆
        '家居': 0.05,             # 家居
    },
    
    # 价格和质量模型默认参数
    'default_price_model': {
        'mean': 100.0,              # 默认价格均值（请根据商品实际价格范围调整）
        'std': 50.0,                # 默认价格标准差
        'skewness': 0.0,            # 默认偏度
        'min': 1.0,                 # 最小价格
        'max': 10000.0,             # 最大价格
        'median': 100.0,            # 中位价格
        'count': 0                  # 数据点数量
    },
    
    'default_quality_model': {
        'mean': 4.0,                # 默认质量评分均值（1-5分）
        'std': 1.0,                 # 默认评分标准差
        'skewness': 0.0,            # 默认偏度
        'min': 1.0,                 # 最小评分
        'max': 5.0,                 # 最大评分
        'median': 4.0,              # 中位评分
        'count': 0                  # 数据点数量
    }
}

# 数据库表结构（用于参考）
DATABASE_SCHEMA = {
    "mysql_tables": {
        "ums_member": ["id", "member_level_id", "username", "nickname", "phone", "status", "create_time", "icon", "gender", "birthday", "city", "job", "personalized_signature", "integration", "growth"],
        "pms_product": ["id", "brand_id", "product_category_id", "name", "pic", "product_sn", "publish_status", "new_status", "recommand_status", "sort", "sale", "price", "promotion_price", "sub_title", "description", "original_price", "stock", "unit", "weight", "service_ids", "keywords", "brand_name", "product_category_name"],
        "pms_product_category": ["id", "parent_id", "name", "level", "product_count", "product_unit", "show_status", "icon", "keywords", "description"],
        "pms_brand": ["id", "name", "first_letter", "factory_status", "show_status", "product_count", "product_comment_count", "logo", "brand_story"],
        "oms_order": ["id", "member_id", "coupon_id", "order_sn", "create_time", "member_username", "total_amount", "pay_amount", "freight_amount", "promotion_amount", "pay_type", "source_type", "status", "order_type", "receiver_name", "receiver_phone", "receiver_province", "receiver_city", "receiver_region", "receiver_detail_address", "note", "confirm_status", "payment_time", "delivery_time", "receive_time", "comment_time"],
        "oms_order_item": ["id", "order_id", "order_sn", "product_id", "product_pic", "product_name", "product_brand", "product_sn", "product_price", "product_quantity", "product_sku_id", "product_category_id", "real_amount", "product_attr"],
        "oms_cart_item": ["id", "product_id", "product_sku_id", "member_id", "quantity", "price", "create_date", "delete_status", "product_attr"],
        "oms_order_return_apply": ["id", "order_id", "product_id", "order_sn", "create_time", "member_username", "return_amount", "status", "product_name", "product_brand", "product_attr", "product_count", "product_price", "reason", "description"],
        "pms_comment": ["id", "product_id", "member_nick_name", "product_name", "star", "create_time", "show_status", "product_attribute", "content", "pics", "replay_count"],
        "ums_member_statistics_info": ["id", "member_id", "consume_amount", "order_count", "coupon_count", "comment_count", "return_order_count", "login_count", "attend_count", "fans_count", "collect_product_count", "recent_order_time"],
        "ums_member_login_log": ["id", "member_id", "create_time", "ip", "city", "login_type", "province"]
    },
    "mongodb_collections": {
        "memberBrandAttention": ["id", "memberId", "memberNickname", "memberIcon", "brandId", "brandName", "brandLogo", "brandCity", "createTime"],
        "memberProductCollection": ["id", "memberId", "memberNickname", "memberIcon", "productId", "productName", "productPic", "productSubTitle", "productPrice", "createTime"]
    }
}
