package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * 三动量系统推荐请求
 * @author macro
 */
@Data
public class ThreeMomentumRecommendationRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 候选商品列表
     */
    private List<ProductInfo> products;

    /**
     * 长期动量数据
     */
    private MomentumData longTermMomentum;

    /**
     * 当前推送动量数据
     */
    private MomentumData currentPushMomentum;

    /**
     * 点击的商品
     */
    private ProductInfo clickedProduct;

    /**
     * 返回推荐数量 (1-100)
     */
    private Integer topK = 10;
}
