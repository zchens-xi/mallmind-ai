package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * 商品得分计算响应
 * @author macro
 */
@Data
public class ProductScoreResponse {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 推荐得分
     */
    private Double recommendationScore;

    /**
     * 动量类型
     */
    private String momentumType;

    /**
     * 计算详情
     */
    private CalculationDetails calculationDetails;

    @Data
    public static class CalculationDetails {
        /**
         * 类别匹配度
         */
        private Double categoryMatch;

        /**
         * 价格兼容性
         */
        private String priceCompatibility;

        /**
         * 质量偏好
         */
        private String qualityPreference;
    }
}
