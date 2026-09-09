package com.macro.mall.recommendation.client;

import com.macro.mall.recommendation.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * FastAPI推荐算法微服务Feign客户端 v2.0
 * 完全对接最新的FastAPI微服务接口文档
 * @author zchens
 */
@FeignClient(
    name = "fastapi-recommendation-service",
    url = "${python.fastapi.service.url:http://100.64.0.2:8000}",
    configuration = PythonModelFeignConfig.class
)
public interface PythonModelClient {

    // ==================== 1. 基础服务接口 ====================

    /**
     * 根路径健康检查
     * GET /
     */
    @GetMapping("/")
    FastApiResponse<ServiceStatusResponse> getServiceStatus();

    /**
     * 详细健康检查
     * GET /health
     */
    @GetMapping("/health")
    FastApiResponse<HealthResponse> healthCheck();

    // ==================== 2. 动量计算接口 ====================

    /**
     * 计算长期动量
     * POST /api/v1/calculate-long-term-momentum
     * 基于用户历史行为数据计算长期购买偏好动量
     */
    @PostMapping("/api/v1/calculate-long-term-momentum")
    FastApiResponse<MomentumData> calculateLongTermMomentum(@RequestBody LongTermMomentumRequest request);

    /**
     * 计算短期动量
     * POST /api/v1/calculate-short-term-momentum
     * 基于单次或多次点击行为计算即时兴趣动量，生成与长期动量结构一致的偏好分布
     */
    @PostMapping("/api/v1/calculate-short-term-momentum")
    FastApiResponse<MomentumData> calculateShortTermMomentum(@RequestBody ShortTermMomentumRequest request);

    /**
     * 计算推送动量
     * POST /api/v1/calculate-push-momentum
     * 融合长期动量和短期动量，生成用于推荐的累积动量
     */
    @PostMapping("/api/v1/calculate-push-momentum")
    FastApiResponse<MomentumData> calculatePushMomentum(@RequestBody PushMomentumRequest request);

    // ==================== 3. 推荐服务接口 ====================

    /**
     * 基于动量推荐商品
     * POST /api/v1/recommend-products
     * 使用已有动量对候选商品进行推荐排序
     */
    @PostMapping("/api/v1/recommend-products")
    FastApiResponse<RecommendationResult> recommendProducts(@RequestBody RecommendProductsRequest request);

    /**
     * 三动量系统推荐
     * POST /api/v1/three-momentum-recommendation
     * 完整的三动量系统，实时计算并更新推送动量，生成个性化推荐
     */
    @PostMapping("/api/v1/three-momentum-recommendation")
    FastApiResponse<ThreeMomentumRecommendationResponse> generateThreeMomentumRecommendation(@RequestBody ThreeMomentumRecommendationRequest request);

    // ==================== 4. 辅助工具接口 ====================

    /**
     * 计算单个商品得分
     * POST /api/v1/calculate-product-score
     * 计算特定商品在给定用户动量下的推荐得分
     */
    @PostMapping("/api/v1/calculate-product-score")
    FastApiResponse<ProductScoreResponse> calculateProductScore(@RequestBody ProductScoreRequest request);

    /**
     * 获取默认动量配置
     * GET /api/v1/get-default-momentum
     * 获取系统默认的长期动量配置，用于新用户或无历史数据用户
     */
    @GetMapping("/api/v1/get-default-momentum")
    FastApiResponse<MomentumData> getDefaultMomentum();

    /**
     * 验证商品数据
     * POST /api/v1/validate-product-data
     * 验证商品数据的完整性和有效性
     */
    @PostMapping("/api/v1/validate-product-data")
    FastApiResponse<ProductValidationResponse> validateProductData(@RequestBody ValidateProductRequest request);
}
