package com.macro.mall.portal.service;

import com.macro.mall.recommendation.dto.RecommendationResult;
import com.macro.mall.recommendation.dto.UserBehaviorData;
import com.macro.mall.recommendation.dto.ProductInfo;
import java.util.List;

public interface PushService {

    UserBehaviorData getUserBehaviorData(Long userId);

    /**
     * 更新短期动量
     * @param userId 用户ID
     * @param clickedProduct 点击的商品信息
     * @param clickStrength 点击强度 (0.1-2.0)
     */
    void renewShortTermMomentum(Long userId, ProductInfo clickedProduct, Double clickStrength);

    /**
     * 更新推送动量
     * @param userId 用户ID
     * @param clickedProduct 点击的商品信息
     * @param clickStrength 点击强度 (0.1-2.0)
     */
    void renewPushMomentum(Long userId, ProductInfo clickedProduct, Double clickStrength);

    /**
     * 基于动量推荐商品
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @return 推荐结果，包含排序后的商品列表
     */
    RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts);

    /**
     * 基于动量推荐商品（支持指定数量）
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @param requestCount 请求的推荐商品数量
     * @return 推荐结果，包含排序后的商品列表
     */
    RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts, int requestCount);

    void renewLongTermMomentum(Long userId);
}
