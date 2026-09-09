package com.macro.mall.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付方式分布DTO
 */
@Data
public class PaymentDistributionDto {

    @ApiModelProperty("支付方式名称")
    private String name;

    @ApiModelProperty("该支付方式的订单数量")
    private Long value;

    @ApiModelProperty("占比百分比")
    private Double percentage;
}
