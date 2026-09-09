package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * Python模型预测响应DTO
 * @author macro
 */
@Data
public class PredictionResponse {

    /**
     * 预测结果状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 预测结果数据
     */
    private PredictionData data;

    @Data
    public static class PredictionData {
        /**
         * 推荐商品ID列表
         */
        private List<Long> recommendedProducts;

        /**
         * 预测分数列表
         */
        private List<Double> scores;

        /**
         * 预测概率
         */
        private Double probability;

        /**
         * 模型版本
         */
        private String modelVersion;

        /**
         * 预测耗时(毫秒)
         */
        private Long processingTime;
    }
}
