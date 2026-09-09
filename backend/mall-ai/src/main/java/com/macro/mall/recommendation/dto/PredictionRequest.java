package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * Python模型预测请求DTO
 * @author macro
 */
@Data
public class PredictionRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商品特征数据
     */
    private List<Double> features;

    /**
     * 用户历史行为数据
     */
    private List<Long> userHistory;

    /**
     * 预测类型 (recommendation, classification, etc.)
     */
    private String predictionType;

    /**
     * 返回结果数量
     */
    private Integer topK;
}
