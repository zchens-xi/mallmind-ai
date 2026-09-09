package com.macro.mall.recommendation.dto;

import lombok.Data;

import java.util.List;

/**
 * 推荐结果DTO - 对应FastAPI推荐响应
 * @author macro
 */
@Data
public class RecommendationResult {

    /**
     * 推荐的商品列表
     */
    private List<RecommendedProduct> recommendations;

    /**
     * 推荐商品总数
     */
    private Integer totalCount;

    /**
     * 使用的动量类型
     */
    private String momentumType;

    /**
     * 算法版本
     */
    private String algorithmVersion;

    /**
     * 更新后的动量数据 (三动量推荐时返回)
     */
    private MomentumData updatedMomentum;

    /**
     * 动量摘要信息
     */
    private MomentumSummary momentumSummary;

    /**
     * 算法信息
     */
    private AlgorithmInfo algorithmInfo;

    @Data
    public static class MomentumSummary {
        /**
         * 动量类型
         */
        private String type;

        /**
         * 累积点击数
         */
        private Integer cumulativeClicks;

        /**
         * 最后更新时间
         */
        private String lastUpdated;

        /**
         * 类别数量
         */
        private Integer categoriesCount;
    }

    @Data
    public static class AlgorithmInfo {
        /**
         * 算法版本
         */
        private String version;

        /**
         * 动量计算公式
         */
        private String momentumFormula;
    }
}