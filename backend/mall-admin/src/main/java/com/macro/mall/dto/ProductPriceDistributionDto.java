package com.macro.mall.dto;

/**
 * 商品价格区间分布DTO
 */
public class ProductPriceDistributionDto {
    private String range; // 价格区间
    private Long count; // 商品数量
    private Double percent; // 占比(%)

    public ProductPriceDistributionDto() {}

    public ProductPriceDistributionDto(String range, Long count, Double percent) {
        this.range = range;
        this.count = count;
        this.percent = percent;
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
}
