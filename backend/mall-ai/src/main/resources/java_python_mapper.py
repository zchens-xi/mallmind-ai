#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Java-Python数据传输兼容性工具
处理Java和Python之间的数据格式转换和兼容性问题
"""

import logging
import math
import json
import numpy as np
from datetime import datetime
from typing import Dict, List, Any, Optional
from config import RECOMMENDATION_CONFIG

logger = logging.getLogger(__name__)

class JavaPythonMapper:
    """Java和Python数据格式映射器"""
    
    @staticmethod
    def sanitize_float_value(value: Any, default: float = 0.0) -> float:
        """
        确保浮点数值安全，处理特殊情况：
        - 将numpy数值类型转换为Python原生类型
        - 将NaN和Infinity转换为0
        - 将None转换为默认值
        
        Args:
            value: 需要清理的数值
            default: 默认值，当输入为None时返回
        Returns:
            清理后的浮点数
        """
        if value is None:
            return default
            
        try:
            # 处理numpy数值类型
            if hasattr(value, 'item'):
                try:
                    value = value.item()  # 转换numpy标量为Python原生类型
                except (ValueError, AttributeError):
                    logger.warning(f"⚠️ 无法转换numpy值: {value}, 类型: {type(value)}")
                    return default
            
            # 转换为浮点数
            float_value = float(value)
            
            # 处理NaN和Infinity
            if math.isnan(float_value) or math.isinf(float_value):
                logger.warning(f"⚠️ 检测到NaN或Infinity值: {value}")
                return default
                
            return float_value
        except (ValueError, TypeError) as e:
            logger.warning(f"⚠️ 数值转换错误: {value}, 类型: {type(value)}, 错误: {str(e)}")
            return default
    
    @staticmethod
    def sanitize_numeric_dict(data: Dict) -> Dict:
        """
        递归清理字典中的数值数据，确保JSON安全
        - 将numpy数值类型转换为Python原生类型
        - 将NaN和Infinity转换为0
        - 将None转换为适当的默认值
        
        Args:
            data: 需要清理的字典数据
        Returns:
            清理后的字典
        """
        if not isinstance(data, dict):
            logger.warning(f"⚠️ sanitize_numeric_dict接收到非字典数据: {type(data)}")
            return {} if data is None else data
            
        result = {}
        for key, value in data.items():
            # 记录处理前的值类型
            if isinstance(value, dict):
                logger.debug(f"🔍 处理嵌套字典: {key}, 类型={type(value)}")
                result[key] = JavaPythonMapper.sanitize_numeric_dict(value)
            elif isinstance(value, list):
                logger.debug(f"🔍 处理列表: {key}, 长度={len(value)}")
                result[key] = [
                    JavaPythonMapper.sanitize_numeric_dict(item) if isinstance(item, dict) 
                    else JavaPythonMapper.sanitize_float_value(item) if isinstance(item, (int, float, np.number))
                    else item
                    for item in value
                ]
            elif isinstance(value, (int, float, np.number)):
                result[key] = JavaPythonMapper.sanitize_float_value(value)
                # 如果值发生了明显变化，记录日志
                if abs(result[key] - float(value)) > 0.0001:
                    logger.warning(f"⚠️ 数值清理: {key} 从 {value} 变为 {result[key]}")
            else:
                result[key] = value
                
        return result
    
    @staticmethod
    def validate_and_extract_data(request_data: Optional[Dict], required_fields: List[str]) -> tuple[bool, Dict, str]:
        """
        验证并提取请求数据
        Args:
            request_data: 请求数据
            required_fields: 必需字段列表
        Returns:
            (is_valid, extracted_data, error_message)
        """
        if not request_data:
            return False, {}, "Request body is empty or invalid JSON"
        
        missing_fields = [field for field in required_fields if field not in request_data]
        if missing_fields:
            return False, {}, f"Missing required fields: {', '.join(missing_fields)}"
        
        return True, request_data, ""
    
    @staticmethod
    def normalize_product_data(product: Dict) -> Dict:
        """
        标准化商品数据格式，确保Java和Python之间的一致性
        Args:
            product: 原始商品数据
        Returns:
            标准化后的商品数据
        """
        normalized = {}
        
        # 标准化字段映射
        field_mapping = {
            'product_id': ['productId', 'id', 'product_id'],
            'category': ['category', 'categoryName'],
            'price': ['price', 'salePrice', 'currentPrice'],
            'rating': ['rating', 'score', 'averageRating'],
            'stock': ['stock', 'inventory', 'stockQuantity'],
            'is_new': ['isNew', 'is_new', 'newProduct'],
            'brand': ['brand', 'brandName'],
            'name': ['name', 'productName', 'title']
        }
        
        for standard_field, possible_fields in field_mapping.items():
            value = None
            for field in possible_fields:
                if field in product and product[field] is not None:
                    value = product[field]
                    break
            
            # 类型转换和默认值
            if standard_field == 'price' and value is not None:
                try:
                    normalized[standard_field] = float(value)
                except (ValueError, TypeError):
                    normalized[standard_field] = 0.0
            elif standard_field == 'rating' and value is not None:
                try:
                    normalized[standard_field] = float(value)
                except (ValueError, TypeError):
                    normalized[standard_field] = 0.0
            elif standard_field == 'stock' and value is not None:
                try:
                    normalized[standard_field] = int(value)
                except (ValueError, TypeError):
                    normalized[standard_field] = 0
            elif standard_field == 'is_new' and value is not None:
                normalized[standard_field] = bool(value)
            else:
                normalized[standard_field] = value if value is not None else ""
        
        # 确保必需字段存在
        if not normalized.get('product_id'):
            normalized['product_id'] = f"unknown_{datetime.now().timestamp()}"
        if not normalized.get('category'):
            normalized['category'] = "unknown"
        if normalized.get('price') is None:
            normalized['price'] = 0.0
        if normalized.get('rating') is None:
            normalized['rating'] = 0.0
        if normalized.get('stock') is None:
            normalized['stock'] = 0
        
        return normalized
    
    @staticmethod
    def normalize_user_click_request(click_data: Dict) -> Dict:
        """
        标准化用户点击请求数据
        Args:
            click_data: 原始点击数据
        Returns:
            标准化后的点击数据
        """
        # 检查是否已经是正确的格式（包含clicked_product）
        if 'clicked_product' in click_data:
            return {
                'user_id': click_data.get('user_id'),
                'clicked_product': JavaPythonMapper.normalize_product_data(click_data['clicked_product']),
                'click_strength': float(click_data.get('click_strength', 0.3))
            }
        
        # 否则从扁平结构构建clicked_product
        clicked_product = {
            'product_id': click_data.get('product_id', click_data.get('productId', '')),
            'category': click_data.get('category', ''),
            'price': click_data.get('price', 0.0),
            'rating': click_data.get('rating', 0.0)
        }
        
        return {
            'user_id': click_data.get('user_id'),
            'clicked_product': JavaPythonMapper.normalize_product_data(clicked_product),
            'click_strength': float(click_data.get('click_strength', 0.3))
        }
    
    @staticmethod
    def format_response_for_java(python_response: Dict, success: bool = True) -> Dict:
        """
        格式化Python响应为Java期望的格式
        Args:
            python_response: Python响应数据
            success: 是否成功
        Returns:
            Java格式的响应
        """
        # 记录原始响应数据
        logger.info(f"📊 格式化响应 - 原始数据类型: {type(python_response)}")
        if isinstance(python_response, dict):
            logger.info(f"📊 原始响应字段: {list(python_response.keys())}")
        
        # 对响应数据进行最终验证和清理
        if isinstance(python_response, dict):
            # 确保所有数值类型正确
            for key, value in list(python_response.items()):
                if isinstance(value, dict):
                    python_response[key] = JavaPythonMapper.sanitize_numeric_dict(value)
                elif isinstance(value, (int, float, np.number)):
                    python_response[key] = JavaPythonMapper.sanitize_float_value(value)
            
            # 检查并修复categoryPreferences
            if 'categoryPreferences' in python_response:
                if not isinstance(python_response['categoryPreferences'], dict):
                    logger.warning(f"⚠️ categoryPreferences类型错误: {type(python_response['categoryPreferences'])}，使用空字典")
                    python_response['categoryPreferences'] = {}
                else:
                    # 确保categoryPreferences中没有嵌套对象
                    for key, value in list(python_response['categoryPreferences'].items()):
                        if isinstance(value, dict):
                            logger.warning(f"⚠️ 发现类别偏好中的嵌套对象: {key}，已移除")
                            del python_response['categoryPreferences'][key]
                        elif not isinstance(value, (int, float, np.number)):
                            logger.warning(f"⚠️ 类别偏好中的非数值: {key}={value}，已移除")
                            del python_response['categoryPreferences'][key]
                
            # 检查并修复priceModel
            if 'priceModel' in python_response and not isinstance(python_response['priceModel'], dict):
                logger.warning(f"⚠️ priceModel类型错误: {type(python_response['priceModel'])}，使用默认模型")
                python_response['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
                
            # 检查并修复qualityModel
            if 'qualityModel' in python_response and not isinstance(python_response['qualityModel'], dict):
                logger.warning(f"⚠️ qualityModel类型错误: {type(python_response['qualityModel'])}，使用默认模型")
                python_response['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        
        # 构建Java格式的响应
        java_response = {
            'success': success,
            'code': 200 if success else 500,
            'message': '操作成功' if success else '操作失败',
            'data': python_response
        }
        
        # 记录最终响应
        logger.info(f"📊 Java响应格式: {list(java_response.keys())}")
        logger.info(f"📊 响应状态: success={java_response['success']}, code={java_response['code']}")
        
        # 将响应转换为JSON字符串并再次解析，确保所有数据都是JSON安全的
        try:
            # 转换为JSON字符串
            response_json = json.dumps(java_response)
            logger.info(f"📊 响应JSON长度: {len(response_json)}")
            
            # 再次解析回Python对象，这样可以捕获任何序列化问题
            parsed_response = json.loads(response_json)
            
            # 最终检查 - 确保categoryPreferences中没有嵌套对象
            if ('data' in parsed_response and 
                isinstance(parsed_response['data'], dict) and 
                'categoryPreferences' in parsed_response['data']):
                
                category_prefs = parsed_response['data']['categoryPreferences']
                if isinstance(category_prefs, dict):
                    # 移除任何嵌套对象
                    for key, value in list(category_prefs.items()):
                        if isinstance(value, dict):
                            logger.warning(f"⚠️ 最终检查: 发现类别偏好中的嵌套对象: {key}，已移除")
                            del category_prefs[key]
            
            return parsed_response
        except Exception as e:
            logger.error(f"❌ JSON序列化失败: {str(e)}")
            # 返回简化的响应
            return {
                'success': False,
                'code': 500,
                'message': f'JSON序列化失败: {str(e)}',
                'data': {}
            }
    
    @staticmethod
    def extract_recommendations_from_response(python_recommendations: List[Dict]) -> List[Dict]:
        """
        从Python推荐结果中提取Java需要的格式，确保数值安全
        Args:
            python_recommendations: Python推荐系统返回的推荐列表
        Returns:
            Java期望的推荐列表格式
        """
        java_recommendations = []
        
        for rec in python_recommendations:
            # 标准化推荐商品数据
            normalized_rec = JavaPythonMapper.normalize_product_data(rec)
            
            # 添加推荐相关字段，使用安全的数值转换
            java_rec = {
                'productId': normalized_rec.get('product_id', ''),
                'category': normalized_rec.get('category', ''),
                'price': JavaPythonMapper.sanitize_float_value(normalized_rec.get('price', 0.0)),
                'rating': JavaPythonMapper.sanitize_float_value(normalized_rec.get('rating', 0.0)),
                'stock': int(normalized_rec.get('stock', 0)),
                'name': normalized_rec.get('name', ''),
                'brand': normalized_rec.get('brand', ''),
                'isNew': bool(normalized_rec.get('is_new', False)),
                'recommendationScore': JavaPythonMapper.sanitize_float_value(rec.get('recommendation_score', 0.0)),
                'reason': rec.get('reason', 'Based on your preferences'),
                'confidence': JavaPythonMapper.sanitize_float_value(rec.get('confidence', 0.8))
            }
            
            java_recommendations.append(java_rec)
        
        return java_recommendations
    
    @staticmethod
    def handle_momentum_response(momentum_data: Dict) -> Dict:
        """
        处理动量响应数据，确保格式兼容和JSON安全
        Args:
            momentum_data: Python动量数据
        Returns:
            处理后的动量数据
        """
        if not momentum_data:
            logger.warning("⚠️ 动量数据为空，返回空字典")
            return {}

        # 记录原始数据
        logger.info(f"📊 处理动量响应 - 原始数据类型: {type(momentum_data)}")
        logger.info(f"📊 原始动量数据字段: {list(momentum_data.keys())}")
        
        # 检查categoryPreferences字段
        if 'categoryPreferences' in momentum_data:
            logger.info(f"📊 原始categoryPreferences类型: {type(momentum_data['categoryPreferences'])}")
        elif 'category_preferences' in momentum_data:
            logger.info(f"📊 原始category_preferences类型: {type(momentum_data['category_preferences'])}")
        else:
            logger.warning("⚠️ 动量数据中没有类别偏好字段")
            
        # 检查priceModel字段
        if 'priceModel' in momentum_data:
            logger.info(f"📊 原始priceModel类型: {type(momentum_data['priceModel'])}")
            if isinstance(momentum_data['priceModel'], dict):
                logger.info(f"📊 priceModel字段: {list(momentum_data['priceModel'].keys())}")
        elif 'price_model' in momentum_data:
            logger.info(f"📊 原始price_model类型: {type(momentum_data['price_model'])}")
            if isinstance(momentum_data['price_model'], dict):
                logger.info(f"📊 price_model字段: {list(momentum_data['price_model'].keys())}")

        # 创建一个新的干净的数据结构
        processed = {}
        
        # 处理类别偏好 - 确保它只包含字符串到浮点数的映射
        if 'categoryPreferences' in momentum_data and isinstance(momentum_data['categoryPreferences'], dict):
            processed['categoryPreferences'] = {}
            for key, value in momentum_data['categoryPreferences'].items():
                if isinstance(key, str) and isinstance(value, (int, float, np.number)) and key not in ['priceModel', 'qualityModel', 'price_model', 'quality_model']:
                    processed['categoryPreferences'][key] = JavaPythonMapper.sanitize_float_value(value)
        elif 'category_preferences' in momentum_data and isinstance(momentum_data['category_preferences'], dict):
            processed['categoryPreferences'] = {}
            for key, value in momentum_data['category_preferences'].items():
                if isinstance(key, str) and isinstance(value, (int, float, np.number)) and key not in ['priceModel', 'qualityModel', 'price_model', 'quality_model']:
                    processed['categoryPreferences'][key] = JavaPythonMapper.sanitize_float_value(value)
        
        # 如果类别偏好为空，使用默认值
        if 'categoryPreferences' not in processed or not processed['categoryPreferences']:
            logger.warning("⚠️ 类别偏好为空，使用默认类别偏好")
            processed['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
        
        # 处理价格模型
        if 'priceModel' in momentum_data and isinstance(momentum_data['priceModel'], dict):
            processed['priceModel'] = JavaPythonMapper.sanitize_numeric_dict(momentum_data['priceModel'])
        elif 'price_model' in momentum_data and isinstance(momentum_data['price_model'], dict):
            processed['priceModel'] = JavaPythonMapper.sanitize_numeric_dict(momentum_data['price_model'])
        else:
            logger.warning("⚠️ 价格模型为空，使用默认价格模型")
            processed['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
        
        # 处理质量模型
        if 'qualityModel' in momentum_data and isinstance(momentum_data['qualityModel'], dict):
            processed['qualityModel'] = JavaPythonMapper.sanitize_numeric_dict(momentum_data['qualityModel'])
        elif 'quality_model' in momentum_data and isinstance(momentum_data['quality_model'], dict):
            processed['qualityModel'] = JavaPythonMapper.sanitize_numeric_dict(momentum_data['quality_model'])
        else:
            logger.warning("⚠️ 质量模型为空，使用默认质量模型")
            processed['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        
        # 复制其他基本字段
        for key in ['type', 'userId', 'lastUpdated', 'dataSource', 'cumulativeClicks', 'isDefault',
                   'totalClicks', 'totalStrength', 'clickedCategories']:
            if key in momentum_data:
                processed[key] = momentum_data[key]
        
        # 处理latestUpdate字段
        if 'latestUpdate' in momentum_data and isinstance(momentum_data['latestUpdate'], dict):
            processed['latestUpdate'] = JavaPythonMapper.sanitize_numeric_dict(momentum_data['latestUpdate'])
        
        # 记录处理后的数据
        logger.info(f"📊 处理后的动量数据字段: {list(processed.keys())}")
        if 'categoryPreferences' in processed:
            logger.info(f"📊 处理后的categoryPreferences类型: {type(processed['categoryPreferences'])}")
            logger.info(f"📊 处理后的categoryPreferences键数量: {len(processed['categoryPreferences'])}")
        if 'priceModel' in processed:
            logger.info(f"📊 处理后的priceModel类型: {type(processed['priceModel'])}")
            if isinstance(processed['priceModel'], dict):
                logger.info(f"📊 处理后的priceModel字段: {list(processed['priceModel'].keys())}")
        
        # 最终验证 - 确保没有嵌套对象在不该有的地方
        if 'categoryPreferences' in processed:
            for key, value in list(processed['categoryPreferences'].items()):
                if isinstance(value, dict):
                    logger.warning(f"⚠️ 发现类别偏好中的嵌套对象: {key}，已移除")
                    del processed['categoryPreferences'][key]
        
        return processed

    @staticmethod
    def log_data_exchange(operation: str, request_data: Dict, response_data: Dict, user_id: Optional[int] = None):
        """
        记录数据交换日志，用于调试
        Args:
            operation: 操作名称
            request_data: 请求数据
            response_data: 响应数据
            user_id: 用户ID（可选）
        """
        log_info = {
            'operation': operation,
            'user_id': user_id,
            'request_size': len(str(request_data)),
            'response_size': len(str(response_data)),
            'timestamp': datetime.now().isoformat()
        }
        
        # 记录关键字段以便调试
        if 'products' in request_data:
            log_info['request_products_count'] = len(request_data['products'])
        if 'clicked_product' in request_data:
            log_info['clicked_product_id'] = request_data['clicked_product'].get('product_id', 'unknown')
        if isinstance(response_data, dict) and 'recommendations' in response_data:
            log_info['response_recommendations_count'] = len(response_data['recommendations'])
        
        logger.info(f"📊 数据交换记录: {log_info}")

# 全局映射器实例
mapper = JavaPythonMapper()
