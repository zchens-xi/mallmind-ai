package com.macro.mall.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 计算长期动量请求
 * @author macro
 */
@Data
public class LongTermMomentumRequest {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 用户行为数据
     */
    @JsonProperty("user_behavior_data")
    private UserBehaviorData userBehaviorData;
}
