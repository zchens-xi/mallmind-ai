package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * 商品验证请求
 * @author macro
 */
@Data
public class ValidateProductRequest {

    /**
     * 要验证的商品列表
     */
    private List<ProductInfo> products;
}
