package com.macro.mall.dto;

import java.math.BigDecimal;

/**
 * 商品销售排行DTO
 */
public class ProductSalesRankDto {
    private Long productId;
    private String productName;
    private BigDecimal salesAmount; // 销售额
    private Integer salesQuantity; // 销售数量
    private Integer rank; // 排名

    public ProductSalesRankDto() {}

    public ProductSalesRankDto(Long productId, String productName, BigDecimal salesAmount, Integer salesQuantity, Integer rank) {
        this.productId = productId;
        this.productName = productName;
        this.salesAmount = salesAmount;
        this.salesQuantity = salesQuantity;
        this.rank = rank;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public Integer getSalesQuantity() {
        return salesQuantity;
    }

    public void setSalesQuantity(Integer salesQuantity) {
        this.salesQuantity = salesQuantity;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
