package com.macro.mall.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 短期动量计算请求
 * 根据新的API文档更新：基于单次或多次点击行为计算即时兴趣动量，生成与长期动量结构一致的偏好分布
 * @author macro
 */
@Data
public class ShortTermMomentumRequest {

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * 点击的商品列表（支持多个商品）
     */
    @JsonProperty("clickedProducts")
    private List<ProductInfo> clickedProducts;

    /**
     * 点击强度列表，与clickedProducts一一对应
     */
    @JsonProperty("clickStrengths")
    private List<Double> clickStrengths;
}
