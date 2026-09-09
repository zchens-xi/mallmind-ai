package com.macro.mall.dto;

import java.util.List;
import java.util.Map;

/**
 * 商品分析结果封装
 */
public class ProductAnalyticsResult {
    private List<Map<String, Object>> productSalesRank; // 商品销售排行
    private List<Map<String, Object>> categorySalesPercent; // 分类销售占比
    private List<Map<String, Object>> productViewRank; // 商品访问排行
    private List<Map<String, Object>> productCollectRank; // 商品收藏排行
    private List<Map<String, Object>> productConversionRank; // 商品转化率排行
    private List<Map<String, Object>> brandRatio; // 品牌销售占比
    private List<Map<String, Object>> priceRangeRatio; // 价格区间分布

    public List<Map<String, Object>> getProductSalesRank() {
        return productSalesRank;
    }

    public void setProductSalesRank(List<Map<String, Object>> productSalesRank) {
        this.productSalesRank = productSalesRank;
    }

    public List<Map<String, Object>> getCategorySalesPercent() {
        return categorySalesPercent;
    }

    public void setCategorySalesPercent(List<Map<String, Object>> categorySalesPercent) {
        this.categorySalesPercent = categorySalesPercent;
    }

    public List<Map<String, Object>> getProductViewRank() {
        return productViewRank;
    }

    public void setProductViewRank(List<Map<String, Object>> productViewRank) {
        this.productViewRank = productViewRank;
    }

    public List<Map<String, Object>> getProductCollectRank() {
        return productCollectRank;
    }

    public void setProductCollectRank(List<Map<String, Object>> productCollectRank) {
        this.productCollectRank = productCollectRank;
    }

    public List<Map<String, Object>> getProductConversionRank() {
        return productConversionRank;
    }

    public void setProductConversionRank(List<Map<String, Object>> productConversionRank) {
        this.productConversionRank = productConversionRank;
    }

    public List<Map<String, Object>> getBrandRatio() {
        return brandRatio;
    }

    public void setBrandRatio(List<Map<String, Object>> brandRatio) {
        this.brandRatio = brandRatio;
    }

    public List<Map<String, Object>> getPriceRangeRatio() {
        return priceRangeRatio;
    }

    public void setPriceRangeRatio(List<Map<String, Object>> priceRangeRatio) {
        this.priceRangeRatio = priceRangeRatio;
    }
} 