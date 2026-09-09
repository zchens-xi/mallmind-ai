package com.macro.mall.dto;

/**
 * 商品库存分布DTO
 */
public class ProductStockDistributionDto {
    private String range; // 库存区间
    private Long count; // 商品数量
    private Double percent; // 占比(%)
    private String status; // 库存状态

    public ProductStockDistributionDto() {}

    public ProductStockDistributionDto(String range, Long count, Double percent, String status) {
        this.range = range;
        this.count = count;
        this.percent = percent;
        this.status = status;
    }

    public String getRange() {
        return range;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
