package com.macro.mall.recommendation.dto;

import lombok.Data;
import java.util.List;

/**
 * 商品验证响应
 * @author macro
 */
@Data
public class ProductValidationResponse {

    /**
     * 总商品数量
     */
    private Integer totalProducts;

    /**
     * 有效商品数量
     */
    private Integer validProducts;

    /**
     * 无效商品数量
     */
    private Integer invalidProducts;

    /**
     * 验证详情列表
     */
    private List<ValidationDetail> validationDetails;

    @Data
    public static class ValidationDetail {
        /**
         * 商品ID
         */
        private String productId;

        /**
         * 是否有效
         */
        private Boolean isValid;

        /**
         * 问题列表
         */
        private List<String> issues;
    }
}
