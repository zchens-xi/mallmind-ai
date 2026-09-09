package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 销售数据分析DTO
 */
@Data
public class SalesAnalyticsDto {

    @ApiModelProperty("销售总额")
    private BigDecimal totalSales;

    @ApiModelProperty("订单总数")
    private Long totalOrderCount;

    @ApiModelProperty("客单价")
    private BigDecimal avgOrderAmount;

    @ApiModelProperty("销售额同比增长率(%)")
    private Double salesGrowthRate;

    @ApiModelProperty("订单量同比增长率(%)")
    private Double orderGrowthRate;

    @ApiModelProperty("客单价同比增长率(%)")
    private Double avgOrderAmountGrowthRate;

    @ApiModelProperty("退款率(%)")
    private Double refundRate;

    @ApiModelProperty("退款金额")
    private BigDecimal refundAmount;

    @ApiModelProperty("日期列表")
    private List<String> dateList;

    @ApiModelProperty("对应日期的销售额列表")
    private List<BigDecimal> salesList;

    @ApiModelProperty("对应日期的订单量列表")
    private List<Long> orderCountList;
}
