import pandas as pd
import numpy as np
from scipy import stats
from sklearn.preprocessing import MinMaxScaler
import json
from datetime import datetime, timedelta
import logging
from typing import Dict, List, Tuple, Any, Optional
import math
from pymongo import MongoClient
from pymongo.collection import Collection
from pymongo.database import Database
import os
from config import MONGODB_CONFIG, RECOMMENDATION_CONFIG

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class MomentumStorage:
    """动量数据MongoDB存储管理"""
    
    def __init__(self, database_name: Optional[str] = None):
        """
        初始化MongoDB连接，使用config.py中的配置
        Args:
            database_name: 数据库名称，如果为None则使用配置文件中的默认值
        """
        # 使用config.py中的MongoDB配置
        mongo_config = MONGODB_CONFIG
        connection_string = f"mongodb://{mongo_config['host']}:{mongo_config['port']}/"
        
        if database_name is None:
            database_name = mongo_config['database']
        
        assert database_name is not None, "Database name cannot be None"
        
        self.client = MongoClient(connection_string)
        self.db: Database = self.client[database_name]
        
        # 集合定义
        self.user_momentum_collection: Collection = self.db['user_momentum']
        self.system_config_collection: Collection = self.db['system_config']
        self.recommendation_logs_collection: Collection = self.db['recommendation_logs']
        
        # 创建索引
        self._create_indexes()
        
        logger.info(f"MongoDB存储已初始化: {database_name} @ {mongo_config['host']}:{mongo_config['port']}")
    
    def _create_indexes(self):
        """创建必要的索引"""
        try:
            # 用户动量索引
            self.user_momentum_collection.create_index("user_id", unique=True)
            self.user_momentum_collection.create_index("last_updated")
            self.user_momentum_collection.create_index("momentum_type")
            
            # 系统配置索引
            self.system_config_collection.create_index("config_key", unique=True)
            
            # 推荐日志索引
            self.recommendation_logs_collection.create_index("user_id")
            self.recommendation_logs_collection.create_index("timestamp")
            self.recommendation_logs_collection.create_index("event_type")
            
            logger.info("MongoDB索引创建完成")
        except Exception as e:
            logger.warning(f"创建索引时出现警告: {e}")
    
    def save_user_momentum(self, user_id: int, momentum_data: Dict[str, Any]) -> bool:
        """
        保存用户动量数据
        Args:
            user_id: 用户ID
            momentum_data: 动量数据
        Returns:
            保存是否成功
        """
        try:
            # 添加元数据
            momentum_record = {
                'user_id': user_id,
                'momentum_data': momentum_data,
                'momentum_type': momentum_data.get('type', 'unknown'),
                'last_updated': momentum_data.get('last_updated', datetime.now().isoformat()),
                'created_at': datetime.now().isoformat(),
                'version': '1.0'
            }
            
            # 使用upsert操作：如果存在则更新，不存在则插入
            result = self.user_momentum_collection.replace_one(
                {'user_id': user_id},
                momentum_record,
                upsert=True
            )
            
            logger.info(f"用户 {user_id} 动量数据已保存到MongoDB")
            return True
            
        except Exception as e:
            logger.error(f"保存用户动量数据失败: {e}")
            return False
    
    def load_user_momentum(self, user_id: int) -> Optional[Dict[str, Any]]:
        """
        加载用户动量数据
        Args:
            user_id: 用户ID
        Returns:
            动量数据，如果不存在返回None
        """
        try:
            record = self.user_momentum_collection.find_one({'user_id': user_id})
            if record:
                logger.info(f"从MongoDB加载用户 {user_id} 的动量数据")
                return record['momentum_data']
            return None
            
        except Exception as e:
            logger.error(f"加载用户动量数据失败: {e}")
            return None
    
    def delete_user_momentum(self, user_id: int) -> bool:
        """
        删除用户动量数据
        Args:
            user_id: 用户ID
        Returns:
            删除是否成功
        """
        try:
            result = self.user_momentum_collection.delete_one({'user_id': user_id})
            if result.deleted_count > 0:
                logger.info(f"用户 {user_id} 的动量数据已从MongoDB删除")
                return True
            return False
            
        except Exception as e:
            logger.error(f"删除用户动量数据失败: {e}")
            return False
    
    def save_system_config(self, config_key: str, config_data: Dict[str, Any]) -> bool:
        """
        保存系统配置参数
        Args:
            config_key: 配置键名
            config_data: 配置数据
        Returns:
            保存是否成功
        """
        try:
            config_record = {
                'config_key': config_key,
                'config_data': config_data,
                'last_updated': datetime.now().isoformat(),
                'version': '1.0'
            }
            
            result = self.system_config_collection.replace_one(
                {'config_key': config_key},
                config_record,
                upsert=True
            )
            
            logger.info(f"系统配置 {config_key} 已保存到MongoDB")
            return True
            
        except Exception as e:
            logger.error(f"保存系统配置失败: {e}")
            return False
    
    def load_system_config(self, config_key: str) -> Optional[Dict[str, Any]]:
        """
        加载系统配置参数
        Args:
            config_key: 配置键名
        Returns:
            配置数据，如果不存在返回None
        """
        try:
            record = self.system_config_collection.find_one({'config_key': config_key})
            if record:
                return record['config_data']
            return None
            
        except Exception as e:
            logger.error(f"加载系统配置失败: {e}")
            return None
    
    def log_recommendation_event(self, user_id: int, event_type: str, 
                               event_data: Dict[str, Any]) -> bool:
        """
        记录推荐事件到MongoDB
        Args:
            user_id: 用户ID
            event_type: 事件类型
            event_data: 事件数据
        Returns:
            记录是否成功
        """
        try:
            log_record = {
                'user_id': user_id,
                'event_type': event_type,
                'event_data': event_data,
                'timestamp': datetime.now().isoformat(),
                'version': '1.0'
            }
            
            self.recommendation_logs_collection.insert_one(log_record)
            return True
            
        except Exception as e:
            logger.error(f"记录推荐事件失败: {e}")
            return False
    
    def get_user_momentum_history(self, user_id: int, limit: int = 10) -> List[Dict]:
        """
        获取用户动量变化历史（通过推荐日志）
        Args:
            user_id: 用户ID
            limit: 返回记录数量限制
        Returns:
            历史记录列表
        """
        try:
            cursor = self.recommendation_logs_collection.find(
                {'user_id': user_id}
            ).sort('timestamp', -1).limit(limit)
            
            return list(cursor)
            
        except Exception as e:
            logger.error(f"获取用户动量历史失败: {e}")
            return []
    
    def get_system_stats(self) -> Dict[str, Any]:
        """获取系统统计信息"""
        try:
            stats = {
                'total_users': self.user_momentum_collection.count_documents({}),
                'momentum_types': {},
                'recent_activity': self.recommendation_logs_collection.count_documents({
                    'timestamp': {'$gte': (datetime.now() - timedelta(hours=24)).isoformat()}
                }),
                'database_name': self.db.name
            }
            
            # 统计各种动量类型的用户数
            pipeline = [
                {'$group': {'_id': '$momentum_type', 'count': {'$sum': 1}}}
            ]
            momentum_stats = list(self.user_momentum_collection.aggregate(pipeline))
            for stat in momentum_stats:
                stats['momentum_types'][stat['_id']] = stat['count']
            
            return stats
            
        except Exception as e:
            logger.error(f"获取系统统计失败: {e}")
            return {}
    
    def cleanup_old_logs(self, days: int = 30) -> int:
        """
        清理旧的推荐日志
        Args:
            days: 保留天数
        Returns:
            删除的记录数
        """
        try:
            cutoff_date = (datetime.now() - timedelta(days=days)).isoformat()
            result = self.recommendation_logs_collection.delete_many({
                'timestamp': {'$lt': cutoff_date}
            })
            
            deleted_count = result.deleted_count
            logger.info(f"清理了 {deleted_count} 条 {days} 天前的推荐日志")
            return deleted_count
            
        except Exception as e:
            logger.error(f"清理旧日志失败: {e}")
            return 0
    
    def close(self):
        """关闭MongoDB连接"""
        try:
            self.client.close()
            logger.info("MongoDB连接已关闭")
        except Exception as e:
            logger.error(f"关闭MongoDB连接失败: {e}")

