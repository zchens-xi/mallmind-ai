package com.macro.mall.recommendation.dto;

import lombok.Data;

/**
 * 商品得分计算请求
 * @author macro
 */
@Data
public class ProductScoreRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 要计算得分的商品
     */
    private ProductInfo product;

    /**
     * 用户动量数据
     */
    private MomentumData momentum;
}
