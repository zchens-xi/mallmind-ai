package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * 商品数据模型
 * @author macro
 */
@Data
public class ProductData {

    /**
     * 商品ID (必填)
     */
    private String productId;

    /**
     * 商品类别 (必填)
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
     * 是否新品 (默认false)
     */
    private Boolean isNew = false;

    /**
     * 品牌 (可选)
     */
    private String brand;

    /**
     * 商品名称 (可选)
     */
    private String name;

    /**
     * 商品数量 (用于订单等场景)
     */
    private Integer quantity;
}
