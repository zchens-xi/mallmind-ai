package com.macro.mall.recommendation.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 推荐商品项
 * @author macro
 */
@Data
public class RecommendedProduct {

    /**
     * 商品ID
     */
    private String productId;

    /**
     * 商品类别
     */
    private String category;

    /**
     * 商品价格
     */
    private Double price;

    /**
     * 商品评分
     */
    private Double rating;

    /**
     * 商品品牌
     */
    private String brand;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 推荐得分 (可选)
     */
    private Double recommendationScore;

    /**
     * 推荐排名
     */
    private Integer rank;
}