class PersonalizedRecommendationSystem:
    """基于动量的个性化推荐系统"""
    
    def __init__(self, storage: Optional[MomentumStorage] = None):
        # MongoDB存储
        self.storage = storage
        
        # 从数据库加载配置或使用默认值
        self._load_system_config()
        
        # 数据预处理器
        self.scaler = MinMaxScaler()
        
    def _load_system_config(self):
        """从MongoDB加载系统配置，如果不存在则使用config.py中的默认值并保存"""
        # 使用config.py中的推荐系统配置作为默认值
        default_config = RECOMMENDATION_CONFIG.copy()
        
        if self.storage:
            # 尝试从数据库加载配置
            loaded_config = self.storage.load_system_config('recommendation_system')
            if loaded_config:
                logger.info("从MongoDB加载系统配置")
                config = loaded_config
            else:
                logger.info("使用config.py默认配置并保存到MongoDB")
                config = default_config
                self.storage.save_system_config('recommendation_system', config)
        else:
            logger.info("未连接MongoDB，使用config.py默认配置")
            config = default_config
        
        # 设置配置参数
        self.behavior_weights = config['behavior_weights']
        self.softmax_temperature = config['softmax_temperature']
        self.momentum_decay = config['momentum_decay']
        
        # 推送动量配置（如果config.py中没有，使用默认值）
        self.push_momentum_config = config.get('push_momentum_config', {
            'price_std_multiplier': 1.0,      # 价格标准差乘数
            'quality_std_multiplier': 1.0,    # 质量标准差乘数
            'category_boost_factor': 1.2,     # 类型权重提升因子
        })
        
    def update_system_config(self, new_config: Dict[str, Any]) -> bool:
        """
        更新系统配置参数
        Args:
            new_config: 新的配置参数
        Returns:
            更新是否成功
        """
        try:
            # 更新内存中的配置
            if 'behavior_weights' in new_config:
                self.behavior_weights.update(new_config['behavior_weights'])
            if 'softmax_temperature' in new_config:
                self.softmax_temperature = new_config['softmax_temperature']
            if 'push_momentum_config' in new_config:
                self.push_momentum_config.update(new_config['push_momentum_config'])
            if 'momentum_decay' in new_config:
                self.momentum_decay = new_config['momentum_decay']
            
            # 保存到数据库
            if self.storage:
                current_config = {
                    'behavior_weights': self.behavior_weights,
                    'softmax_temperature': self.softmax_temperature,
                    'push_momentum_config': self.push_momentum_config,
                    'momentum_decay': self.momentum_decay,
                }
                return self.storage.save_system_config('recommendation_system', current_config)
            
            logger.info("系统配置已更新")
            return True
            
        except Exception as e:
            logger.error(f"更新系统配置失败: {e}")
            return False
        
    def softmax(self, x, temperature: float = 1.0) -> np.ndarray:
        """
        Softmax归一化函数
        Args:
            x: 输入数据（可以是list或ndarray）
            temperature: 温度参数，控制分布的锐度
        Returns:
            归一化后的概率分布
        """
        x = np.array(x, dtype=np.float64) / temperature
        # 数值稳定性：减去最大值
        x_shifted = x - np.max(x)
        exp_x = np.exp(x_shifted)
        return exp_x / np.sum(exp_x)
    
    def calculate_biased_normal_params(self, data: List[float]) -> Dict[str, float]:
        """
        计算有偏正态分布参数
        Args:
            data: 输入数据列表
        Returns:
            包含统计参数的字典
        """
        if not data:
            return {
                'mean': 0.0, 'std': 1.0, 'skewness': 0.0,
                'min': 0.0, 'max': 0.0, 'median': 0.0,
                'count': 0
            }
        
        # 过滤无效数据并转换为NumPy数组
        valid_data = [x for x in data if x is not None and not np.isnan(x) and not np.isinf(x)]
        if not valid_data:
            return {
                'mean': 0.0, 'std': 1.0, 'skewness': 0.0,
                'min': 0.0, 'max': 0.0, 'median': 0.0,
                'count': 0
            }
        
        data_np = np.array(valid_data)
        
        # 计算基础统计量
        mean = np.mean(data_np)
        # 使用样本标准差 (ddof=1)，当样本量小于2时使用总体标准差
        std = np.std(data_np, ddof=1 if len(data_np) > 1 else 0)
        # 避免标准差为0的情况
        std = max(std, 1e-6)
        
        # 计算偏度
        try:
            skewness = float(stats.skew(data_np))
        except:
            skewness = 0.0
        
        return {
            'mean': float(mean),
            'std': float(std),
            'skewness': skewness,
            'min': float(np.min(data_np)),
            'max': float(np.max(data_np)),
            'median': float(np.median(data_np)),
            'count': len(valid_data)
        }
    
    def compute_long_term_momentum(self, user_behavior_data: Dict) -> Dict[str, Any]:
        """
        计算长期动量（基础偏好分布）- 用户稳定的长期偏好分布
        这是用户在各个条件/标签下的稳定偏好基准
        
        计算流程：泛行为权重 -> 加权采样 -> 分别计算（类型、价格、质量）三大偏好分布 -> Softmax归一化
        """
        orders_df = user_behavior_data.get('orders', pd.DataFrame())
        cart_df = user_behavior_data.get('cart', pd.DataFrame())
        returns_df = user_behavior_data.get('returns', pd.DataFrame())
        
        # 1. 计算加权行为数据（泛行为权重应用）
        weighted_data = []
        
        # 处理订单数据
        for _, order in orders_df.iterrows():
            weighted_data.append({
                'category': order.get('category', ''),
                'price': order.get('price', 0),
                'rating': order.get('rating', 0),
                'weight': self.behavior_weights['purchase']
            })
        
        # 处理购物车数据
        for _, cart_item in cart_df.iterrows():
            weighted_data.append({
                'category': cart_item.get('category', ''),
                'price': cart_item.get('price', 0),
                'rating': cart_item.get('rating', 0),
                'weight': self.behavior_weights['cart']
            })
        
        # 处理退货数据
        for _, return_item in returns_df.iterrows():
            weighted_data.append({
                'category': return_item.get('category', ''),
                'price': return_item.get('price', 0),
                'rating': return_item.get('rating', 0),
                'weight': self.behavior_weights['return']
            })
        
        if not weighted_data:
            return self._default_long_term_momentum()
        
        # 2. 计算三大偏好模型
        
        # 2.1 类型偏好：每个类型独立赋权，由商品类型权值统一管理
        category_weights = {}
        for item in weighted_data:
            category = item['category']
            weight = item['weight']
            category_weights[category] = category_weights.get(category, 0) + weight
        
        # 使用Softmax归一化类型权重（模型权重部分）
        if category_weights:
            categories = list(category_weights.keys())
            weights = list(category_weights.values())
            normalized_category_weights = self.softmax(weights, self.softmax_temperature)
            category_preferences = dict(zip(categories, normalized_category_weights))
        else:
            category_preferences = {}
        
        # 2.2 价格偏好模型：分析购买成功商品价格分布，建立有偏正态分布
        positive_weight_data = [item for item in weighted_data if item['weight'] > 0]
        if positive_weight_data:
            weighted_prices = []
            for item in positive_weight_data:
                repeat_count = max(1, int(item['weight']))
                weighted_prices.extend([item['price']] * repeat_count)
            price_model = self.calculate_biased_normal_params(weighted_prices)
        else:
            price_model = self.calculate_biased_normal_params([])
        
        # 2.3 质量偏好模型：基于商品评价分数作为质量标准
        if positive_weight_data:
            weighted_ratings = []
            for item in positive_weight_data:
                repeat_count = max(1, int(item['weight']))
                weighted_ratings.extend([item['rating']] * repeat_count)
            quality_model = self.calculate_biased_normal_params(weighted_ratings)
        else:
            quality_model = self.calculate_biased_normal_params([])
        
        return {
            'type': 'long_term',
            'category_preferences': category_preferences,
            'price_model': price_model,
            'quality_model': quality_model,
            'last_updated': datetime.now().isoformat(),
            'data_source': 'user_behavior_analysis'
        }
    
    def _default_long_term_momentum(self) -> Dict[str, Any]:
        """
        默认长期动量配置
        当用户没有历史行为数据时使用config.py中的默认值
        """
        return {
            'type': 'long_term',
            'category_preferences': {
                # 可以根据商城的主要类别设置默认偏好
                'electronics': 0.2,
                'clothing': 0.2,
                'books': 0.15,
                'home': 0.15,
                'sports': 0.1,
                'food': 0.1,
                'beauty': 0.1
            },
            'price_model': RECOMMENDATION_CONFIG['default_price_model'].copy(),
            'quality_model': RECOMMENDATION_CONFIG['default_quality_model'].copy(),
            'last_updated': datetime.now().isoformat(),
            'is_default': True,
            'data_source': 'config_default'
        }
    
    def compute_short_term_momentum(self, clicked_product: Dict, click_strength: float = 1.0) -> Dict[str, Any]:
        """
        计算短期动量（即时偏好变化）
        本质上仍是用户偏好分布，表示用户即时兴趣变化的方向和强度
        这是对长期偏好分布的瞬时调整量
        """
        return {
            'type': 'short_term',
            'clicked_category': clicked_product.get('category', ''),
            'clicked_price': clicked_product.get('price', 0),
            'clicked_rating': clicked_product.get('rating', 0),
            'click_strength': click_strength,
            'timestamp': datetime.now().isoformat(),
            'data_source': 'user_click'
        }
    
    def compute_push_momentum(self, long_term_momentum: Dict, short_term_momentum: Dict, 
                            current_push_momentum: Optional[Dict] = None) -> Dict[str, Any]:
        """
        计算推送动量 - 用户偏好分布的累积状态
        
        核心公式：推送动量 += 长期动量 + 短期动量
        
        推送动量本质上仍然是用户在各个条件/标签下的偏好分布，
        与长期、短期动量是同一类型的数据结构，只是会累积保留历史影响。
        长期动量提供稳定的偏好基准，短期动量提供即时的偏好变化。
        """
        # 获取当前推送动量作为基础（如果存在）
        if current_push_momentum and current_push_momentum.get('type') == 'push_momentum':
            base_push_momentum = current_push_momentum
        else:
            # 如果没有现有推送动量，从长期动量初始化
            base_push_momentum = {
                'type': 'push_momentum',
                'category_preferences': long_term_momentum.get('category_preferences', {}).copy(),
                'price_model': long_term_momentum.get('price_model', {}).copy(),
                'quality_model': long_term_momentum.get('quality_model', {}).copy(),
                'cumulative_clicks': 0,
                'last_updated': datetime.now().isoformat(),
                'data_source': 'initialized_from_long_term'
            }
        
        # 获取基础数据
        clicked_category = short_term_momentum.get('clicked_category', '')
        clicked_price = short_term_momentum.get('clicked_price', 0)
        clicked_rating = short_term_momentum.get('clicked_rating', 0)
        click_strength = short_term_momentum.get('click_strength', 1.0)
        
        # 复制当前推送动量作为新的推送动量（保持偏好分布结构）
        new_push_momentum = {
            'type': 'push_momentum',
            'category_preferences': base_push_momentum.get('category_preferences', {}).copy(),
            'price_model': base_push_momentum.get('price_model', {}).copy(),
            'quality_model': base_push_momentum.get('quality_model', {}).copy(),
            'cumulative_clicks': base_push_momentum.get('cumulative_clicks', 0) + 1,
            'last_updated': datetime.now().isoformat(),
            'data_source': 'cumulative_push_momentum'
        }
        
        # 1. 累积更新类型偏好分布 - 在当前推送偏好基础上叠加影响
        if clicked_category:
            current_prefs = new_push_momentum['category_preferences']
            
            # 获取长期偏好分布中的权重
            long_term_weight = long_term_momentum.get('category_preferences', {}).get(clicked_category, 0.1)
            
            # 累积更新偏好分布：当前偏好 += 长期基准偏好 + 短期偏好变化
            current_weight = current_prefs.get(clicked_category, 0.0)
            boost_factor = self.push_momentum_config['category_boost_factor']
            
            # 累积更新：当前偏好 + 长期偏好影响 + 点击偏好增量
            long_term_influence = long_term_weight * 0.1  # 长期偏好分布的稳定影响
            short_term_influence = click_strength * boost_factor * 0.2  # 短期偏好变化
            
            new_weight = current_weight + long_term_influence + short_term_influence
            current_prefs[clicked_category] = min(new_weight, 1.0)  # 防止权重过大
            
            # 重新归一化偏好分布
            if current_prefs and sum(current_prefs.values()) > 0:
                categories = list(current_prefs.keys())
                weights = list(current_prefs.values())
                normalized_weights = self.softmax(weights, self.softmax_temperature)
                new_push_momentum['category_preferences'] = dict(zip(categories, normalized_weights))
        
        # 2. 累积更新价格偏好分布 - 在当前推送偏好基础上调整
        if clicked_price > 0:
            current_price_model = new_push_momentum['price_model']
            long_term_price_model = long_term_momentum.get('price_model', {})
            
            current_mean = current_price_model.get('mean', 100.0)
            current_std = current_price_model.get('std', 50.0)
            long_term_mean = long_term_price_model.get('mean', 100.0)
            
            # 累积调整偏好分布：当前偏好 + 长期偏好基准拉力 + 短期偏好推力
            long_term_pull = (long_term_mean - current_mean) * 0.05  # 长期偏好分布的稳定拉力
            short_term_push = (clicked_price - current_mean) * click_strength * 0.1  # 短期偏好推力
            
            new_mean = current_mean + long_term_pull + short_term_push
            
            # 标准差的累积调整：逐渐扩大偏好接受范围
            std_expansion = click_strength * 0.05 * self.push_momentum_config['price_std_multiplier']
            new_std = current_std * (1 + std_expansion)
            
            current_price_model['mean'] = new_mean
            current_price_model['std'] = new_std
        
        # 3. 累积更新质量偏好分布
        if clicked_rating > 0:
            current_quality_model = new_push_momentum['quality_model']
            long_term_quality_model = long_term_momentum.get('quality_model', {})
            
            current_mean = current_quality_model.get('mean', 4.0)
            current_std = current_quality_model.get('std', 1.0)
            long_term_mean = long_term_quality_model.get('mean', 4.0)
            
            # 累积调整质量偏好分布
            long_term_pull = (long_term_mean - current_mean) * 0.03  # 长期质量偏好的稍微拉力
            short_term_push = (clicked_rating - current_mean) * click_strength * 0.05  # 短期质量偏好推力
            
            new_mean = max(1.0, min(5.0, current_mean + long_term_pull + short_term_push))
            
            # 质量偏好标准差的适度扩展
            std_expansion = click_strength * 0.02 * self.push_momentum_config['quality_std_multiplier']
            new_std = current_std * (1 + std_expansion)
            
            current_quality_model['mean'] = new_mean
            current_quality_model['std'] = new_std
        
        # 记录本次更新的详细信息
        new_push_momentum['latest_update'] = {
            'long_term_base': long_term_momentum.get('type', 'unknown'),
            'short_term_influence': short_term_momentum,
            'update_timestamp': datetime.now().isoformat(),
            'note': '推送动量本质上仍是用户偏好分布，与长期短期动量是同类数据结构'
        }
        
        return new_push_momentum
    
    def update_short_term_momentum(self, long_term_momentum: Dict, 
                                 clicked_product: Dict, momentum_strength: float = 0.3) -> Dict:
        """
        ⚠️ 已弃用：请使用新的推送系统
        为了兼容性保留此方法，实际调用新的推送计算
        """
        # 计算短期动量
        short_term = self.compute_short_term_momentum(clicked_product, momentum_strength)
        
        # 计算推送动量
        push_momentum = self.compute_push_momentum(long_term_momentum, short_term)
        
        # 为了向后兼容，返回推送动量格式
        push_momentum['is_short_term'] = True
        push_momentum['momentum_strength'] = momentum_strength
        
        return push_momentum
    
    def calculate_product_score(self, product: Dict, momentum: Dict) -> float:
        """
        基于动量计算商品得分
        Args:
            product: 商品信息
            momentum: 用户动量（长期或短期）
        Returns:
            商品推荐得分
        """
        # 1. 类型得分
        category = product.get('category', '')
        category_prefs = momentum.get('category_preferences', {})
        category_score = category_prefs.get(category, 0.05)  # 未知类型给予小权重
        
        # 2. 价格得分 - 使用改进的正态分布评分
        price = product.get('price', 0)
        price_model = momentum.get('price_model', {})
        if price > 0 and price_model.get('count', 0) > 0:
            mean = price_model.get('mean', 100.0)
            std = price_model.get('std', 50.0)
            
            # 使用正态分布的概率密度，但进行标准化处理
            max_pdf = stats.norm.pdf(mean, mean, std)  # 在均值处的最大概率密度
            current_pdf = stats.norm.pdf(price, mean, std)
            price_score = current_pdf / max_pdf if max_pdf > 0 else 0.1
            
            # 对于极端价格给予惩罚
            z_score = abs(price - mean) / std if std > 0 else 0
            if z_score > 3:  # 超过3个标准差的价格
                price_score *= 0.1
        else:
            price_score = 0.5  # 无历史数据时给予中等得分
        
        # 3. 质量得分
        rating = product.get('rating', 0)
        quality_model = momentum.get('quality_model', {})
        if rating > 0 and quality_model.get('count', 0) > 0:
            mean = quality_model.get('mean', 4.0)
            std = quality_model.get('std', 1.0)
            
            # 质量评分使用不同的策略：偏向高质量
            if rating >= mean:
                # 高于平均质量的商品得分更高
                quality_score = 0.5 + 0.5 * (rating - mean) / (5.0 - mean) if mean < 5.0 else 1.0
            else:
                # 低于平均质量的商品得分按正态分布
                max_pdf = stats.norm.pdf(mean, mean, std)
                current_pdf = stats.norm.pdf(rating, mean, std)
                quality_score = (current_pdf / max_pdf) * 0.5 if max_pdf > 0 else 0.1
        else:
            quality_score = 0.5  # 无历史数据时给予中等得分
        
        # 4. 综合得分（权重可配置）
        # 类型偏好权重较高，因为它直接反映用户兴趣
        # 价格和质量权重相当，但可以根据业务需求调整
        total_score = (category_score * 0.5 + 
                      price_score * 0.25 + 
                      quality_score * 0.25)
        
        # 5. 应用额外的业务规则
        # 例如：新品加成、库存影响等
        if product.get('is_new', False):
            total_score *= 1.1  # 新品10%加成
        
        if product.get('stock', 100) <= 0:
            total_score *= 0.1  # 无库存大幅降分
        elif product.get('stock', 100) <= 5:
            total_score *= 0.8  # 低库存适度降分
        
        # 确保得分在合理范围内
        return max(0.0, min(1.0, total_score))
    
    def recommend_products(self, products: List[Dict], momentum: Dict, 
                         top_k: int = 10) -> List[Dict]:
        """基于动量推荐商品"""
        scored_products = []
        
        for product in products:
            score = self.calculate_product_score(product, momentum)
            scored_products.append({
                **product,
                'recommendation_score': score
            })
        
        # 按得分排序
        scored_products.sort(key=lambda x: x['recommendation_score'], reverse=True)
        
        return scored_products[:top_k]
    
    def log_recommendation_event(self, user_id: int, event_type: str, 
                               products: List[Dict], momentum: Dict):
        """记录推荐事件日志"""
        log_data = {
            'user_id': user_id,
            'event_type': event_type,  # 'recommendation', 'click', 'purchase'
            'timestamp': datetime.now().isoformat(),
            'products': products,
            'momentum_snapshot': momentum,
            'system_version': '1.0'
        }
        
        # 这里可以保存到MongoDB或其他日志系统
        logger.info(f"推荐事件记录: {event_type} for user {user_id}")
        return log_data

    def apply_momentum_decay(self, momentum: Dict, hours_since_update: float = 24.0) -> Dict:
        """
        应用动量衰减
        短期动量会随时间衰减，回归到长期动量
        Args:
            momentum: 当前动量
            hours_since_update: 距离上次更新的小时数
        Returns:
            衰减后的动量
        """
        if not momentum.get('is_short_term', False):
            return momentum  # 长期动量不需要衰减
        
        # 计算衰减系数（指数衰减）
        decay_rate = 0.1  # 每小时衰减10%
        decay_factor = math.exp(-decay_rate * hours_since_update)
        
        # 对短期影响进行衰减
        original_strength = momentum.get('momentum_strength', 0.3)
        decayed_strength = original_strength * decay_factor
        
        if decayed_strength < 0.05:  # 衰减到很小时，移除短期标记
            momentum = momentum.copy()
            momentum.pop('is_short_term', None)
            momentum.pop('momentum_strength', None)
        else:
            momentum['momentum_strength'] = decayed_strength
        
        return momentum
    
    def validate_product_data(self, product: Dict) -> bool:
        """
        验证商品数据的完整性
        Args:
            product: 商品数据
        Returns:
            数据是否有效
        """
        required_fields = ['product_id', 'category', 'price']
        
        # 检查必需字段
        for field in required_fields:
            if field not in product or product[field] is None:
                logger.warning(f"商品数据缺少必需字段: {field}")
                return False
        
        # 检查数值字段的有效性
        if not isinstance(product.get('price', 0), (int, float)) or product['price'] < 0:
            logger.warning(f"商品价格无效: {product.get('price')}")
            return False
        
        rating = product.get('rating', 0)
        if rating is not None and (not isinstance(rating, (int, float)) or rating < 0 or rating > 5):
            logger.warning(f"商品评分无效: {rating}")
            return False
        
        return True
    
    def get_momentum_summary(self, momentum: Dict) -> Dict[str, Any]:
        """
        获取动量的摘要信息，用于日志和调试
        Args:
            momentum: 动量数据
        Returns:
            摘要信息
        """
        category_prefs = momentum.get('category_preferences', {})
        price_model = momentum.get('price_model', {})
        quality_model = momentum.get('quality_model', {})
        
        # 找出偏好最高的类别
        top_categories = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)[:3]
        
        return {
            'type': 'short_term' if momentum.get('is_short_term') else 'long_term',
            'top_categories': top_categories,
            'price_preference': {
                'mean': price_model.get('mean', 0),
                'range': f"[{price_model.get('mean', 0) - price_model.get('std', 0):.1f}, "
                        f"{price_model.get('mean', 0) + price_model.get('std', 0):.1f}]"
            },
            'quality_preference': {
                'mean': quality_model.get('mean', 0),
                'preferred_range': f"{quality_model.get('mean', 0):.1f}±{quality_model.get('std', 0):.1f}"
            },
            'last_updated': momentum.get('last_updated'),
            'data_points': price_model.get('count', 0)
        }

