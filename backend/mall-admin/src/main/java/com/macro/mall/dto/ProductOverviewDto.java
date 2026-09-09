package com.macro.mall.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品概览数据DTO
 */
public class ProductOverviewDto {
    private Long totalProducts; // 商品总数
    private Long onSaleProducts; // 在售商品数
    private Long totalViews; // 总浏览量
    private Double viewsPerUser; // 人均浏览量
    private Long totalCollects; // 总收藏量
    private Double collectRate; // 收藏率(%)
    private Double avgConversionRate; // 平均转化率(%)
    private Double cartRate; // 加购率(%)
    private List<CategorySalesPercent> categorySalesPercent; // 商品分类销售占比

    public static class CategorySalesPercent {
        private Long categoryId;
        private String categoryName;
        private BigDecimal salesAmount;
        private Double percent;

        public CategorySalesPercent() {}

        public CategorySalesPercent(Long categoryId, String categoryName, BigDecimal salesAmount, Double percent) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.salesAmount = salesAmount;
            this.percent = percent;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public void setCategoryId(Long categoryId) {
            this.categoryId = categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public BigDecimal getSalesAmount() {
            return salesAmount;
        }

        public void setSalesAmount(BigDecimal salesAmount) {
            this.salesAmount = salesAmount;
        }

        public Double getPercent() {
            return percent;
        }

        public void setPercent(Double percent) {
            this.percent = percent;
        }
    }

    public Long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(Long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public Long getOnSaleProducts() {
        return onSaleProducts;
    }

    public void setOnSaleProducts(Long onSaleProducts) {
        this.onSaleProducts = onSaleProducts;
    }

    public Long getTotalViews() {
        return totalViews;
    }

    public void setTotalViews(Long totalViews) {
        this.totalViews = totalViews;
    }

    public Double getViewsPerUser() {
        return viewsPerUser;
    }

    public void setViewsPerUser(Double viewsPerUser) {
        this.viewsPerUser = viewsPerUser;
    }

    public Long getTotalCollects() {
        return totalCollects;
    }

    public void setTotalCollects(Long totalCollects) {
        this.totalCollects = totalCollects;
    }

    public Double getCollectRate() {
        return collectRate;
    }

    public void setCollectRate(Double collectRate) {
        this.collectRate = collectRate;
    }

    public Double getAvgConversionRate() {
        return avgConversionRate;
    }

    public void setAvgConversionRate(Double avgConversionRate) {
        this.avgConversionRate = avgConversionRate;
    }

    public Double getCartRate() {
        return cartRate;
    }

    public void setCartRate(Double cartRate) {
        this.cartRate = cartRate;
    }

    public List<CategorySalesPercent> getCategorySalesPercent() {
        return categorySalesPercent;
    }

    public void setCategorySalesPercent(List<CategorySalesPercent> categorySalesPercent) {
        this.categorySalesPercent = categorySalesPercent;
    }
}
