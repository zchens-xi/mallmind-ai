package com.macro.mall.recommendation.service;

import com.macro.mall.recommendation.dto.MomentumData;
import com.macro.mall.recommendation.dto.ProductInfo;
import com.macro.mall.recommendation.dto.RecommendationResult;
import com.macro.mall.recommendation.dto.UserBehaviorData;
import java.util.List;

public interface MomentumCalculationService {
    /**
     * 计算长期动量
     * 基于用户历史行为数据计算长期购买偏好动量
     */
    MomentumData calculateLongTermMomentum(Long userId, UserBehaviorData userBehaviorData);

    /**
     * 计算短期动量
     */
    MomentumData calculateShortTermMomentum(Long userId, List<ProductInfo> clickedProducts, List<Double> clickStrengths);

    /**
     * 计算推送动量
     */
    MomentumData calculatePushMomentum(MomentumData longTermMomentum, MomentumData shortTermMomentum, MomentumData currentPushMomentum);

    /**
     * 处理单次商品点击，更新短期动量和推送动量
     */
    MomentumData handleProductClick(Long userId, ProductInfo clickedProduct, Double clickStrength,
                                   MomentumData longTermMomentum, MomentumData currentPushMomentum);

    /**
     * 批量处理多个商品点击
     */
    MomentumData handleMultipleClicks(Long userId, List<ProductInfo> clickedProducts, List<Double> clickStrengths,
                                     MomentumData longTermMomentum, MomentumData currentPushMomentum);

    /**
     * 计算单个商品得分
     * @param productInfo 商品信息
     * @param pushMomentum 推送动量
     * @return 商品得分
     */
    Double calculateProductScore(ProductInfo productInfo, MomentumData pushMomentum);

    /**
     * 基于动量推荐商品
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @param pushMomentum 推送动量
     * @param requestCount 请求的推荐商品数量
     * @return 推荐结果
     */
    RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts, MomentumData pushMomentum, int requestCount);
}
