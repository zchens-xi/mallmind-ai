package com.macro.mall.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 推送动量计算请求
 * 根据新的API文档更新：融合长期动量和短期动量，生成用于推荐的累积动量
 * @author zchens
 */
@Data
public class PushMomentumRequest {

    /**
     * 用户ID
     */
    @JsonProperty("userId")
    private Long userId;

    /**
     * 长期动量数据
     */
    @JsonProperty("longTermMomentum")
    private MomentumData longTermMomentum;

    /**
     * 短期动量数据
     */
    @JsonProperty("shortTermMomentum")
    private MomentumData shortTermMomentum;

    /**
     * 当前推送动量数据
     */
    @JsonProperty("currentPushMomentum")
    private MomentumData currentPushMomentum;
}
