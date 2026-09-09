package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * 推荐商品响应数据
 * @author macro
 */
@Data
public class RecommendationData {

    /**
     * 推荐商品列表
     */
    private List<RecommendedProduct> recommendations;

    /**
     * 总数量
     */
    private Integer totalCount;

    /**
     * 动量类型
     */
    private String momentumType;

    /**
     * 算法版本
     */
    private String algorithmVersion;

    /**
     * 更新后的动量数据
     */
    private MomentumData updatedMomentum;

    /**
     * 动量摘要
     */
    private MomentumSummary momentumSummary;

    /**
     * 算法信息
     */
    private AlgorithmInfo algorithmInfo;

    /**
     * 推荐商品
     */
    @Data
    public static class RecommendedProduct {
        private String productId;
        private String category;
        private Double price;
        private Double rating;
        private String brand;
        private String name;
        private Double recommendationScore;
        private Integer rank;
    }

    /**
     * 动量摘要
     */
    @Data
    public static class MomentumSummary {
        private String type;
        private Integer cumulativeClicks;
        private String lastUpdated;
        private Integer categoriesCount;
    }

    /**
     * 算法信息
     */
    @Data
    public static class AlgorithmInfo {
        private String version;
        private String momentumFormula;
    }
}
