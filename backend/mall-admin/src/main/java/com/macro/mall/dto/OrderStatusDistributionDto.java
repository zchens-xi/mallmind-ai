package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 订单状态分布DTO
 */
@Data
public class OrderStatusDistributionDto {

    @ApiModelProperty("订单状态名称")
    private String name;

    @ApiModelProperty("该状态的订单数量")
    private Long value;

    @ApiModelProperty("占比百分比")
    private Double percentage;
}
