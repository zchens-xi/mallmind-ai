#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
FastAPI推荐算法微服务
专注于核心推荐算法计算，数据IO由SpringBoot管理
"""
import uvicorn
from fastapi import FastAPI, HTTPException, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel, Field
from typing import Dict, List, Optional, Any
import logging
from datetime import datetime
import traceback
import json

# 导入核心推荐算法类（去掉MongoDB依赖）
from recommendation_core import RecommendationEngine
from java_python_mapper import mapper
from config import RECOMMENDATION_CONFIG

# 配置日志
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 创建FastAPI应用
app = FastAPI(
    title="Mall AI Recommendation Service",
    description="个性化推荐算法微服务 - 专注于核心算法计算",
    version="2.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 生产环境中应该指定具体的域名
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 全局推荐引擎实例
recommendation_engine = RecommendationEngine()

# Pydantic模型定义
class ProductModel(BaseModel):
    product_id: str = Field(..., description="商品ID", alias="productId")
    category: str = Field(..., description="商品类别")
    price: float = Field(..., ge=0, description="商品价格")
    rating: float = Field(default=0.0, ge=0, le=5, description="商品评分")
    stock: int = Field(default=0, ge=0, description="库存数量")
    is_new: bool = Field(default=False, description="是否新品", alias="isNew")
    brand: Optional[str] = Field(default="", description="品牌")
    name: Optional[str] = Field(default="", description="商品名称")
    
    class Config:
        allow_population_by_field_name = True
        
    def dict(self, **kwargs):
        # 重写dict方法，确保输出的字段名是驼峰命名法
        data = super().dict(**kwargs)
        # 将蛇形命名法转换为驼峰命名法
        if 'product_id' in data:
            data['productId'] = data.pop('product_id')
        if 'is_new' in data:
            data['isNew'] = data.pop('is_new')
        return data

class UserBehaviorModel(BaseModel):
    orders: List[Dict[str, Any]] = Field(default=[], description="订单历史")
    cart: List[Dict[str, Any]] = Field(default=[], description="购物车历史")
    returns: List[Dict[str, Any]] = Field(default=[], description="退货历史")
    
    class Config:
        allow_population_by_field_name = True

class RecommendationRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    products: List[ProductModel] = Field(..., description="候选商品列表")
    user_momentum: Optional[Dict[str, Any]] = Field(default=None, description="用户动量数据", alias="userMomentum")
    top_k: int = Field(default=10, ge=1, le=100, description="推荐数量", alias="topK")
    include_scores: bool = Field(default=False, description="是否包含推荐得分", alias="includeScores")
    
    class Config:
        allow_population_by_field_name = True

class ClickRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    clicked_product: ProductModel = Field(..., description="点击的商品", alias="clickedProduct")
    current_momentum: Optional[Dict[str, Any]] = Field(default=None, description="当前用户动量", alias="currentMomentum")
    click_strength: float = Field(default=0.3, ge=0.1, le=2.0, description="点击强度", alias="clickStrength")
    
    class Config:
        allow_population_by_field_name = True

class ShortTermMomentumRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    clicked_products: List[ProductModel] = Field(..., description="点击的商品列表", alias="clickedProducts")
    click_strengths: Optional[List[float]] = Field(default=None, description="点击强度列表", alias="clickStrengths")
    
    class Config:
        allow_population_by_field_name = True

class LongTermMomentumRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    user_behavior_data: UserBehaviorModel = Field(..., description="用户行为数据", alias="userBehaviorData")
    
    class Config:
        allow_population_by_field_name = True

class PushMomentumRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    long_term_momentum: Optional[Dict[str, Any]] = Field(default=None, description="长期动量", alias="longTermMomentum")
    short_term_momentum: Optional[Dict[str, Any]] = Field(default=None, description="短期动量", alias="shortTermMomentum")
    current_push_momentum: Optional[Dict[str, Any]] = Field(default=None, description="当前推送动量", alias="currentPushMomentum")
    
    class Config:
        allow_population_by_field_name = True

class ThreeMomentumRequest(BaseModel):
    user_id: int = Field(..., description="用户ID", alias="userId")
    products: List[ProductModel] = Field(..., description="候选商品列表")
    long_term_momentum: Optional[Dict[str, Any]] = Field(default=None, description="长期动量", alias="longTermMomentum")
    current_push_momentum: Optional[Dict[str, Any]] = Field(default=None, description="当前推送动量", alias="currentPushMomentum")
    clicked_product: Optional[ProductModel] = Field(default=None, description="点击的商品", alias="clickedProduct")
    top_k: int = Field(default=10, ge=1, le=100, description="推荐数量", alias="topK")
    
    class Config:
        allow_population_by_field_name = True

# API路由定义

@app.get("/")
async def root():
    """根路径 - 服务健康检查"""
    return {
        "service": "Mall AI Recommendation Service",
        "status": "healthy",
        "version": "2.0.0",
        "description": "专注于推荐算法计算的微服务",
        "timestamp": datetime.now().isoformat()
    }

@app.get("/health")
async def health_check():
    """健康检查接口"""
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "service_info": {
            "algorithm_engine": "initialized",
            "dependencies": "minimal",
            "data_storage": "external_springboot"
        }
    }

@app.get("/api/v1/momentum-logs")
async def get_momentum_logs(lines: int = 100):
    """
    获取长期动量计算日志
    Args:
        lines: 返回的日志行数
    Returns:
        最近的日志行
    """
    try:
        import os
        log_file = os.path.join(os.path.dirname(__file__), 'logs', 'long_term_momentum.log')
        
        if not os.path.exists(log_file):
            return {"status": "error", "message": "Log file not found", "logs": []}
        
        # 读取最后N行日志
        with open(log_file, 'r', encoding='utf-8') as f:
            all_lines = f.readlines()
            log_lines = all_lines[-lines:] if len(all_lines) > lines else all_lines
        
        return {
            "status": "success", 
            "message": f"Retrieved {len(log_lines)} lines of logs", 
            "logs": log_lines
        }
    except Exception as e:
        logger.error(f"Error reading momentum logs: {e}")
        return {"status": "error", "message": str(e), "logs": []}

@app.post("/api/v1/calculate-long-term-momentum")
async def calculate_long_term_momentum(request: LongTermMomentumRequest):
    """计算用户长期动量"""
    user_id = request.user_id
    logger.info(f"🔄 开始计算用户 {user_id} 的长期动量")
    
    try:
        # 记录请求详情
        orders_count = len(request.user_behavior_data.orders) if hasattr(request.user_behavior_data, 'orders') else 0
        cart_count = len(request.user_behavior_data.cart) if hasattr(request.user_behavior_data, 'cart') else 0
        returns_count = len(request.user_behavior_data.returns) if hasattr(request.user_behavior_data, 'returns') else 0
        
        logger.info(f"📊 用户 {user_id} 行为数据统计: 订单={orders_count}, 购物车={cart_count}, 退货={returns_count}")
        
        # 记录订单数据结构样例
        if orders_count > 0:
            order_sample = request.user_behavior_data.orders[0]
            logger.info(f"📋 订单数据结构样例: {order_sample}")
            logger.info(f"📋 订单数据字段: {list(order_sample.keys())}")
            logger.info(f"📋 订单数据关键字段检查: 类别={'category' in order_sample}, 价格={'price' in order_sample}")
        
        # 记录购物车数据结构样例
        if cart_count > 0:
            cart_sample = request.user_behavior_data.cart[0]
            logger.info(f"📋 购物车数据结构样例: {cart_sample}")
            logger.info(f"📋 购物车数据字段: {list(cart_sample.keys())}")
        
        logger.info(f"⚙️ 正在为用户 {user_id} 计算长期动量...")
        
        # 转换为字典格式
        user_behavior_dict = {
            'orders': request.user_behavior_data.orders,
            'cart': request.user_behavior_data.cart,
            'returns': request.user_behavior_data.returns
        }
        
        # 计算长期动量
        momentum_data = recommendation_engine.compute_long_term_momentum(user_behavior_dict)
        
        # 记录计算结果
        logger.info(f"✅ 用户 {user_id} 长期动量计算完成: 类型={momentum_data.get('type')}, 涉及类别数={len(momentum_data.get('categoryPreferences', {}))}")
        
        # 详细记录动量数据
        logger.info(f"📊 动量数据详情:")
        logger.info(f"📊 类别偏好: {momentum_data.get('categoryPreferences', {})}")
        logger.info(f"📊 价格模型: {momentum_data.get('priceModel', {})}")
        logger.info(f"📊 质量模型: {momentum_data.get('qualityModel', {})}")
        
        # 处理响应数据
        logger.info(f"🔄 正在处理用户 {user_id} 的长期动量响应数据...")
        response_data = mapper.handle_momentum_response(momentum_data)
        
        # 记录处理后的响应数据
        logger.info(f"📊 处理后的响应数据:")
        logger.info(f"📊 类别偏好: {response_data.get('categoryPreferences', {})}")
        logger.info(f"📊 价格模型: {response_data.get('priceModel', {})}")
        logger.info(f"📊 质量模型: {response_data.get('qualityModel', {})}")
        
        # 最终验证，确保数据结构与Java端兼容
        if 'priceModel' in response_data and not isinstance(response_data['priceModel'], dict):
            logger.warning(f"⚠️ 最终验证: priceModel类型错误 ({type(response_data['priceModel'])}), 修正为字典")
            response_data['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
            
        if 'qualityModel' in response_data and not isinstance(response_data['qualityModel'], dict):
            logger.warning(f"⚠️ 最终验证: qualityModel类型错误 ({type(response_data['qualityModel'])}), 修正为字典")
            response_data['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()
            
        if 'categoryPreferences' in response_data and not isinstance(response_data['categoryPreferences'], dict):
            logger.warning(f"⚠️ 最终验证: categoryPreferences类型错误 ({type(response_data['categoryPreferences'])}), 修正为字典")
            response_data['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
            
        # 记录最终JSON
        logger.info(f"📊 响应数据JSON: {json.dumps(response_data)}")
        
        # 记录数据交换信息
        mapper.log_data_exchange('calculate_long_term_momentum', 
                                        {'user_id': user_id}, 
                                        response_data, 
                                        user_id)
        
        logger.info(f"🎉 用户 {user_id} 长期动量计算成功完成")
        return mapper.format_response_for_java(response_data)
    except Exception as e:
        logger.error(f"❌ 计算用户 {user_id} 长期动量时出错: {str(e)}")
        logger.exception(e)
        return mapper.format_response_for_java({}, success=False)

@app.post("/api/v1/calculate-short-term-momentum")
async def calculate_short_term_momentum(request: ShortTermMomentumRequest):
    """
    计算短期动量（基于单次或多次点击）
    Args:
        request: 短期动量请求数据
    Returns:
        短期动量数据
    """
    logger.info(f"🔄 开始计算用户 {request.user_id} 的短期动量")
    try:
        # 转换商品数据格式
        clicked_products = [product.dict() for product in request.clicked_products]
        
        # 记录请求数据
        logger.info(f"📊 请求数据: {json.dumps({'userId': request.user_id, 'clickedProducts': clicked_products})}")
        
        click_strengths = request.click_strengths
        
        # 如果没有提供点击强度，则使用默认值
        if click_strengths is None:
            click_strengths = [1.0] * len(clicked_products)
        
        # 记录点击信息
        products_count = len(clicked_products)
        logger.info(f"📱 用户 {request.user_id} 点击商品数量: {products_count}")
        
        # 记录部分点击信息
        if products_count > 0:
            sample_product = clicked_products[0]
            product_id = sample_product.get('productId', 'unknown')
            category = sample_product.get('category', 'unknown')
            price = sample_product.get('price', 0.0)
            logger.info(f"📱 示例点击商品: ID={product_id}, 类别={category}, 价格={price}")
        
        # 计算短期动量
        logger.info(f"⚙️ 正在为用户 {request.user_id} 计算短期动量...")
        short_term_momentum = recommendation_engine.compute_short_term_momentum(
            clicked_products,
            click_strengths
        )
        
        # 记录计算结果
        momentum_type = short_term_momentum.get('type', 'unknown')
        total_clicks = short_term_momentum.get('totalClicks', 0)
        total_strength = short_term_momentum.get('totalStrength', 0.0)
        logger.info(f"✅ 用户 {request.user_id} 短期动量计算完成: 类型={momentum_type}, 总点击数={total_clicks}, 总强度={total_strength:.2f}")
        
        # 记录短期动量详情
        category_prefs = short_term_momentum.get('category_preferences', {})
        if category_prefs:
            logger.info(f"📊 用户 {request.user_id} 短期动量类别偏好:")
            top_categories = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)[:3]
            for i, (category, weight) in enumerate(top_categories):
                logger.info(f"   {i+1}. {category}: {weight:.4f} ({weight*100:.1f}%)")
        
        # 记录价格和质量偏好
        price_model = short_term_momentum.get('price_model', {})
        quality_model = short_term_momentum.get('quality_model', {})
        
        if price_model:
            price_mean = price_model.get('mean', 0)
            price_std = price_model.get('std', 0)
            logger.info(f"💰 用户 {request.user_id} 短期价格偏好: 均值={price_mean:.2f}, 标准差={price_std:.2f}")
            logger.info(f"   价格偏好范围: [{max(0, price_mean-price_std):.2f}, {price_mean+price_std:.2f}]")
        
        if quality_model:
            quality_mean = quality_model.get('mean', 0)
            quality_std = quality_model.get('std', 0)
            logger.info(f"⭐ 用户 {request.user_id} 短期质量偏好: 均值={quality_mean:.2f}, 标准差={quality_std:.2f}")
            logger.info(f"   质量偏好范围: [{max(0, quality_mean-quality_std):.2f}, {min(5, quality_mean+quality_std):.2f}]")
        
        # 格式化响应
        logger.info(f"🔄 正在处理用户 {request.user_id} 的短期动量响应数据...")
        response_data = mapper.handle_momentum_response(short_term_momentum)
        
        # 记录日志
        mapper.log_data_exchange(
            "calculate_short_term_momentum",
            {"user_id": request.user_id, "products_count": len(clicked_products)},
            response_data,
            request.user_id
        )
        
        logger.info(f"🎉 用户 {request.user_id} 短期动量计算成功完成")
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"❌ 计算用户 {request.user_id} 短期动量失败: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"计算短期动量失败: {str(e)}")

@app.post("/api/v1/calculate-push-momentum")
async def calculate_push_momentum(request: ClickRequest):
    """
    计算推送动量（累积更新）
    Args:
        request: 包含当前动量和点击信息的请求
    Returns:
        更新后的推送动量
    """
    logger.info(f"🔄 开始计算用户 {request.user_id} 的推送动量")
    try:
        # 转换数据格式
        clicked_product = request.clicked_product.dict()
        current_momentum = request.current_momentum
        
        # 记录输入状态
        product_id = clicked_product.get('productId', 'unknown')
        has_current_momentum = current_momentum is not None
        logger.info(f"📊 用户 {request.user_id} 推送动量输入: 点击商品={product_id}, 已有动量={has_current_momentum}")
        
        # 如果没有提供当前动量，使用默认长期动量
        if current_momentum is None:
            logger.info(f"⚠️ 用户 {request.user_id} 没有提供当前动量，使用默认长期动量")
            current_momentum = recommendation_engine.get_default_long_term_momentum()
        
        # 计算短期动量
        logger.info(f"⚙️ 正在为用户 {request.user_id} 计算短期动量（用于推送动量更新）...")
        short_term_momentum = recommendation_engine.compute_short_term_momentum(
            [clicked_product],  # 转换为列表格式
            [request.click_strength]  # 转换为列表格式
        )
        
        # 计算推送动量
        logger.info(f"⚙️ 正在为用户 {request.user_id} 计算推送动量...")
        push_momentum = recommendation_engine.compute_push_momentum(
            current_momentum, 
            short_term_momentum,
            request.current_momentum  # 传入当前推送动量作为基础
        )
        
        # 记录计算结果
        momentum_type = push_momentum.get('type', 'unknown')
        cumulative_clicks = push_momentum.get('cumulative_clicks', 0)
        categories_count = len(push_momentum.get('category_preferences', {}))
        logger.info(f"✅ 用户 {request.user_id} 推送动量计算完成: 类型={momentum_type}, 累积点击={cumulative_clicks}, 涉及类别数={categories_count}")
        
        # 记录推送动量详情
        category_prefs = push_momentum.get('category_preferences', {})
        if category_prefs:
            logger.info(f"📊 用户 {request.user_id} 推送动量类别偏好:")
            top_categories = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)[:5]
            for i, (category, weight) in enumerate(top_categories):
                logger.info(f"   {i+1}. {category}: {weight:.4f} ({weight*100:.1f}%)")
        
        # 记录价格和质量偏好
        price_model = push_momentum.get('price_model', {})
        quality_model = push_momentum.get('quality_model', {})
        
        if price_model:
            price_mean = price_model.get('mean', 0)
            price_std = price_model.get('std', 0)
            logger.info(f"💰 用户 {request.user_id} 推送动量价格偏好: 均值={price_mean:.2f}, 标准差={price_std:.2f}")
            logger.info(f"   价格偏好范围: [{max(0, price_mean-price_std):.2f}, {price_mean+price_std:.2f}]")
        
        if quality_model:
            quality_mean = quality_model.get('mean', 0)
            quality_std = quality_model.get('std', 0)
            logger.info(f"⭐ 用户 {request.user_id} 推送动量质量偏好: 均值={quality_mean:.2f}, 标准差={quality_std:.2f}")
            logger.info(f"   质量偏好范围: [{max(0, quality_mean-quality_std):.2f}, {min(5, quality_mean+quality_std):.2f}]")
        
        # 记录动量变化
        latest_update = push_momentum.get('latest_update', {})
        if latest_update:
            logger.info(f"🔄 用户 {request.user_id} 推送动量更新信息:")
            logger.info(f"   - 长期基础: {latest_update.get('long_term_base', 'unknown')}")
            logger.info(f"   - 更新时间: {latest_update.get('update_timestamp', 'unknown')}")
            
            # 如果有短期影响的详细信息
            short_term = latest_update.get('short_term_influence', {})
            if short_term:
                short_term_strength = short_term.get('total_strength', 0)
                short_term_clicks = short_term.get('total_clicks', 0)
                logger.info(f"   - 短期影响: {short_term_clicks}个点击, 总强度={short_term_strength:.2f}")
        
        # 格式化响应
        logger.info(f"🔄 正在处理用户 {request.user_id} 的推送动量响应数据...")
        response_data = mapper.handle_momentum_response(push_momentum)
        
        # 记录日志
        mapper.log_data_exchange(
            "calculate_push_momentum",
            {"user_id": request.user_id, "product_id": clicked_product.get('product_id')},
            response_data,
            request.user_id
        )
        
        logger.info(f"🎉 用户 {request.user_id} 推送动量计算成功完成")
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"❌ 计算用户 {request.user_id} 推送动量失败: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"计算推送动量失败: {str(e)}")

@app.post("/api/v1/recommend-products")
async def recommend_products(request: RecommendationRequest):
    """
    基于动量推荐商品
    Args:
        request: 推荐请求
    Returns:
        推荐商品列表
    """
    logger.info(f"🔄 开始为用户 {request.user_id} 推荐商品")
    try:
        # 转换商品数据格式
        products = [product.dict() for product in request.products]
        products_count = len(products)
        
        # 获取或创建用户动量
        user_momentum = request.user_momentum
        has_momentum = user_momentum is not None
        
        logger.info(f"📊 用户 {request.user_id} 推荐请求: 候选商品={products_count}, Top-K={request.top_k}, 包含分数={request.include_scores}, 已有动量={has_momentum}")
        
        if user_momentum is None:
            logger.info(f"⚠️ 用户 {request.user_id} 没有提供动量，使用默认长期动量")
            user_momentum = recommendation_engine.get_default_long_term_momentum()
        
        # 检查并修复动量数据中的类别偏好
        if user_momentum.get('categoryPreferences') is None or len(user_momentum.get('categoryPreferences', {})) == 0:
            logger.warning(f"⚠️ 用户 {request.user_id} 的动量数据中类别偏好为空，使用默认类别偏好")
            user_momentum['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
        
        # 检查并修复价格模型
        if user_momentum.get('priceModel') is None:
            logger.warning(f"⚠️ 用户 {request.user_id} 的动量数据中价格模型为空，使用默认价格模型")
            user_momentum['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
        
        # 检查并修复质量模型
        if user_momentum.get('qualityModel') is None:
            logger.warning(f"⚠️ 用户 {request.user_id} 的动量数据中质量模型为空，使用默认质量模型")
            user_momentum['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()

        # 生成推荐
        logger.info(f"⚙️ 正在为用户 {request.user_id} 生成推荐...")
        recommendations = recommendation_engine.recommend_products(
            products,
            user_momentum,
            request.top_k
        )
        
        recommendations_count = len(recommendations)
        logger.info(f"✅ 用户 {request.user_id} 推荐生成完成: 返回 {recommendations_count} 个推荐商品")
        
        # 添加推荐得分（如果请求）
        if not request.include_scores:
            logger.info(f"🔄 移除用户 {request.user_id} 推荐结果中的分数信息")
            for rec in recommendations:
                rec.pop('recommendation_score', None)

        # 转换为Java期望的格式
        logger.info(f"🔄 正在为用户 {request.user_id} 转换推荐格式...")
        java_recommendations = mapper.extract_recommendations_from_response(recommendations)

        response_data = {
            "recommendations": java_recommendations,
            "total_count": len(java_recommendations),
            "momentum_type": user_momentum.get('type', 'unknown'),
            "algorithm_version": "2.0.0"
        }
        
        # 记录日志
        mapper.log_data_exchange(
            "recommend_products",
            {"user_id": request.user_id, "products_count": len(products), "top_k": request.top_k},
            response_data,
            request.user_id
        )
        
        logger.info(f"🎉 用户 {request.user_id} 商品推荐成功完成，返回 {len(java_recommendations)} 个商品")
        return mapper.format_response_for_java(response_data)

    except Exception as e:
        logger.error(f"❌ 用户 {request.user_id} 商品推荐失败: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"商品推荐失败: {str(e)}")

@app.post("/api/v1/three-momentum-recommendation")
async def three_momentum_recommendation(request: ThreeMomentumRequest):
    """
    使用三动量系统生成推荐
    Args:
        request: 三动量推荐请求
    Returns:
        基于三动量系统的推荐结果
    """
    logger.info(f"🔄 开始为用户 {request.user_id} 执行三动量推荐")
    try:
        # 转换数据格式
        products = [product.dict() for product in request.products]
        clicked_product = request.clicked_product.dict() if request.clicked_product else None
        
        products_count = len(products)
        has_click = clicked_product is not None
        has_long_term = request.long_term_momentum is not None
        has_push = request.current_push_momentum is not None
        
        logger.info(f"📊 用户 {request.user_id} 三动量推荐输入: 候选商品={products_count}, Top-K={request.top_k}")
        logger.info(f"📊 动量状态: 长期动量={has_long_term}, 推送动量={has_push}, 有新点击={has_click}")
        
        # 1. 获取长期动量
        long_term_momentum = request.long_term_momentum
        if long_term_momentum is None:
            logger.info(f"⚠️ 用户 {request.user_id} 没有长期动量，使用默认配置")
            long_term_momentum = recommendation_engine.get_default_long_term_momentum()
        
        # 检查并修复长期动量中的类别偏好
        if long_term_momentum.get('categoryPreferences') is None or len(long_term_momentum.get('categoryPreferences', {})) == 0:
            logger.warning(f"⚠️ 用户 {request.user_id} 的长期动量中类别偏好为空，使用默认类别偏好")
            long_term_momentum['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
        
        # 检查并修复长期动量中的价格模型
        if long_term_momentum.get('priceModel') is None:
            logger.warning(f"⚠️ 用户 {request.user_id} 的长期动量中价格模型为空，使用默认价格模型")
            long_term_momentum['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
        
        # 检查并修复长期动量中的质量模型
        if long_term_momentum.get('qualityModel') is None:
            logger.warning(f"⚠️ 用户 {request.user_id} 的长期动量中质量模型为空，使用默认质量模型")
            long_term_momentum['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        
        # 2. 获取当前推送动量
        current_push_momentum = request.current_push_momentum
        
        # 如果有当前推送动量，也检查并修复
        if current_push_momentum:
            if current_push_momentum.get('categoryPreferences') is None or len(current_push_momentum.get('categoryPreferences', {})) == 0:
                logger.warning(f"⚠️ 用户 {request.user_id} 的推送动量中类别偏好为空，使用默认类别偏好")
                current_push_momentum['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
            
            if current_push_momentum.get('priceModel') is None:
                logger.warning(f"⚠️ 用户 {request.user_id} 的推送动量中价格模型为空，使用默认价格模型")
                current_push_momentum['priceModel'] = RECOMMENDATION_CONFIG['default_price_model'].copy()
            
            if current_push_momentum.get('qualityModel') is None:
                logger.warning(f"⚠️ 用户 {request.user_id} 的推送动量中质量模型为空，使用默认质量模型")
                current_push_momentum['qualityModel'] = RECOMMENDATION_CONFIG['default_quality_model'].copy()
        
        # 3. 如果有点击，计算新的推送动量
        final_momentum = None
        if clicked_product:
            product_id = clicked_product.get('product_id', 'unknown')
            category = clicked_product.get('category', 'unknown')
            logger.info(f"📱 用户 {request.user_id} 有新点击: 商品ID={product_id}, 类别={category}")
            
            # 计算短期动量
            logger.info(f"⚙️ 正在为用户 {request.user_id} 计算短期动量...")
            short_term_momentum = recommendation_engine.compute_short_term_momentum(
                [clicked_product],  # 转换为列表格式
                [1.0]  # 默认点击强度
            )
            
            # 计算新的推送动量
            logger.info(f"⚙️ 正在为用户 {request.user_id} 更新推送动量...")
            new_push_momentum = recommendation_engine.compute_push_momentum(
                long_term_momentum,
                short_term_momentum,
                current_push_momentum
            )
            final_momentum = new_push_momentum
            logger.info(f"✅ 用户 {request.user_id} 推送动量更新完成")
        else:
            logger.info(f"ℹ️ 用户 {request.user_id} 无新点击，使用现有动量")
            # 没有点击，使用现有推送动量或长期动量
            if current_push_momentum:
                final_momentum = current_push_momentum
                logger.info(f"✅ 用户 {request.user_id} 使用现有推送动量")
            else:
                # 初始化推送动量
                logger.info(f"⚙️ 为用户 {request.user_id} 初始化推送动量...")
                empty_short_term = {'clicked_category': '', 'clicked_price': 0, 'clicked_rating': 0, 'click_strength': 0}
                final_momentum = recommendation_engine.compute_push_momentum(
                    long_term_momentum, empty_short_term, None
                )
                logger.info(f"✅ 用户 {request.user_id} 推送动量初始化完成")
        
        # 最终检查，确保最终动量的类别偏好不为空
        if final_momentum.get('categoryPreferences') is None or len(final_momentum.get('categoryPreferences', {})) == 0:
            logger.warning(f"⚠️ 用户 {request.user_id} 的最终动量中类别偏好为空，使用默认类别偏好")
            final_momentum['categoryPreferences'] = RECOMMENDATION_CONFIG['default_category_preferences'].copy()
        
        # 4. 生成推荐
        final_momentum_type = final_momentum.get('type', 'unknown')
        logger.info(f"⚙️ 正在为用户 {request.user_id} 基于 {final_momentum_type} 动量生成推荐...")
        
        # 记录最终动量的详细信息
        logger.info(f"📊 用户 {request.user_id} 最终动量详情:")
        
        # 记录类别偏好
        category_prefs = final_momentum.get('categoryPreferences', {})
        if category_prefs:
            top_categories = sorted(category_prefs.items(), key=lambda x: x[1], reverse=True)[:5]
            logger.info(f"   类别偏好 Top-5:")
            for i, (category, weight) in enumerate(top_categories):
                logger.info(f"      {i+1}. {category}: {weight:.4f} ({weight*100:.1f}%)")
        
        # 记录价格偏好
        price_model = final_momentum.get('priceModel', {})
        if price_model:
            price_mean = price_model.get('mean', 0)
            price_std = price_model.get('std', 0)
            logger.info(f"   价格偏好: 均值={price_mean:.2f}, 标准差={price_std:.2f}, 范围=[{price_mean-price_std:.2f}, {price_mean+price_std:.2f}]")
        
        # 记录质量偏好
        quality_model = final_momentum.get('qualityModel', {})
        if quality_model:
            quality_mean = quality_model.get('mean', 0)
            quality_std = quality_model.get('std', 0)
            logger.info(f"   质量偏好: 均值={quality_mean:.2f}, 标准差={quality_std:.2f}, 范围=[{max(0, quality_mean-quality_std):.2f}, {min(5, quality_mean+quality_std):.2f}]")
        
        # 记录候选商品数量和类别分布
        category_counts = {}
        for product in products:
            category = product.get('category', '')
            category_counts[category] = category_counts.get(category, 0) + 1
        
        logger.info(f"📋 用户 {request.user_id} 候选商品类别分布:")
        sorted_categories = sorted(category_counts.items(), key=lambda x: x[1], reverse=True)
        for category, count in sorted_categories:
            percentage = (count / len(products)) * 100
            logger.info(f"   - {category}: {count}个 ({percentage:.1f}%)")
        
        # 生成推荐
        recommendations = recommendation_engine.recommend_products(
            products, final_momentum, request.top_k
        )
        
        # 转换为Java期望的格式
        recommendations_count = len(recommendations)
        logger.info(f"✅ 用户 {request.user_id} 三动量推荐生成完成: 返回 {recommendations_count} 个推荐商品")
        
        # 检查得分是否相同
        scores = [rec.get('recommendation_score', 0) for rec in recommendations]
        if len(set(scores)) == 1 and len(scores) > 1:
            logger.warning(f"⚠️ 警告: 用户 {request.user_id} 所有推荐商品得分相同 ({scores[0]:.4f})，可能存在算法问题!")
        
        logger.info(f"🔄 正在为用户 {request.user_id} 转换三动量推荐格式...")
        java_recommendations = mapper.extract_recommendations_from_response(recommendations)
        
        response_data = {
            "recommendations": java_recommendations,
            "updated_momentum": mapper.handle_momentum_response(final_momentum),
            "momentum_summary": {
                "type": final_momentum.get('type', 'unknown'),
                "cumulative_clicks": final_momentum.get('cumulative_clicks', 0),
                "last_updated": final_momentum.get('last_updated'),
                "categories_count": len(final_momentum.get('categoryPreferences', {}))
            },
            "algorithm_info": {
                "version": "2.0.0",
                "momentum_formula": "推送动量 += 长期动量 + 短期动量"
            }
        }
        
        # 记录动量摘要
        momentum_summary = response_data["momentum_summary"]
        logger.info(f"📊 用户 {request.user_id} 最终动量摘要: 类型={momentum_summary['type']}, 累积点击={momentum_summary['cumulative_clicks']}, 类别数={momentum_summary['categories_count']}")
        
        # 记录推荐结果详情
        recommendations = response_data.get("recommendations", [])
        if recommendations:
            logger.info(f"🏆 用户 {request.user_id} 推荐结果分析:")
            
            # 记录Top-5推荐商品
            top_count = min(5, len(recommendations))
            if top_count > 0:
                logger.info(f"   Top-{top_count} 推荐商品:")
                for i, rec in enumerate(recommendations[:top_count]):
                    product_id = rec.get('product_id', 'unknown')
                    category = rec.get('category', 'unknown')
                    price = rec.get('price', 0)
                    score = rec.get('recommendation_score', 0)
                    logger.info(f"   {i+1}. ID={product_id}, 类别={category}, 价格={price:.2f}, 得分={score:.4f}")
            
            # 分析推荐结果的类别分布
            category_counts = {}
            for rec in recommendations:
                category = rec.get('category', 'unknown')
                category_counts[category] = category_counts.get(category, 0) + 1
            
            if category_counts:
                logger.info(f"   类别分布:")
                sorted_categories = sorted(category_counts.items(), key=lambda x: x[1], reverse=True)
                for category, count in sorted_categories:
                    percentage = (count / len(recommendations)) * 100
                    logger.info(f"   - {category}: {count}个 ({percentage:.1f}%)")
            
            # 分析价格分布
            prices = [rec.get('price', 0) for rec in recommendations if rec.get('price', 0) > 0]
            if prices:
                avg_price = sum(prices) / len(prices)
                min_price = min(prices)
                max_price = max(prices)
                logger.info(f"   价格分布: 平均={avg_price:.2f}, 范围=[{min_price:.2f}, {max_price:.2f}]")
        
        # 记录日志
        mapper.log_data_exchange(
            "three_momentum_recommendation",
            {
                "user_id": request.user_id, 
                "products_count": len(products),
                "has_click": clicked_product is not None,
                "top_k": request.top_k
            },
            response_data,
            request.user_id
        )
        
        logger.info(f"🎉 用户 {request.user_id} 三动量推荐成功完成")
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"❌ 用户 {request.user_id} 三动量推荐失败: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"三动量推荐失败: {str(e)}")

@app.post("/api/v1/calculate-product-score")
async def calculate_product_score(
    user_id: int,
    product: ProductModel, 
    momentum: Dict[str, Any]
):
    """
    计算单个商品的推荐得分
    Args:
        user_id: 用户ID
        product: 商品信息
        momentum: 用户动量
    Returns:
        商品推荐得分
    """
    try:
        # 转换商品数据格式
        product_dict = product.dict()
        
        # 计算得分
        score = recommendation_engine.calculate_product_score(product_dict, momentum)
        
        response_data = {
            "product_id": product_dict.get('product_id'),
            "recommendation_score": float(score),
            "momentum_type": momentum.get('type', 'unknown'),
            "calculation_details": {
                "category_match": momentum.get('category_preferences', {}).get(product_dict.get('category'), 0),
                "price_compatibility": "calculated",
                "quality_preference": "calculated"
            }
        }
        
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"计算商品得分失败: {e}")
        logger.error(traceback.format_exc())
        raise HTTPException(status_code=500, detail=f"计算商品得分失败: {str(e)}")

@app.get("/api/v1/get-default-momentum")
async def get_default_momentum():
    """
    获取默认长期动量配置
    Returns:
        默认动量配置
    """
    logger.info("🔄 获取默认动量配置")
    try:
        default_momentum = recommendation_engine.get_default_long_term_momentum()
        momentum_type = default_momentum.get('type', 'unknown')
        categories_count = len(default_momentum.get('category_preferences', {}))
        
        logger.info(f"✅ 默认动量获取成功: 类型={momentum_type}, 类别数={categories_count}")
        
        response_data = mapper.handle_momentum_response(default_momentum)
        
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"❌ 获取默认动量失败: {e}")
        raise HTTPException(status_code=500, detail=f"获取默认动量失败: {str(e)}")

@app.post("/api/v1/validate-product-data")
async def validate_product_data(products: List[ProductModel]):
    """
    验证商品数据的完整性
    Args:
        products: 商品列表
    Returns:
        验证结果
    """
    try:
        validation_results = []
        valid_count = 0
        
        for product in products:
            product_dict = product.dict()
            is_valid = recommendation_engine.validate_product_data(product_dict)
            
            validation_results.append({
                "product_id": product_dict.get('product_id'),
                "is_valid": is_valid,
                "issues": [] if is_valid else ["数据验证失败"]
            })
            
            if is_valid:
                valid_count += 1
        
        response_data = {
            "total_products": len(products),
            "valid_products": valid_count,
            "invalid_products": len(products) - valid_count,
            "validation_details": validation_results
        }
        
        return mapper.format_response_for_java(response_data)
        
    except Exception as e:
        logger.error(f"验证商品数据失败: {e}")
        raise HTTPException(status_code=500, detail=f"验证商品数据失败: {str(e)}")

@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """全局异常处理器"""
    request_path = str(request.url)
    request_method = request.method
    
    logger.error(f"🚨 全局异常捕获 - {request_method} {request_path}")
    logger.error(f"❌ 异常类型: {type(exc).__name__}")
    logger.error(f"❌ 异常信息: {exc}")
    logger.error("❌ 异常堆栈:")
    logger.error(traceback.format_exc())
    
    error_response = mapper.format_response_for_java(
        {
            "error": str(exc), 
            "error_type": type(exc).__name__,
            "request_path": request_path,
            "request_method": request_method
        },
        success=False
    )
    
    return JSONResponse(
        status_code=500,
        content=error_response
    )

# 启动信息
@app.on_event("startup")
async def startup_event():
    logger.info("🚀 Mall AI推荐算法微服务启动")
    logger.info("📊 专注于核心推荐算法计算")
    logger.info("🔗 数据IO由SpringBoot微服务管理")
    logger.info("⚡ 服务已就绪，等待算法计算请求...")

if __name__ == "__main__":

    uvicorn.run(
        "fastapi_service:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
