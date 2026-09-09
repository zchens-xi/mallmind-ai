package com.macro.mall.dto;

/**
 * 商品转化率排行DTO
 */
public class ProductConversionRankDto {
    private Long productId;
    private String productName;
    private Long viewUsers; // 浏览用户数
    private Long buyUsers; // 购买用户数
    private Double conversionRate; // 转化率(%)
    private Integer rank; // 排名

    public ProductConversionRankDto() {}

    public ProductConversionRankDto(Long productId, String productName, Long viewUsers, Long buyUsers, Double conversionRate, Integer rank) {
        this.productId = productId;
        this.productName = productName;
        this.viewUsers = viewUsers;
        this.buyUsers = buyUsers;
        this.conversionRate = conversionRate;
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

    public Long getViewUsers() {
        return viewUsers;
    }

    public void setViewUsers(Long viewUsers) {
        this.viewUsers = viewUsers;
    }

    public Long getBuyUsers() {
        return buyUsers;
    }

    public void setBuyUsers(Long buyUsers) {
        this.buyUsers = buyUsers;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
