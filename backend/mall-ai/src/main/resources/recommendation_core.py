#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
推荐算法核心引擎 - 纯算法计算，无数据库依赖
专注于动量推荐算法的核心计算逻辑
"""

import pandas as pd
import numpy as np
from scipy import stats
from scipy.stats import chi2, kstest
from sklearn.preprocessing import MinMaxScaler
from datetime import datetime, timedelta
import logging
from typing import Dict, List, Tuple, Any, Optional
import math
from config import RECOMMENDATION_CONFIG, LOGGING_CONFIG

# 配置日志
import os
from logging.handlers import RotatingFileHandler

# 创建日志目录
log_dir = os.path.join(os.path.dirname(__file__), 'logs')
os.makedirs(log_dir, exist_ok=True)

# 获取日志配置
log_level = getattr(logging, LOGGING_CONFIG.get('default_level', 'INFO'))
file_level = getattr(logging, LOGGING_CONFIG.get('file_level', 'INFO'))
console_level = getattr(logging, LOGGING_CONFIG.get('console_level', 'INFO'))
detailed_scoring = LOGGING_CONFIG.get('detailed_product_scoring', False)
max_log_size = LOGGING_CONFIG.get('max_log_file_size', 10*1024*1024)
backup_count = LOGGING_CONFIG.get('backup_count', 5)
log_format = LOGGING_CONFIG.get('log_format', '%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# 配置主日志
logging.basicConfig(level=log_level)
logger = logging.getLogger(__name__)

# 添加文件处理器，将长期动量计算日志单独保存
momentum_log_file = os.path.join(log_dir, 'long_term_momentum.log')
file_handler = RotatingFileHandler(
    momentum_log_file, 
    maxBytes=max_log_size,
    backupCount=backup_count
)
file_handler.setLevel(file_level)
file_formatter = logging.Formatter(log_format)
file_handler.setFormatter(file_formatter)

# 创建专用于长期动量计算的日志记录器
momentum_logger = logging.getLogger('momentum_calculation')
momentum_logger.setLevel(file_level)
momentum_logger.addHandler(file_handler)
momentum_logger.propagate = False  # 避免日志重复

class RecommendationEngine:
    """纯算法推荐引擎 - 无存储依赖"""
    
    def __init__(self):
        """初始化推荐引擎，使用config.py中的配置"""
        # 从配置文件加载参数
        config = RECOMMENDATION_CONFIG
        
        self.behavior_weights = config['behavior_weights']
        self.softmax_temperature = config['softmax_temperature']
        self.momentum_decay = config['momentum_decay']
        
        # 推送动量配置
        self.push_momentum_config = config.get('push_momentum_config', {
            'price_std_multiplier': 1.0,
            'quality_std_multiplier': 1.0,
            'category_boost_factor': 1.2,
        })
        
        # 数据预处理器
        self.scaler = MinMaxScaler()
        
        # 是否记录详细的商品评分过程
        self.detailed_scoring = detailed_scoring
        
        logger.info("推荐算法引擎已初始化（纯算法模式）")
    
    def _fit_chi_square_distribution(self, data: List[float]) -> Dict[str, float]:
        """
        内部方法：使用卡方分布拟合数据，通过线性变换调整
        Args:
            data: 输入数据列表
        Returns:
            包含卡方分布参数和检验结果的字典
        """
        if not data or len(data) < 3:
            return {
                'df': 2.0,
                'location': 0.0,
                'scale': 1.0,
                'goodness_of_fit': 0.0,
                'expansion_factor': 1.0
            }
        
        # 过滤无效数据
        valid_data = [x for x in data if x is not None and not np.isnan(x) and not np.isinf(x) and x >= 0]
        if len(valid_data) < 3:
            return self._fit_chi_square_distribution([])
        
        data_np = np.array(valid_data)
        
        # 确保数据为正值（卡方分布要求）
        min_val = np.min(data_np)
        if min_val <= 0:
            data_np = data_np - min_val + 1e-6
        
        # 使用矩估计法估算卡方分布参数
        data_mean = np.mean(data_np)
        data_var = np.var(data_np, ddof=1)
        
        # 线性变换: X = scale * Chi2(df) + location
        try:
            data_skewness = float(stats.skew(data_np))
            if data_skewness > 0:
                estimated_df = max(1.0, min(50.0, 8.0 / (data_skewness ** 2)))
            else:
                estimated_df = 5.0
        except:
            estimated_df = 5.0
        
        # 估算尺度和位置参数
        scale = np.sqrt(data_var / (2 * estimated_df))
        scale = max(scale, 1e-6)
        location = data_mean - scale * estimated_df
        
        # KS检验拟合优度
        try:
            standardized_data = (data_np - location) / scale
            standardized_data = np.maximum(standardized_data, 1e-6)
            
            ks_statistic, p_value = kstest(standardized_data, 
                                         lambda x: chi2.cdf(x, df=estimated_df))
            goodness_of_fit = p_value
        except Exception as e:
            logger.warning(f"卡方分布拟合检验失败: {e}")
            goodness_of_fit = 0.5
        
        # 根据拟合优度计算扩展因子
        if goodness_of_fit < 0.05:
            expansion_factor = 2.0
        elif goodness_of_fit < 0.1:
            expansion_factor = 1.5
        elif goodness_of_fit < 0.2:
            expansion_factor = 1.2
        else:
            expansion_factor = 1.0
        
        # 限制扩展因子不超过1
        expansion_factor = min(expansion_factor, 1.0)
        
        return {
            'df': float(estimated_df),
            'location': float(location),
            'scale': float(scale),
            'goodness_of_fit': float(goodness_of_fit),
            'expansion_factor': float(expansion_factor)
        }
    
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
        """
        # 使用专用日志记录器，同时输出到主日志
        logger.info("🔄 开始计算长期动量...")
        momentum_logger.info("🔄 开始计算长期动量...")
        
        # 处理DataFrame格式或列表格式的数据
        orders_data = user_behavior_data.get('orders', [])
        cart_data = user_behavior_data.get('cart', [])
        returns_data = user_behavior_data.get('returns', [])
        
        # 记录原始数据统计
        logger.info(f"📊 原始行为数据统计: 订单={len(orders_data)}, 购物车={len(cart_data)}, 退货={len(returns_data)}")
        momentum_logger.info(f"📊 原始行为数据统计: 订单={len(orders_data)}, 购物车={len(cart_data)}, 退货={len(returns_data)}")
        
        # 转换为统一格式
        if isinstance(orders_data, pd.DataFrame):
            orders_data = orders_data.to_dict('records')
        if isinstance(cart_data, pd.DataFrame):
            cart_data = cart_data.to_dict('records')
        if isinstance(returns_data, pd.DataFrame):
            returns_data = returns_data.to_dict('records')
        
        # 检查数据结构
        if orders_data and isinstance(orders_data, list) and len(orders_data) > 0:
            logger.info(f"📋 订单数据样例: {orders_data[0]}")
            momentum_logger.info(f"📋 订单数据样例: {orders_data[0]}")
        if cart_data and isinstance(cart_data, list) and len(cart_data) > 0:
            logger.info(f"📋 购物车数据样例: {cart_data[0]}")
            momentum_logger.info(f"📋 购物车数据样例: {cart_data[0]}")
        if returns_data and isinstance(returns_data, list) and len(returns_data) > 0:
            logger.info(f"📋 退货数据样例: {returns_data[0]}")
            momentum_logger.info(f"📋 退货数据样例: {returns_data[0]}")
        
        # 1. 计算加权行为数据（泛行为权重应用）
        weighted_data = []
        category_missing_count = 0
        price_missing_count = 0
        rating_missing_count = 0
        
        # 处理订单数据
        logger.info("⚙️ 处理订单数据...")
        momentum_logger.info("⚙️ 处理订单数据...")
        for order in orders_data:
            # 检查是否有商品类别信息
            category = order.get('category', '')
            if not category:
                # 尝试从其他可能的字段名获取类别信息
                category = (order.get('product_category_name', '') or 
                           order.get('productCategoryName', '') or 
                           order.get('categoryName', '') or 
                           order.get('productCategory', '') or 
                           '')
                if not category:
                    category_missing_count += 1
            
            # 检查是否有价格信息
            price = order.get('price', 0)
            if not price:
                price = (order.get('product_price', 0) or 
                        order.get('productPrice', 0) or 
                        order.get('real_amount', 0) or
                        order.get('realAmount', 0) or
                        0)
                if not price:
                    price_missing_count += 1
            
            # 检查是否有评分信息
            rating = order.get('rating', 0)
            if not rating:
                rating = (order.get('star', 0) or 
                         order.get('score', 0) or
                         order.get('product_rating', 0) or
                         order.get('productRating', 0) or
                         0)
                if not rating:
                    rating_missing_count += 1
            
            weighted_data.append({
                'category': category,
                'price': price,
                'rating': rating,
                'weight': self.behavior_weights['purchase']
            })
        
        # 处理购物车数据
        logger.info("⚙️ 处理购物车数据...")
        momentum_logger.info("⚙️ 处理购物车数据...")
        for cart_item in cart_data:
            # 检查是否有商品类别信息
            category = cart_item.get('category', '')
            if not category:
                # 尝试从其他可能的字段名获取类别信息
                category = (cart_item.get('product_category_name', '') or 
                           cart_item.get('productCategoryName', '') or 
                           cart_item.get('categoryName', '') or 
                           cart_item.get('productCategory', '') or 
                           '')
                if not category:
                    category_missing_count += 1
            
            # 检查是否有价格信息
            price = cart_item.get('price', 0)
            if not price:
                price = (cart_item.get('product_price', 0) or 
                        cart_item.get('productPrice', 0) or 
                        0)
                if not price:
                    price_missing_count += 1
            
            weighted_data.append({
                'category': category,
                'price': price,
                'rating': cart_item.get('rating', 0),
                'weight': self.behavior_weights['cart']
            })
        
        # 处理退货数据
        logger.info("⚙️ 处理退货数据...")
        momentum_logger.info("⚙️ 处理退货数据...")
        for return_item in returns_data:
            # 检查是否有商品类别信息
            category = return_item.get('category', '')
            if not category:
                # 尝试从其他可能的字段名获取类别信息
                category = (return_item.get('product_category_name', '') or 
                           return_item.get('productCategoryName', '') or 
                           return_item.get('categoryName', '') or 
                           return_item.get('productCategory', '') or 
                           '')
                if not category:
                    category_missing_count += 1
            
            # 检查是否有价格信息
            price = return_item.get('price', 0)
            if not price:
                price = (return_item.get('product_price', 0) or 
                        return_item.get('productPrice', 0) or 
                        return_item.get('product_real_price', 0) or
                        return_item.get('productRealPrice', 0) or
                        0)
                if not price:
                    price_missing_count += 1
            
            weighted_data.append({
                'category': category,
                'price': price,
                'rating': return_item.get('rating', 0),
                'weight': self.behavior_weights['return']
            })
        
        # 记录数据质量统计
        logger.info(f"📊 数据质量统计: 缺失类别={category_missing_count}, 缺失价格={price_missing_count}, 缺失评分={rating_missing_count}")
        logger.info(f"📊 加权数据总数: {len(weighted_data)}")
        momentum_logger.info(f"📊 数据质量统计: 缺失类别={category_missing_count}, 缺失价格={price_missing_count}, 缺失评分={rating_missing_count}")
        momentum_logger.info(f"📊 加权数据总数: {len(weighted_data)}")
        
        # 如果没有有效数据，返回默认动量
        if not weighted_data:
            logger.warning("⚠️ 没有有效的行为数据，使用默认长期动量")
            momentum_logger.warning("⚠️ 没有有效的行为数据，使用默认长期动量")
            return self.get_default_long_term_momentum()
        
        # 2. 计算三大偏好模型
        logger.info("⚙️ 计算类别偏好模型...")
        momentum_logger.info("⚙️ 计算类别偏好模型...")
        
        # 2.1 类型偏好：每个类型独立赋权
        category_weights = {}
        valid_category_count = 0
        
        for item in weighted_data:
            category = item['category']
            weight = item['weight']
            
            # 跳过空类别
            if not category:
                continue
                
            category_weights[category] = category_weights.get(category, 0) + weight
            valid_category_count += 1
        
        # 记录有效类别数据统计
        logger.info(f"📊 有效类别数据: {valid_category_count}/{len(weighted_data)} ({valid_category_count/len(weighted_data)*100:.1f}%)")
        logger.info(f"📊 不同类别数量: {len(category_weights)}")
        momentum_logger.info(f"📊 有效类别数据: {valid_category_count}/{len(weighted_data)} ({valid_category_count/len(weighted_data)*100:.1f}%)")
        momentum_logger.info(f"📊 不同类别数量: {len(category_weights)}")
        
        # 如果没有有效类别数据，使用默认类别偏好
        if not category_weights:
            logger.warning("⚠️ 没有有效的类别数据，使用默认类别偏好")
            momentum_logger.warning("⚠️ 没有有效的类别数据，使用默认类别偏好")
            category_preferences = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
            
            # 确保默认类别偏好不为空
            if not category_preferences:
                logger.error("❌ 严重错误: 默认类别偏好为空，请检查config.py文件")
                momentum_logger.error("❌ 严重错误: 默认类别偏好为空，请检查config.py文件")
                # 创建一个简单的默认类别偏好
                category_preferences = {
                    '手机通讯': 0.15,
                    '笔记本': 0.15,
                    '家用电器': 0.1,
                    '服装': 0.1,
                    '休闲裤': 0.05,
                    'T恤': 0.05,
                    '洗衣机': 0.05,
                    '冰箱': 0.05,
                    '空调': 0.05,
                    '厨房小电': 0.05,
                    '食品': 0.05,
                    '图书': 0.05,
                    '美妆': 0.05,
                    '家居': 0.05,
                }
            
            # 记录使用的默认类别偏好
            logger.info("📊 使用默认类别偏好:")
            momentum_logger.info("📊 使用默认类别偏好:")
            top_categories = sorted(category_preferences.items(), key=lambda x: x[1], reverse=True)[:5]
            for i, (cat, weight) in enumerate(top_categories):
                logger.info(f"   {i+1}. {cat}: {weight:.4f} ({weight*100:.1f}%)")
                momentum_logger.info(f"   {i+1}. {cat}: {weight:.4f} ({weight*100:.1f}%)")
        else:
            # 使用Softmax归一化类型权重
            categories = list(category_weights.keys())
            weights = list(category_weights.values())
            normalized_category_weights = self.softmax(weights, self.softmax_temperature)
            category_preferences = dict(zip(categories, normalized_category_weights))
            
            # 记录类别偏好详情
            logger.info("📊 计算得到的类别偏好:")
            momentum_logger.info("📊 计算得到的类别偏好:")
            top_categories = sorted(category_preferences.items(), key=lambda x: x[1], reverse=True)[:5]
            for i, (cat, weight) in enumerate(top_categories):
                logger.info(f"   {i+1}. {cat}: {weight:.4f} ({weight*100:.1f}%)")
                momentum_logger.info(f"   {i+1}. {cat}: {weight:.4f} ({weight*100:.1f}%)")
        
        # 2.2 价格偏好模型：分析购买成功商品价格分布
        logger.info("⚙️ 计算价格偏好模型...")
        momentum_logger.info("⚙️ 计算价格偏好模型...")
        positive_weight_data = [item for item in weighted_data if item['weight'] > 0]
        valid_price_count = sum(1 for item in positive_weight_data if item['price'] > 0)
        
        logger.info(f"📊 有效价格数据: {valid_price_count}/{len(positive_weight_data)} ({valid_price_count/len(positive_weight_data)*100:.1f}% 如果有正向权重数据)")
        
        if positive_weight_data and valid_price_count > 0:
            weighted_prices = []
            for item in positive_weight_data:
                if item['price'] <= 0:
                    continue
                    
                repeat_count = max(1, int(item['weight']))
                weighted_prices.extend([item['price']] * repeat_count)
            
            logger.info(f"📊 价格数据点数量: {len(weighted_prices)}")
            
            if weighted_prices:
                price_model = self.calculate_biased_normal_params(weighted_prices)
                logger.info(f"📊 价格偏好模型: 均值={price_model['mean']:.2f}, 标准差={price_model['std']:.2f}")
            else:
                logger.warning("⚠️ 没有有效的价格数据，使用默认价格模型")
                price_model = RECOMMENDATION_CONFIG['default_price_model'].copy()
        else:
            logger.warning("⚠️ 没有有效的价格数据，使用默认价格模型")
            price_model = RECOMMENDATION_CONFIG['default_price_model'].copy()
        
        # 2.3 质量偏好模型：基于商品评价分数
        logger.info("⚙️ 计算质量偏好模型...")
        valid_rating_count = sum(1 for item in positive_weight_data if item['rating'] > 0)
        
        logger.info(f"📊 有效评分数据: {valid_rating_count}/{len(positive_weight_data)} ({valid_rating_count/len(positive_weight_data)*100:.1f}% 如果有正向权重数据)")
        
        if positive_weight_data and valid_rating_count > 0:
            weighted_ratings = []
            for item in positive_weight_data:
                if item['rating'] <= 0:
                    continue
                    
                repeat_count = max(1, int(item['weight']))
                weighted_ratings.extend([item['rating']] * repeat_count)
            
            logger.info(f"📊 评分数据点数量: {len(weighted_ratings)}")
            
            if weighted_ratings:
                quality_model = self.calculate_biased_normal_params(weighted_ratings)
                logger.info(f"📊 质量偏好模型: 均值={quality_model['mean']:.2f}, 标准差={quality_model['std']:.2f}")
            else:
                logger.warning("⚠️ 没有有效的评分数据，使用默认质量模型")
                quality_model = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        else:
            logger.warning("⚠️ 没有有效的评分数据，使用默认质量模型")
            quality_model = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        
        # 构建最终动量模型
        result = {
            'type': 'long_term',
            'categoryPreferences': {},  # 先创建空字典，然后单独填充
            'priceModel': price_model,
            'qualityModel': quality_model,
            'lastUpdated': datetime.now().isoformat(),
            'dataSource': 'user_behavior_analysis'
        }
        
        # 单独填充类别偏好，确保没有嵌套对象
        for category, weight in category_preferences.items():
            if isinstance(category, str) and isinstance(weight, (int, float, np.number)):
                result['categoryPreferences'][category] = float(weight)  # 确保是原生Python浮点数
        
        # 记录最终动量摘要
        logger.info("✅ 长期动量计算完成")
        logger.info(f"📊 类别偏好数量: {len(result['categoryPreferences'])}")
        logger.info(f"📊 价格模型数据点: {price_model.get('count', 0)}")
        logger.info(f"📊 质量模型数据点: {quality_model.get('count', 0)}")
        
        momentum_logger.info("✅ 长期动量计算完成")
        momentum_logger.info(f"📊 类别偏好数量: {len(result['categoryPreferences'])}")
        momentum_logger.info(f"📊 价格模型数据点: {price_model.get('count', 0)}")
        momentum_logger.info(f"📊 质量模型数据点: {quality_model.get('count', 0)}")
        momentum_logger.info("="*50)
        
        return result
    
    def get_default_long_term_momentum(self) -> Dict[str, Any]:
        """
        获取默认长期动量配置
        当用户没有历史行为数据时使用config.py中的默认值
        注意: 请确保config.py中的类别名称与数据库中的实际类别名称一致
        """
        # 创建基本结构
        result = {
            'type': 'long_term',
            'categoryPreferences': {},
            'priceModel': RECOMMENDATION_CONFIG['default_price_model'].copy(),
            'qualityModel': RECOMMENDATION_CONFIG['default_quality_model'].copy(),
            'lastUpdated': datetime.now().isoformat(),
            'isDefault': True,
            'dataSource': 'config_default'
        }
        
        # 单独填充类别偏好，确保没有嵌套对象
        for category, weight in RECOMMENDATION_CONFIG['default_category_preferences'].items():
            if isinstance(category, str) and isinstance(weight, (int, float, np.number)):
                result['categoryPreferences'][category] = float(weight)  # 确保是原生Python浮点数
                
        return result
    
    def compute_short_term_momentum(self, clicked_products: List[Dict], click_strengths: Optional[List[float]] = None) -> Dict[str, Any]:
        """
        计算短期动量（即时偏好变化）
        本质上是用户偏好分布，表示用户即时兴趣变化的方向和强度
        支持单次点击或多次点击的累积短期偏好计算
        
        Args:
            clicked_products: 点击的商品列表（可以是单个或多个）
            click_strengths: 对应的点击强度列表（可选）
        Returns:
            与长期动量结构一致的短期偏好分布
        """
        # 处理单个商品的情况（向后兼容）
        if isinstance(clicked_products, dict):
            clicked_products = [clicked_products]
        
        if click_strengths is None:
            click_strengths = [1.0] * len(clicked_products)
        elif len(click_strengths) != len(clicked_products):
            click_strengths = [1.0] * len(clicked_products)
        
        if not clicked_products:
            logger.info("⚠️ 没有提供点击商品，返回空短期动量")
            return self._get_empty_short_term_momentum()
        
        logger.debug(f"🔍 短期动量计算 - 输入: {len(clicked_products)} 个商品点击, 总强度: {sum(click_strengths):.2f}")
        
        # 记录点击商品的详细信息
        for i, (product, strength) in enumerate(zip(clicked_products, click_strengths)):
            # 处理字段名兼容性
            product_id = product.get('productId', product.get('product_id', 'unknown'))
            category = product.get('category', 'unknown')
            price = product.get('price', 0)
            rating = product.get('rating', 0)
            logger.debug(f"📌 点击商品[{i+1}]: ID={product_id}, 类别={category}, 价格={price}, 评分={rating}, 强度={strength:.2f}")
        
        # 1. 计算短期类别偏好分布
        category_weights = {}
        for product, strength in zip(clicked_products, click_strengths):
            category = product.get('category', '')
            if category:
                category_weights[category] = category_weights.get(category, 0) + strength
        
        # 使用Softmax归一化类别权重
        if category_weights:
            categories = list(category_weights.keys())
            weights = list(category_weights.values())
            normalized_weights = self.softmax(weights, self.softmax_temperature)
            category_preferences = dict(zip(categories, normalized_weights))
            
            # 记录类别偏好详情
            logger.info(f"📊 短期类别偏好: {len(category_preferences)} 个类别")
            top_categories = sorted(category_preferences.items(), key=lambda x: x[1], reverse=True)[:3]
            for category, weight in top_categories:
                logger.info(f"   - {category}: {weight:.4f} ({weight*100:.1f}%)")
        else:
            category_preferences = {}
            logger.info("⚠️ 没有有效的类别偏好")
        
        # 2. 计算短期价格偏好分布
        weighted_prices = []
        for product, strength in zip(clicked_products, click_strengths):
            price = product.get('price', 0)
            if price > 0:
                # 根据点击强度重复价格数据点
                repeat_count = max(1, int(strength * 2))  # 短期动量的权重放大
                weighted_prices.extend([price] * repeat_count)
        
        price_model = self.calculate_biased_normal_params(weighted_prices)
        logger.info(f"💰 短期价格偏好: 均值={price_model.get('mean', 0):.2f}, 标准差={price_model.get('std', 0):.2f}")
        
        # 3. 计算短期质量偏好分布
        weighted_ratings = []
        for product, strength in zip(clicked_products, click_strengths):
            rating = product.get('rating', 0)
            if rating > 0:
                repeat_count = max(1, int(strength * 2))
                weighted_ratings.extend([rating] * repeat_count)
        
        quality_model = self.calculate_biased_normal_params(weighted_ratings)
        logger.info(f"⭐ 短期质量偏好: 均值={quality_model.get('mean', 0):.2f}, 标准差={quality_model.get('std', 0):.2f}")
        
        # 创建基本结构
        result = {
            'type': 'short_term',
            'categoryPreferences': {},  # 先创建空字典，然后单独填充
            'priceModel': price_model,
            'qualityModel': quality_model,
            'totalClicks': len(clicked_products),
            'totalStrength': sum(click_strengths),
            'clickedCategories': list(set(p.get('category', '') for p in clicked_products if p.get('category'))),
            'timestamp': datetime.now().isoformat(),
            'dataSource': 'user_clicks_aggregated'
        }
        
        # 单独填充类别偏好，确保没有嵌套对象
        for category, weight in category_preferences.items():
            if isinstance(category, str) and isinstance(weight, (int, float, np.number)):
                result['categoryPreferences'][category] = float(weight)  # 确保是原生Python浮点数
        
        logger.info(f"✅ 短期动量计算完成: {len(clicked_products)}个点击, 总强度={sum(click_strengths):.2f}")
        return result
    
    def compute_short_term_momentum_single(self, clicked_product: Dict, click_strength: float = 1.0) -> Dict[str, Any]:
        """
        向后兼容方法：计算单个商品点击的短期动量
        内部调用新的多点击方法
        
        Args:
            clicked_product: 单个点击的商品
            click_strength: 点击强度
        Returns:
            短期动量（与其他动量结构一致）
        """
        return self.compute_short_term_momentum([clicked_product], [click_strength])
    
    def _get_empty_short_term_momentum(self) -> Dict[str, Any]:
        """获取空的短期动量结构"""
        return {
            'type': 'short_term',
            'categoryPreferences': {},
            'category_preferences': {},  # 兼容旧版字段名
            'priceModel': self.calculate_biased_normal_params([]),
            'price_model': self.calculate_biased_normal_params([]),  # 兼容旧版字段名
            'qualityModel': self.calculate_biased_normal_params([]),
            'quality_model': self.calculate_biased_normal_params([]),  # 兼容旧版字段名
            'totalClicks': 0,
            'total_clicks': 0,  # 兼容旧版字段名
            'totalStrength': 0.0,
            'total_strength': 0.0,  # 兼容旧版字段名
            'clickedCategories': [],
            'clicked_categories': [],  # 兼容旧版字段名
            'timestamp': datetime.now().isoformat(),
            'dataSource': 'empty_short_term',
            'data_source': 'empty_short_term'  # 兼容旧版字段名
        }
    
    def compute_push_momentum(self, long_term_momentum: Dict, short_term_momentum: Dict, 
                            current_push_momentum: Optional[Dict] = None) -> Dict[str, Any]:
        """
        计算推送动量 - 用户偏好分布的累积状态
        
        核心公式：推送动量 += 长期动量 + 短期动量
        """
        logger.info("🔄 开始计算推送动量...")
        
        # 获取当前推送动量作为基础
        if current_push_momentum and current_push_momentum.get('type') == 'push_momentum':
            base_push_momentum = current_push_momentum
            logger.info("📊 使用现有推送动量作为基础")
            logger.info(f"   - 累积点击: {base_push_momentum.get('cumulative_clicks', 0)}")
            
            # 记录现有推送动量的主要特征
            base_categories = base_push_momentum.get('category_preferences', {})
            if base_categories:
                top_cats = sorted(base_categories.items(), key=lambda x: x[1], reverse=True)[:2]
                cat_str = ", ".join([f"{c}: {w:.3f}" for c, w in top_cats])
                logger.info(f"   - 主要类别偏好: {cat_str}")
            
            base_price_model = base_push_momentum.get('price_model', {})
            logger.info(f"   - 价格偏好: 均值={base_price_model.get('mean', 0):.2f}, 标准差={base_price_model.get('std', 0):.2f}")
        else:
            # 从长期动量初始化
            base_push_momentum = {
                'type': 'push_momentum',
                'categoryPreferences': long_term_momentum.get('categoryPreferences', {}).copy() or long_term_momentum.get('category_preferences', {}).copy(),
                'category_preferences': long_term_momentum.get('categoryPreferences', {}).copy() or long_term_momentum.get('category_preferences', {}).copy(),  # 兼容旧版字段名
                'priceModel': long_term_momentum.get('priceModel', {}).copy() or long_term_momentum.get('price_model', {}).copy(),
                'price_model': long_term_momentum.get('priceModel', {}).copy() or long_term_momentum.get('price_model', {}).copy(),  # 兼容旧版字段名
                'qualityModel': long_term_momentum.get('qualityModel', {}).copy() or long_term_momentum.get('quality_model', {}).copy(),
                'quality_model': long_term_momentum.get('qualityModel', {}).copy() or long_term_momentum.get('quality_model', {}).copy(),  # 兼容旧版字段名
                'cumulativeClicks': 0,
                'cumulative_clicks': 0,  # 兼容旧版字段名
                'lastUpdated': datetime.now().isoformat(),
                'last_updated': datetime.now().isoformat(),  # 兼容旧版字段名
                'dataSource': 'initialized_from_long_term',
                'data_source': 'initialized_from_long_term'  # 兼容旧版字段名
            }
            logger.info("📊 从长期动量初始化新的推送动量")
        
        # 获取短期动量数据
        short_term_category_prefs = short_term_momentum.get('categoryPreferences', {}) or short_term_momentum.get('category_preferences', {})
        short_term_price_model = short_term_momentum.get('priceModel', {}) or short_term_momentum.get('price_model', {})
        short_term_quality_model = short_term_momentum.get('qualityModel', {}) or short_term_momentum.get('quality_model', {})
        total_clicks = short_term_momentum.get('totalClicks', 1) or short_term_momentum.get('total_clicks', 1)
        total_strength = short_term_momentum.get('totalStrength', 1.0) or short_term_momentum.get('total_strength', 1.0)
        
        # 记录短期动量的影响
        logger.info(f"📈 短期动量影响: {total_clicks}个点击, 总强度={total_strength:.2f}")
        if short_term_category_prefs:
            top_cats = sorted(short_term_category_prefs.items(), key=lambda x: x[1], reverse=True)[:2]
            cat_str = ", ".join([f"{c}: {w:.3f}" for c, w in top_cats])
            logger.info(f"   - 短期类别偏好: {cat_str}")
        
        if short_term_price_model:
            logger.info(f"   - 短期价格偏好: 均值={short_term_price_model.get('mean', 0):.2f}, 标准差={short_term_price_model.get('std', 0):.2f}")
        
        # 复制当前推送动量
        new_push_momentum = {
            'type': 'push_momentum',
            'categoryPreferences': base_push_momentum.get('categoryPreferences', {}).copy() or base_push_momentum.get('category_preferences', {}).copy(),
            'category_preferences': base_push_momentum.get('categoryPreferences', {}).copy() or base_push_momentum.get('category_preferences', {}).copy(),  # 兼容旧版字段名
            'priceModel': base_push_momentum.get('priceModel', {}).copy() or base_push_momentum.get('price_model', {}).copy(),
            'price_model': base_push_momentum.get('priceModel', {}).copy() or base_push_momentum.get('price_model', {}).copy(),  # 兼容旧版字段名
            'qualityModel': base_push_momentum.get('qualityModel', {}).copy() or base_push_momentum.get('quality_model', {}).copy(),
            'quality_model': base_push_momentum.get('qualityModel', {}).copy() or base_push_momentum.get('quality_model', {}).copy(),  # 兼容旧版字段名
            'cumulativeClicks': base_push_momentum.get('cumulativeClicks', 0) + total_clicks or base_push_momentum.get('cumulative_clicks', 0) + total_clicks,
            'cumulative_clicks': base_push_momentum.get('cumulativeClicks', 0) + total_clicks or base_push_momentum.get('cumulative_clicks', 0) + total_clicks,  # 兼容旧版字段名
            'lastUpdated': datetime.now().isoformat(),
            'last_updated': datetime.now().isoformat(),  # 兼容旧版字段名
            'dataSource': 'cumulative_push_momentum',
            'data_source': 'cumulative_push_momentum'  # 兼容旧版字段名
        }
        
        logger.info(f"⚙️ 累积点击数: {new_push_momentum['cumulative_clicks']}")
        
        # 1. 累积更新类型偏好分布（基于短期动量的偏好分布）
        if short_term_category_prefs:
            current_prefs = new_push_momentum['category_preferences']
            
            for category, short_term_weight in short_term_category_prefs.items():
                # 获取长期偏好分布中的权重
                long_term_weight = long_term_momentum.get('category_preferences', {}).get(category, 0.1)
                
                # 累积更新偏好分布
                current_weight = current_prefs.get(category, 0.0)
                boost_factor = self.push_momentum_config['category_boost_factor']
                
                # 累积更新公式，考虑短期动量的强度
                long_term_influence = long_term_weight * 0.1
                short_term_influence = short_term_weight * total_strength * boost_factor * 0.2
                
                new_weight = current_weight + long_term_influence + short_term_influence
                current_prefs[category] = min(new_weight, 1.0)
            
            # 重新归一化偏好分布
            if current_prefs and sum(current_prefs.values()) > 0:
                categories = list(current_prefs.keys())
                weights = list(current_prefs.values())
                normalized_weights = self.softmax(weights, self.softmax_temperature)
                new_push_momentum['category_preferences'] = dict(zip(categories, normalized_weights))
        
        # 2. 累积更新价格偏好分布（基于短期动量的价格分布）
        if short_term_price_model.get('count', 0) > 0:
            current_price_model = new_push_momentum['price_model']
            long_term_price_model = long_term_momentum.get('price_model', {})
            
            current_mean = current_price_model.get('mean', 100.0)
            current_std = current_price_model.get('std', 50.0)
            long_term_mean = long_term_price_model.get('mean', 100.0)
            short_term_mean = short_term_price_model.get('mean', current_mean)
            
            # 使用累积的点击数据进行卡方分布拟合
            cumulative_clicks = new_push_momentum.get('cumulative_clicks', 1)
            
            # 构建价格数据点：基于长期动量的价格分布 + 当前累积的推送动量变化
            price_data_points = []
            
            # 基于短期动量的价格分布生成数据点
            short_term_count = short_term_price_model.get('count', 0)
            if short_term_count > 0:
                short_term_std = short_term_price_model.get('std', 1.0)
                # 生成符合短期分布的数据点
                np.random.seed(42)
                price_data_points = np.random.normal(short_term_mean, short_term_std, short_term_count).tolist()
                price_data_points = [max(p, 1.0) for p in price_data_points]
            
            # 从长期偏好和当前偏好的差异推断用户行为趋势
            if cumulative_clicks > 1 and current_mean != long_term_mean:
                # 生成反映用户偏好变化趋势的数据点
                for i in range(min(cumulative_clicks - 1, 10)):
                    progress = (i + 1) / cumulative_clicks
                    interpolated_mean = long_term_mean + (current_mean - long_term_mean) * progress
                    
                    np.random.seed(42 + i)
                    noise = np.random.normal(0, current_std * 0.1)
                    trend_point = max(1.0, interpolated_mean + noise)
                    price_data_points.append(trend_point)
            
            # 如果数据点足够，使用卡方分布拟合
            if len(price_data_points) >= 3:
                chi_fit_result = self._fit_chi_square_distribution(price_data_points)
                expansion_factor = chi_fit_result['expansion_factor']
                
                # 传统的动量更新，基于分布差异
                long_term_pull = (long_term_mean - current_mean) * 0.05
                short_term_push = (short_term_mean - current_mean) * total_strength * 0.1
                
                # 根据卡方分布拟合结果调整更新强度
                adjustment_strength = expansion_factor * total_strength
                new_mean = current_mean + long_term_pull + short_term_push * adjustment_strength
                
                # 标准差调整
                std_expansion = total_strength * 0.05 * self.push_momentum_config['price_std_multiplier']
                std_expansion *= expansion_factor
                new_std = current_std * (1 + std_expansion)
                
                current_price_model['_chi_fit_info'] = {
                    'method': 'short_term_distribution_chi_fitting',
                    'goodness_of_fit': chi_fit_result['goodness_of_fit'],
                    'expansion_factor': chi_fit_result['expansion_factor'],
                    'data_points': len(price_data_points),
                    'cumulative_clicks': cumulative_clicks
                }
            else:
                # 数据不足时使用传统更新
                long_term_pull = (long_term_mean - current_mean) * 0.05
                short_term_push = (short_term_mean - current_mean) * total_strength * 0.1
                new_mean = current_mean + long_term_pull + short_term_push
                
                std_expansion = total_strength * 0.05 * self.push_momentum_config['price_std_multiplier']
                new_std = current_std * (1 + std_expansion)
                
                current_price_model['_chi_fit_info'] = {
                    'method': 'distribution_based_traditional_update',
                    'reason': 'insufficient_trend_data',
                    'data_points': len(price_data_points)
                }
            
            current_price_model['mean'] = new_mean
            current_price_model['std'] = new_std
        
        # 3. 累积更新质量偏好分布（基于短期动量的质量分布）
        if short_term_quality_model.get('count', 0) > 0:
            current_quality_model = new_push_momentum['quality_model']
            long_term_quality_model = long_term_momentum.get('quality_model', {})
            
            current_mean = current_quality_model.get('mean', 4.0)
            current_std = current_quality_model.get('std', 1.0)
            long_term_mean = long_term_quality_model.get('mean', 4.0)
            short_term_mean = short_term_quality_model.get('mean', current_mean)
            
            # 使用累积的点击数据进行卡方分布拟合
            cumulative_clicks = new_push_momentum.get('cumulative_clicks', 1)
            
            # 构建质量数据点：基于短期动量的质量分布
            quality_data_points = []
            
            # 基于短期动量的质量分布生成数据点
            short_term_count = short_term_quality_model.get('count', 0)
            if short_term_count > 0:
                short_term_std = short_term_quality_model.get('std', 0.5)
                # 生成符合短期分布的数据点
                np.random.seed(42)
                quality_data_points = np.random.normal(short_term_mean, short_term_std, short_term_count).tolist()
                quality_data_points = [max(1.0, min(5.0, q)) for q in quality_data_points]
            
            # 从长期偏好和当前偏好的差异推断用户行为趋势
            if cumulative_clicks > 1 and current_mean != long_term_mean:
                # 生成反映用户偏好变化趋势的数据点
                for i in range(min(cumulative_clicks - 1, 8)):
                    progress = (i + 1) / cumulative_clicks
                    interpolated_mean = long_term_mean + (current_mean - long_term_mean) * progress
                    
                    np.random.seed(42 + i)
                    noise = np.random.normal(0, current_std * 0.1)
                    trend_point = max(1.0, min(5.0, interpolated_mean + noise))
                    quality_data_points.append(trend_point)
            
            # 如果数据点足够，使用卡方分布拟合
            if len(quality_data_points) >= 3:
                chi_fit_result = self._fit_chi_square_distribution(quality_data_points)
                expansion_factor = chi_fit_result['expansion_factor']
                
                # 传统的动量更新，基于分布差异
                long_term_pull = (long_term_mean - current_mean) * 0.03
                short_term_push = (short_term_mean - current_mean) * total_strength * 0.05
                
                # 根据卡方分布拟合结果调整更新强度
                adjustment_strength = expansion_factor * total_strength
                new_mean = max(1.0, min(5.0, current_mean + long_term_pull + short_term_push * adjustment_strength))
                
                # 标准差调整
                std_expansion = total_strength * 0.02 * self.push_momentum_config['quality_std_multiplier']
                std_expansion *= expansion_factor
                new_std = current_std * (1 + std_expansion)
                
                current_quality_model['_chi_fit_info'] = {
                    'method': 'short_term_distribution_chi_fitting',
                    'goodness_of_fit': chi_fit_result['goodness_of_fit'],
                    'expansion_factor': chi_fit_result['expansion_factor'],
                    'data_points': len(quality_data_points),
                    'cumulative_clicks': cumulative_clicks
                }
            else:
                # 数据不足时使用传统更新
                long_term_pull = (long_term_mean - current_mean) * 0.03
                short_term_push = (short_term_mean - current_mean) * total_strength * 0.05
                new_mean = max(1.0, min(5.0, current_mean + long_term_pull + short_term_push))
                
                std_expansion = total_strength * 0.02 * self.push_momentum_config['quality_std_multiplier']
                new_std = current_std * (1 + std_expansion)
                
                current_quality_model['_chi_fit_info'] = {
                    'method': 'distribution_based_traditional_update',
                    'reason': 'insufficient_trend_data',
                    'data_points': len(quality_data_points)
                }
            
            current_quality_model['mean'] = new_mean
            current_quality_model['std'] = new_std
        
        # 记录本次更新信息
        new_push_momentum['latest_update'] = {
            'long_term_base': long_term_momentum.get('type', 'unknown'),
            'short_term_influence': short_term_momentum,
            'update_timestamp': datetime.now().isoformat(),
            'note': '推送动量本质上仍是用户偏好分布，与长期短期动量是同类数据结构'
        }
        
        # 记录最终推送动量的详细状态
        logger.info("✅ 推送动量计算完成")
        
        # 记录最终的类别偏好
        final_categories = new_push_momentum.get('category_preferences', {})
        if final_categories:
            logger.info("📊 最终类别偏好分布:")
            top_categories = sorted(final_categories.items(), key=lambda x: x[1], reverse=True)[:5]
            for i, (category, weight) in enumerate(top_categories):
                logger.info(f"   {i+1}. {category}: {weight:.4f} ({weight*100:.1f}%)")
        
        # 记录价格偏好变化
        final_price_model = new_push_momentum.get('price_model', {})
        original_price_model = base_push_momentum.get('price_model', {})
        price_mean_change = final_price_model.get('mean', 0) - original_price_model.get('mean', 0)
        logger.info(f"💰 价格偏好变化: {original_price_model.get('mean', 0):.2f} → {final_price_model.get('mean', 0):.2f} (变化: {price_mean_change:+.2f})")
        
        # 记录质量偏好变化
        final_quality_model = new_push_momentum.get('quality_model', {})
        original_quality_model = base_push_momentum.get('quality_model', {})
        quality_mean_change = final_quality_model.get('mean', 0) - original_quality_model.get('mean', 0)
        logger.info(f"⭐ 质量偏好变化: {original_quality_model.get('mean', 0):.2f} → {final_quality_model.get('mean', 0):.2f} (变化: {quality_mean_change:+.2f})")
        
        # 记录推送动量摘要
        momentum_summary = self.get_momentum_summary(new_push_momentum)
        top_categories_str = ", ".join([f"{cat}: {weight:.3f}" for cat, weight in momentum_summary.get('top_categories', [])])
        logger.info(f"📝 推送动量摘要: 类型={momentum_summary.get('type')}, 主要类别=[{top_categories_str}]")
        logger.info(f"   价格偏好: {momentum_summary.get('price_preference', {}).get('range')}")
        logger.info(f"   质量偏好: {momentum_summary.get('quality_preference', {}).get('preferred_range')}")
        
        return new_push_momentum
    
    def calculate_product_score(self, product: Dict, momentum: Dict) -> float:
        """
        基于动量计算商品得分
        Args:
            product: 商品信息
            momentum: 用户动量
        Returns:
            商品推荐得分
        """
        # 处理字段名兼容性
        product_id = product.get('productId', product.get('product_id', 'unknown'))
        category = product.get('category', '')
        price = product.get('price', 0)
        rating = product.get('rating', 0)
        stock = product.get('stock', 100)
        is_new = product.get('isNew', product.get('is_new', False))
        
        # 根据配置决定是否记录详细的评分过程
        if self.detailed_scoring:
            logger.info(f"🔢 计算商品得分 - ID={product_id}, 类别={category}, 价格={price}, 评分={rating}, 库存={stock}, 新品={is_new}")
        
        # 处理字段名兼容性问题
        # 1. 类型得分 - 检查两种可能的字段名
        category_prefs = momentum.get('categoryPreferences', {}) or momentum.get('category_preferences', {})
        
        # 检查类别偏好是否为空，如果为空，可能是算法问题
        if not category_prefs:
            logger.warning(f"⚠️ 警告: 动量中的类别偏好为空，这可能导致所有商品得分相同!")
            logger.warning(f"⚠️ 动量数据字段: {list(momentum.keys())}")
            category_score = 0.05
            
            # 尝试使用默认类别偏好
            category_prefs = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
        else:
            category_score = category_prefs.get(category, 0.05)
            # 检查所有类别权重是否相同
            unique_weights = set(category_prefs.values())
            if len(unique_weights) == 1:
                logger.warning(f"⚠️ 警告: 所有类别权重都相同 ({list(unique_weights)[0]})，这可能导致得分问题!")
        
        # 记录类别偏好详情
        if self.detailed_scoring:
            top_categories = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)[:3]
            top_categories_str = ", ".join([f"{c}: {w:.3f}" for c, w in top_categories])
            logger.info(f"   - 类别得分: {category_score:.4f} (类别={category})")
            logger.info(f"   - 用户类别偏好Top-3: {top_categories_str}")
        
        # 添加随机因子以避免完全相同的得分 (0.98-1.02的随机数)
        import random
        random_factor = 0.98 + random.random() * 0.04
        category_score = category_score * random_factor
        
        if self.detailed_scoring:
            logger.info(f"   - 应用随机因子: {random_factor:.4f}, 调整后类别得分: {category_score:.4f}")
        
        # 2. 价格得分 - 检查两种可能的字段名
        price_model = momentum.get('priceModel', {}) or momentum.get('price_model', {})
        
        if price > 0 and price_model.get('count', 0) > 0:
            mean = price_model.get('mean', 100.0)
            std = price_model.get('std', 50.0)
            
            # 检查标准差是否接近0，这可能导致所有商品得分相同
            if std < 0.001:
                logger.warning(f"⚠️ 警告: 价格偏好的标准差接近0 ({std:.6f})，这可能导致得分问题!")
                # 使用默认标准差
                std = 50.0
                if self.detailed_scoring:
                    logger.info(f"   - 使用默认标准差: {std}")
            
            # 使用正态分布的概率密度
            max_pdf = stats.norm.pdf(mean, mean, std)
            current_pdf = stats.norm.pdf(price, mean, std)
            price_score = current_pdf / max_pdf if max_pdf > 0 else 0.1
            
            # 对于极端价格给予惩罚
            z_score = abs(price - mean) / std if std > 0 else 0
            
            # 记录价格评分详情
            if self.detailed_scoring:
                logger.info(f"   - 价格分布: 用户偏好均值={mean:.2f}, 标准差={std:.2f}")
                logger.info(f"   - 价格得分计算: max_pdf={max_pdf:.6f}, current_pdf={current_pdf:.6f}")
            
            if z_score > 3:
                original_price_score = price_score
                price_score *= 0.1
                if self.detailed_scoring:
                    logger.info(f"   - 价格得分(极端惩罚): {original_price_score:.4f} → {price_score:.4f} (Z值={z_score:.2f} > 3, 惩罚系数=0.1)")
            elif self.detailed_scoring:
                logger.info(f"   - 价格得分: {price_score:.4f} (价格={price}, Z值={z_score:.2f})")
        else:
            price_score = 0.5
            if self.detailed_scoring:
                logger.info(f"   - 价格得分: {price_score:.4f} (默认值, 无有效价格模型)")
        
        # 添加随机因子以避免完全相同的得分 (0.97-1.03的随机数)
        random_factor = 0.97 + random.random() * 0.06
        price_score = price_score * random_factor
        
        if self.detailed_scoring:
            logger.info(f"   - 应用随机因子: {random_factor:.4f}, 调整后价格得分: {price_score:.4f}")
        
        # 3. 质量得分 - 检查两种可能的字段名
        quality_model = momentum.get('qualityModel', {}) or momentum.get('quality_model', {})
        
        if rating > 0 and quality_model.get('count', 0) > 0:
            mean = quality_model.get('mean', 4.0)
            std = quality_model.get('std', 1.0)
            
            # 检查标准差是否接近0，这可能导致所有商品得分相同
            if std < 0.001:
                logger.warning(f"⚠️ 警告: 质量偏好的标准差接近0 ({std:.6f})，这可能导致得分问题!")
                # 使用默认标准差
                std = 1.0
                if self.detailed_scoring:
                    logger.info(f"   - 使用默认标准差: {std}")
            
            # 记录质量评分详情
            if self.detailed_scoring:
                logger.info(f"   - 质量分布: 用户偏好均值={mean:.2f}, 标准差={std:.2f}")
            
            if rating >= mean:
                quality_score = 0.5 + 0.5 * (rating - mean) / (5.0 - mean) if mean < 5.0 else 1.0
                if self.detailed_scoring:
                    logger.info(f"   - 质量得分(高于均值): {quality_score:.4f} (评分={rating}, 计算公式=0.5 + 0.5 * (rating - mean) / (5.0 - mean))")
            else:
                max_pdf = stats.norm.pdf(mean, mean, std)
                current_pdf = stats.norm.pdf(rating, mean, std)
                quality_score = (current_pdf / max_pdf) * 0.5 if max_pdf > 0 else 0.1
                if self.detailed_scoring:
                    logger.info(f"   - 质量得分(低于均值): {quality_score:.4f} (评分={rating}, max_pdf={max_pdf:.6f}, current_pdf={current_pdf:.6f})")
        else:
            quality_score = 0.5
            if self.detailed_scoring:
                logger.info(f"   - 质量得分: {quality_score:.4f} (默认值, 无有效质量模型)")
        
        # 添加随机因子以避免完全相同的得分 (0.96-1.04的随机数)
        random_factor = 0.96 + random.random() * 0.08
        quality_score = quality_score * random_factor
        
        if self.detailed_scoring:
            logger.info(f"   - 应用随机因子: {random_factor:.4f}, 调整后质量得分: {quality_score:.4f}")
        
        # 4. 综合得分
        total_score = (category_score * 0.5 + 
                      price_score * 0.25 + 
                      quality_score * 0.25)
        
        if self.detailed_scoring:
            logger.info(f"   - 基础综合得分: {total_score:.4f} = 类别({category_score:.4f}×0.5) + 价格({price_score:.4f}×0.25) + 质量({quality_score:.4f}×0.25)")
        
        # 5. 应用业务规则
        original_score = total_score
        
        if is_new:
            total_score *= 1.1
            if self.detailed_scoring:
                logger.info(f"   - 新品加成: {original_score:.4f} → {total_score:.4f} (×1.1)")
        
        if stock <= 0:
            original_score = total_score
            total_score *= 0.1
            if self.detailed_scoring:
                logger.info(f"   - 无库存惩罚: {original_score:.4f} → {total_score:.4f} (×0.1)")
        elif stock <= 5:
            original_score = total_score
            total_score *= 0.8
            if self.detailed_scoring:
                logger.info(f"   - 低库存惩罚: {original_score:.4f} → {total_score:.4f} (×0.8)")
        
        final_score = max(0.0, min(1.0, total_score))
        
        if self.detailed_scoring:
            logger.info(f"   - 最终得分: {final_score:.4f}")
        
        return final_score
    
    def recommend_products(self, products: List[Dict], momentum: Dict, 
                         top_k: int = 10) -> List[Dict]:
        """基于动量推荐商品"""
        logger.info(f"🔍 开始推荐商品: 候选商品数={len(products)}, Top-K={top_k}")
        
        # 记录动量类型和主要特征
        momentum_type = momentum.get('type', 'unknown')
        logger.info(f"📊 使用动量类型: {momentum_type}")
        
        # 处理字段名兼容性问题
        # 确保字段名一致，同时支持驼峰命名和下划线命名
        if 'categoryPreferences' in momentum and 'category_preferences' not in momentum:
            momentum['category_preferences'] = momentum['categoryPreferences']
        elif 'category_preferences' in momentum and 'categoryPreferences' not in momentum:
            momentum['categoryPreferences'] = momentum['category_preferences']
            
        if 'priceModel' in momentum and 'price_model' not in momentum:
            momentum['price_model'] = momentum['priceModel']
        elif 'price_model' in momentum and 'priceModel' not in momentum:
            momentum['priceModel'] = momentum['price_model']
            
        if 'qualityModel' in momentum and 'quality_model' not in momentum:
            momentum['quality_model'] = momentum['qualityModel']
        elif 'quality_model' in momentum and 'qualityModel' not in momentum:
            momentum['qualityModel'] = momentum['quality_model']
        
        # 记录动量的主要特征
        if momentum_type != 'unknown':
            # 记录类别偏好
            category_prefs = momentum.get('categoryPreferences', {}) or momentum.get('category_preferences', {})
            if category_prefs:
                top_cats = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)
                logger.info(f"📊 动量的完整类别偏好:")
                for i, (category, weight) in enumerate(top_cats):
                    logger.info(f"   {i+1}. {category}: {weight:.4f} ({weight*100:.1f}%)")
            
            # 记录价格偏好
            price_model = momentum.get('priceModel', {}) or momentum.get('price_model', {})
            if price_model:
                price_mean = price_model.get('mean', 0)
                price_std = price_model.get('std', 0)
                logger.info(f"💰 价格偏好: 均值={price_mean:.2f}, 标准差={price_std:.2f}, 范围=[{price_mean-price_std:.2f}, {price_mean+price_std:.2f}]")
                logger.info(f"   价格偏好详情: {price_model}")
            
            # 记录质量偏好
            quality_model = momentum.get('qualityModel', {}) or momentum.get('quality_model', {})
            if quality_model:
                quality_mean = quality_model.get('mean', 0)
                quality_std = quality_model.get('std', 0)
                logger.info(f"⭐ 质量偏好: 均值={quality_mean:.2f}, 标准差={quality_std:.2f}, 范围=[{max(0, quality_mean-quality_std):.2f}, {min(5, quality_mean+quality_std):.2f}]")
                logger.info(f"   质量偏好详情: {quality_model}")
        
        # 计算商品得分
        scored_products = []
        logger.info("⚙️ 开始计算商品得分...")
        
        # 记录所有候选商品的基本信息
        logger.info("📋 候选商品列表:")
        for i, product in enumerate(products[:20]):  # 限制显示前20个，避免日志过长
            product_id = product.get('product_id', 'unknown')
            category = product.get('category', 'unknown')
            price = product.get('price', 0)
            rating = product.get('rating', 0)
            logger.info(f"   {i+1}. ID={product_id}, 类别={category}, 价格={price:.2f}, 评分={rating:.1f}")
        
        if len(products) > 20:
            logger.info(f"   ... 还有 {len(products) - 20} 个商品未显示")
        
        logger.info("🧮 开始为每个商品计算得分...")
        
        # 为每个商品计算得分
        for product in products:
            logger.info(f"-------------------------------------------")
            score = self.calculate_product_score(product, momentum)
            scored_products.append({
                **product,
                'recommendation_score': score
            })
        
        # 按得分排序
        scored_products.sort(key=lambda x: x['recommendation_score'], reverse=True)
        top_recommendations = scored_products[:top_k]
        
        # 记录推荐结果
        logger.info(f"✅ 推荐完成: 返回 {len(top_recommendations)} 个商品")
        
        # 记录所有推荐商品详情
        logger.info(f"🏆 所有推荐商品 (Top-{len(top_recommendations)}):")
        for i, product in enumerate(top_recommendations):
            product_id = product.get('product_id', 'unknown')
            category = product.get('category', 'unknown')
            price = product.get('price', 0)
            rating = product.get('rating', 0)
            score = product.get('recommendation_score', 0)
            logger.info(f"   {i+1}. ID={product_id}, 类别={category}, 价格={price:.2f}, 评分={rating:.1f}, 得分={score:.4f}")
        
        # 检查得分是否相同
        scores = [p.get('recommendation_score', 0) for p in top_recommendations]
        if len(set(scores)) == 1 and len(scores) > 1:
            logger.warning("⚠️ 警告: 所有推荐商品得分相同，可能存在算法问题!")
            logger.warning(f"   所有商品得分均为: {scores[0]:.4f}")
        
        # 记录得分分布
        if scores:
            min_score = min(scores)
            max_score = max(scores)
            avg_score = sum(scores) / len(scores)
            logger.info(f"📊 推荐得分分布: 最小={min_score:.4f}, 最大={max_score:.4f}, 平均={avg_score:.4f}")
        
        # 记录推荐商品类别分布
        category_counts = {}
        for product in top_recommendations:
            category = product.get('category', 'unknown')
            category_counts[category] = category_counts.get(category, 0) + 1
        
        if category_counts:
            logger.info("📊 推荐商品类别分布:")
            sorted_categories = sorted(category_counts.items(), key=lambda x: x[1], reverse=True)
            for category, count in sorted_categories:
                percentage = (count / len(top_recommendations)) * 100
                logger.info(f"   - {category}: {count}个 ({percentage:.1f}%)")
        
        # 记录所有候选商品的得分分布
        all_scores = [p.get('recommendation_score', 0) for p in scored_products]
        if all_scores:
            min_all = min(all_scores)
            max_all = max(all_scores)
            avg_all = sum(all_scores) / len(all_scores)
            logger.info(f"📊 所有候选商品得分分布: 最小={min_all:.4f}, 最大={max_all:.4f}, 平均={avg_all:.4f}")
            
            # 检查得分是否都相同
            if len(set(all_scores)) == 1:
                logger.warning("⚠️ 严重警告: 所有候选商品得分完全相同，算法可能存在严重问题!")
        
        return top_recommendations
    
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
        获取动量的摘要信息
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
            'type': momentum.get('type', 'unknown'),
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
    
    def apply_momentum_decay(self, momentum: Dict, hours_since_update: float = 24.0) -> Dict:
        """
        应用动量衰减
        短期动量会随时间衰减，回归到长期动量
        """
        if momentum.get('type') != 'push_momentum':
            return momentum
        
        # 计算衰减系数
        decay_rate = 0.1
        decay_factor = math.exp(-decay_rate * hours_since_update)
        
        # 对推送动量进行适度衰减
        if 'cumulative_clicks' in momentum and momentum['cumulative_clicks'] > 0:
            # 衰减累积影响
            original_clicks = momentum['cumulative_clicks']
            decayed_clicks = max(1, int(original_clicks * decay_factor))
            momentum['cumulative_clicks'] = decayed_clicks
            momentum['decay_applied'] = {
                'hours_elapsed': hours_since_update,
                'decay_factor': decay_factor,
                'original_clicks': original_clicks,
                'decayed_clicks': decayed_clicks
            }
        
        return momentum
