package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 销售数据分析结果
 */
@Getter
@Setter
public class AnalyticsDataResult {
    
    @ApiModelProperty("日期列表")
    private List<String> dateList;
    
    @ApiModelProperty("销售额列表")
    private List<Double> salesList;
    
    @ApiModelProperty("订单数量列表")
    private List<Long> orderCountList;
    
    @ApiModelProperty("销售总额")
    private Double totalSales;
    
    @ApiModelProperty("订单总数")
    private Long totalOrderCount;
    
    @ApiModelProperty("客单价")
    private Double avgOrderAmount;
    
    @ApiModelProperty("同比增长率")
    private Double salesGrowthRate;
} 