class RecommendationAPI:
    """推荐系统API接口 - 使用MongoDB持久化存储"""
    
    def __init__(self, storage: Optional[MomentumStorage] = None):
        # 初始化存储
        if storage is None:
            storage = MomentumStorage()
        self.storage = storage
        
        # 初始化推荐系统，传入存储实例
        self.rec_system = PersonalizedRecommendationSystem(storage)
        
        logger.info("推荐API已初始化，使用MongoDB持久化存储")
    
    def _get_user_momentum(self, user_id: int) -> Optional[Dict[str, Any]]:
        """从MongoDB获取用户动量数据"""
        return self.storage.load_user_momentum(user_id)
    
    def _save_user_momentum(self, user_id: int, momentum_data: Dict[str, Any]) -> bool:
        """保存用户动量数据到MongoDB"""
        return self.storage.save_user_momentum(user_id, momentum_data)
    
    def get_user_recommendations(self, user_id: int, products: List[Dict], 
                               top_k: int = 10, include_scores: bool = False) -> List[Dict]:
        """
        获取用户个性化推荐
        Args:
            user_id: 用户ID
            products: 候选商品列表
            top_k: 返回推荐数量
            include_scores: 是否包含推荐得分
        Returns:
            推荐商品列表
        """
        try:
            # 过滤无效商品
            valid_products = [p for p in products if self.rec_system.validate_product_data(p)]
            if not valid_products:
                logger.warning("没有有效的商品数据")
                return []
            
            # 从MongoDB获取用户动量
            current_momentum = self._get_user_momentum(user_id)
            if current_momentum is None:
                # 如果没有动量数据，使用默认长期动量
                current_momentum = self.rec_system._default_long_term_momentum()
                # 保存默认动量到数据库
                self._save_user_momentum(user_id, current_momentum)
                logger.info(f"为用户 {user_id} 创建默认动量")
            
            # 生成推荐
            recommendations = self.rec_system.recommend_products(
                valid_products, current_momentum, top_k
            )
            
            # 是否包含推荐得分
            if not include_scores:
                for rec in recommendations:
                    rec.pop('recommendation_score', None)
            
            # 记录推荐事件到MongoDB
            self.storage.log_recommendation_event(
                user_id, 'recommendation', {
                    'products': recommendations,
                    'momentum_snapshot': current_momentum,
                    'request_params': {'top_k': top_k, 'include_scores': include_scores}
                }
            )
            
            logger.info(f"为用户 {user_id} 生成了 {len(recommendations)} 个推荐")
            return recommendations
            
        except Exception as e:
            logger.error(f"推荐生成失败: {e}")
            # 返回随机推荐作为降级方案
            import random
            fallback_products = random.sample(products, min(top_k, len(products)))
            return fallback_products
    
    def handle_product_click(self, user_id: int, clicked_product: Dict, 
                           click_strength: float = 0.3) -> bool:
        """
        处理商品点击事件 - 更新MongoDB中的动量数据
        Args:
            user_id: 用户ID
            clicked_product: 点击的商品信息
            click_strength: 点击强度
        Returns:
            处理是否成功
        """
        try:
            if not self.rec_system.validate_product_data(clicked_product):
                logger.warning("点击的商品数据无效")
                return False
            
            # 从MongoDB获取当前动量
            current_momentum = self._get_user_momentum(user_id)
            if current_momentum is None:
                current_momentum = self.rec_system._default_long_term_momentum()
            
            # 更新短期动量
            short_term_momentum = self.rec_system.update_short_term_momentum(
                current_momentum, clicked_product, click_strength
            )
            
            # 保存更新后的动量到MongoDB
            self._save_user_momentum(user_id, short_term_momentum)
            
            # 记录点击事件到MongoDB
            self.storage.log_recommendation_event(
                user_id, 'click', {
                    'clicked_product': clicked_product,
                    'click_strength': click_strength,
                    'updated_momentum': short_term_momentum
                }
            )
            
            logger.info(f"用户 {user_id} 点击商品 {clicked_product.get('product_id')}，动量已更新并保存到MongoDB")
            return True
            
        except Exception as e:
            logger.error(f"处理点击事件失败: {e}")
            return False
    
    def batch_handle_clicks(self, user_id: int, clicked_products: List[Dict]) -> int:
        """
        批量处理点击事件
        Args:
            user_id: 用户ID
            clicked_products: 点击的商品列表
        Returns:
            成功处理的点击数量
        """
        success_count = 0
        # 批量点击时，降低单次点击的影响强度
        batch_strength = 0.1
        
        for product in clicked_products:
            if self.handle_product_click(user_id, product, batch_strength):
                success_count += 1
        
        logger.info(f"批量处理用户 {user_id} 的 {len(clicked_products)} 次点击，成功 {success_count} 次")
        return success_count
    
    def get_user_momentum_summary(self, user_id: int) -> Dict[str, Any]:
        """
        获取用户动量摘要
        Args:
            user_id: 用户ID
        Returns:
            动量摘要信息
        """
        momentum = self._get_user_momentum(user_id)
        if momentum is None:
            return {"status": "no_data", "message": "用户暂无动量数据"}
        
        summary = self.rec_system.get_momentum_summary(momentum)
        summary["user_id"] = user_id
        summary["storage_status"] = "MongoDB持久化存储"
        
        return summary
    
    def clear_user_data(self, user_id: Optional[int] = None) -> bool:
        """
        清除用户MongoDB数据
        Args:
            user_id: 用户ID，如果为None则清除所有用户数据
        Returns:
            操作是否成功
        """
        try:
            if user_id is None:
                # 清除所有用户动量数据
                result = self.storage.db['user_momentum'].delete_many({})
                logger.info(f"已清除所有用户动量数据，删除了 {result.deleted_count} 条记录")
            else:
                # 清除特定用户数据
                result = self.storage.db['user_momentum'].delete_many({"user_id": user_id})
                logger.info(f"已清除用户 {user_id} 的动量数据，删除了 {result.deleted_count} 条记录")
            return True
        except Exception as e:
            logger.error(f"清除用户数据失败: {e}")
            return False
    
    def update_user_long_term_momentum(self, user_id: int, user_behavior_data: Dict) -> Optional[Dict]:
        """
        更新用户长期动量 - 保存到MongoDB
        Args:
            user_id: 用户ID
            user_behavior_data: 用户行为数据
        Returns:
            更新后的长期动量
        """
        try:
            long_term_momentum = self.rec_system.compute_long_term_momentum(user_behavior_data)
            # 直接保存到MongoDB
            self._save_user_momentum(user_id, long_term_momentum)
            
            logger.info(f"用户 {user_id} 长期动量已更新并保存到MongoDB，数据点数: {long_term_momentum.get('price_model', {}).get('count', 0)}")
            return long_term_momentum
        except Exception as e:
            logger.error(f"更新长期动量失败: {e}")
            return None
    
    def get_system_stats(self) -> Dict[str, Any]:
        """获取系统统计信息 - 从MongoDB获取"""
        mongo_stats = self.storage.get_system_stats()
        return {
            "storage_type": "MongoDB持久化存储",
            "database_info": {
                "host": MONGODB_CONFIG['host'],
                "port": MONGODB_CONFIG['port'],
                "database": MONGODB_CONFIG['database']
            },
            "system_version": "2.0_with_MongoDB",
            **mongo_stats
        }
    
    def generate_recommendations_with_three_momentum(self, user_id: int, products: List[Dict], 
                                                   clicked_product: Optional[Dict] = None,
                                                   top_k: int = 10) -> List[Dict]:
        """
        使用三动量系统生成推荐 - 基于MongoDB存储
        正确的动量理解：
        - 推送动量 = 累积偏好分布状态（保留所有历史点击影响）
        - 长期动量 = 基础偏好分布（稳定基准）
        - 短期动量 = 偏好变化增量（单次点击的影响）
        
        公式：推送动量 += 长期动量 + 短期动量
        """
        try:
            # 1. 获取长期动量（基础偏好分布/稳定基准）
            long_term_momentum = None
            current_momentum = self._get_user_momentum(user_id)
            
            # 如果数据库中有长期动量，使用它；否则使用默认
            if current_momentum and current_momentum.get('type') == 'long_term':
                long_term_momentum = current_momentum
            else:
                # 使用默认长期动量
                long_term_momentum = self.rec_system._default_long_term_momentum()
            
            # 2. 获取当前推送动量（累积偏好分布状态）
            current_push_momentum = None
            if current_momentum and current_momentum.get('type') == 'push_momentum':
                current_push_momentum = current_momentum
                logger.info(f"用户 {user_id} 使用MongoDB中的推送动量（累积点击：{current_push_momentum.get('cumulative_clicks', 0)}）")
            
            # 3. 如果有点击行为，更新推送动量
            if clicked_product and self.rec_system.validate_product_data(clicked_product):
                # 计算短期动量（偏好变化增量）
                short_term_momentum = self.rec_system.compute_short_term_momentum(clicked_product)
                
                # 计算新的推送动量：推送动量 += 长期动量 + 短期动量
                new_push_momentum = self.rec_system.compute_push_momentum(
                    long_term_momentum, 
                    short_term_momentum,
                    current_push_momentum  # 传入当前推送动量作为基础
                )
                
                # 保存到MongoDB
                self._save_user_momentum(user_id, new_push_momentum)
                
                # 记录点击事件到MongoDB
                self.storage.log_recommendation_event(
                    user_id, 'click_update_push_momentum', {
                        'clicked_product': clicked_product,
                        'updated_momentum': new_push_momentum
                    }
                )
                
                # 使用更新后的推送动量进行推荐
                final_momentum = new_push_momentum
                
                logger.info(f"用户 {user_id} 点击更新推送动量（累积点击：{final_momentum.get('cumulative_clicks', 0)}）")
                
            else:
                # 没有点击行为
                if current_push_momentum:
                    # 使用现有推送动量
                    final_momentum = current_push_momentum
                    logger.info(f"用户 {user_id} 使用现有推送动量生成推荐")
                else:
                    # 使用长期动量初始化推送动量
                    initial_push_momentum = self.rec_system.compute_push_momentum(
                        long_term_momentum,
                        {'clicked_category': '', 'clicked_price': 0, 'clicked_rating': 0, 'click_strength': 0},
                        None
                    )
                    self._save_user_momentum(user_id, initial_push_momentum)
                    final_momentum = initial_push_momentum
                    logger.info(f"用户 {user_id} 初始化推送动量")
            
            # 4. 基于最终推送动量生成推荐
            valid_products = [p for p in products if self.rec_system.validate_product_data(p)]
            if not valid_products:
                logger.warning("没有有效的商品数据")
                return []
            
            recommendations = self.rec_system.recommend_products(
                valid_products, final_momentum, top_k
            )
            
            # 记录推荐事件到MongoDB
            self.storage.log_recommendation_event(
                user_id, 'three_momentum_recommendation', {
                    'products': recommendations,
                    'momentum_snapshot': final_momentum,
                    'request_params': {'top_k': top_k}
                }
            )
            
            return recommendations
            
        except Exception as e:
            logger.error(f"三动量推荐生成失败: {e}")
            # 降级方案
            return self.get_user_recommendations(user_id, products, top_k, include_scores=False)
    
    def explain_momentum_system(self, user_id: int) -> Dict[str, Any]:
        """
        解释当前用户的动量系统状态 - 基于MongoDB存储
        正确的三动量概念：
        - 推送动量：累积偏好分布状态（保留所有历史影响）
        - 长期动量：基础偏好分布（稳定基准）  
        - 短期动量：偏好变化增量（单次点击影响）
        """
        momentum = self._get_user_momentum(user_id)
        if momentum is None:
            return {"status": "no_data", "message": "用户暂无动量数据"}
        
        momentum_type = momentum.get('type', 'unknown')
        explanation = {
            "user_id": user_id,
            "current_momentum_type": momentum_type,
            "storage_info": "MongoDB持久化存储",
            "momentum_explanation": {},
            "system_flow": [],
            "momentum_formula": "推送动量 += 长期动量（稳定基准）+ 短期动量（变化方向）"
        }
        
        if momentum_type == 'long_term':
            explanation["momentum_explanation"] = {
                "类型": "长期动量（基础偏好分布）",
                "含义": "用户稳定、长期的偏好分布基准",
                "本质": "与短期、推送动量同构的偏好分布数据",
                "数据来源": momentum.get('data_source', 'unknown'),
                "类型偏好分布": momentum.get('category_preferences', {}),
                "价格偏好中心": momentum.get('price_model', {}).get('mean', 0),
                "质量偏好中心": momentum.get('quality_model', {}).get('mean', 0)
            }
            explanation["system_flow"] = [
                "1. 分析用户历史行为（订单、购物车、退货）",
                "2. 应用泛行为权重进行加权采样",
                "3. 计算类型、价格、质量三大偏好分布",
                "4. 使用Softmax进行归一化",
                "5. 形成长期偏好分布基准",
                "6. 保存到MongoDB持久化存储"
            ]
        
        elif momentum_type == 'push_momentum':
            cumulative_clicks = momentum.get('cumulative_clicks', 0)
            latest_update = momentum.get('latest_update', {})
            
            explanation["momentum_explanation"] = {
                "类型": "推送动量（累积偏好分布）⭐",
                "含义": "用户偏好分布的累积状态，会保留历史点击的累积影响",
                "本质": "与长期、短期动量同构的偏好分布数据",
                "累积点击次数": cumulative_clicks,
                "最新更新": latest_update.get('update_timestamp', ''),
                "最新点击影响": {
                    "点击类别": latest_update.get('short_term_influence', {}).get('clicked_category', ''),
                    "点击价格": latest_update.get('short_term_influence', {}).get('clicked_price', 0),
                    "点击评分": latest_update.get('short_term_influence', {}).get('clicked_rating', 0),
                    "影响强度": latest_update.get('short_term_influence', {}).get('click_strength', 0)
                },
                "当前类型偏好分布": momentum.get('category_preferences', {}),
                "当前价格偏好中心": momentum.get('price_model', {}).get('mean', 0),
                "当前质量偏好中心": momentum.get('quality_model', {}).get('mean', 0)
            }
            explanation["system_flow"] = [
                "1. 推送动量 = 用户偏好分布的累积状态",
                "2. 用户点击 → 生成短期偏好变化增量",
                "3. 获取长期偏好分布基准",
                "4. 🔄 推送偏好分布 += 长期偏好基准 + 短期偏好增量",
                "5. 对于价格/质量：累积调整正态分布参数",
                "6. 对于类型：累积提升点击类型权重",
                "7. 保存到MongoDB持久化存储",
                "8. 使用更新后的推送偏好分布进行推荐",
                f"📊 当前累积了 {cumulative_clicks} 次点击的影响"
            ]
        
        # 添加动量系统原理说明
        explanation["system_principles"] = {
            "三种动量本质": "都是用户在各条件/标签下的偏好分布，数据结构完全一致",
            "推送动量": "累积的偏好分布状态，会保留历史点击累积影响 ✅",
            "长期动量": "稳定的用户偏好分布基准",
            "短期动量": "每次点击的偏好变化增量",
            "累积机制": "每次点击都会在推送偏好分布基础上累积更新",
            "存储方式": "MongoDB持久化存储，支持分布式部署",
            "公式核心": "推送偏好分布 += 长期偏好基准 + 短期偏好增量"
        }
        
        return explanation

# 全局推荐API实例
recommendation_api = RecommendationAPI()
