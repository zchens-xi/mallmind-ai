package com.macro.mall.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 商品信息DTO - 对应FastAPI的ProductModel
 * @author zchens
 */
@Data
public class ProductInfo {

    /**
     * 商品ID (必填)
     */
    @JsonProperty("productId")
    private String productId;

    /**
     * 商品分类 (必填)
     */
    private String category;

    /**
     * 商品价格 (必填, >= 0)
     */
    private Double price;

    /**
     * 商品评分 (0-5, 默认0.0)
     */
    private Double rating = 0.0;

    /**
     * 库存数量 (>= 0, 默认0)
     */
    private Integer stock = 0;

    /**
     * 是否为新品 (默认false)
     */
    @JsonProperty("isNew")
    private Boolean isNew = false;

    /**
     * 商品品牌 (可选)
     */
    private String brand;

    /**
     * 商品名称 (可选)
     */
    private String name;

    /**
     * 推荐分数 (推荐结果中返回)
     */
    @JsonProperty("recommendationScore")
    private Double recommendationScore;

    /**
     * 推荐排名 (推荐结果中返回)
     */
    private Integer rank;
}
