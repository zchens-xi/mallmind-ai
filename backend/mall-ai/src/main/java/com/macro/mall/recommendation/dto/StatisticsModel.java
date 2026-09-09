package com.macro.mall.recommendation.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * 统计模型数据结构
 * 支持最新FastAPI接口文档v2.0的卡方拟合信息
 * @author macro
 */
@Data
public class StatisticsModel {

    /**
     * 均值
     */
    private Double mean;

    /**
     * 标准差
     */
    private Double std;

    /**
     * 偏度
     */
    private Double skewness;

    /**
     * 最小值
     */
    private Double min;

    /**
     * 最大值
     */
    private Double max;

    /**
     * 中位数
     */
    private Double median;

    /**
     * 数据点数量
     */
    private Integer count;

    /**
     * 卡方拟合信息（可选）
     */
    @JsonProperty("_chi_fit_info")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ChiFitInfo chiFitInfo;

    @Data
    public static class ChiFitInfo {
        /**
         * 拟合方法: short_term_distribution_chi_fitting | distribution_based_traditional_update | trend_based_chi_fitting
         */
        private String method;

        /**
         * 拟合优度 (KS检验p值，0-1)
         */
        private Double goodnessOfFit;

        /**
         * 动态调整因子 (1.0-2.0)
         */
        private Double expansionFactor;

        /**
         * 用于拟合的数据点数量
         */
        private Integer dataPoints;

        /**
         * 累积点击数（仅推送动量）
         */
        private Integer cumulativeClicks;
    }
}
