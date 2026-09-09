package com.macro.mall.dto;

/**
 * 商品浏览排行DTO
 */
public class ProductViewRankDto {
    private Long productId;
    private String productName;
    private Long viewCount; // 浏览量
    private Long uniqueVisitors; // 独立访客数
    private Integer rank; // 排名

    public ProductViewRankDto() {}

    public ProductViewRankDto(Long productId, String productName, Long viewCount, Long uniqueVisitors, Integer rank) {
        this.productId = productId;
        this.productName = productName;
        this.viewCount = viewCount;
        this.uniqueVisitors = uniqueVisitors;
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

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Long getUniqueVisitors() {
        return uniqueVisitors;
    }

    public void setUniqueVisitors(Long uniqueVisitors) {
        this.uniqueVisitors = uniqueVisitors;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }
}
