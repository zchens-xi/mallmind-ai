package com.macro.mall.recommendation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 推荐商品请求
 * @author macro
 */
@Data
public class RecommendProductsRequest {

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 候选商品列表
     */
    private List<ProductInfo> products;

    /**
     * 用户动量数据
     */
    @JsonProperty("user_momentum")
    private MomentumData userMomentum;

    /**
     * 返回推荐数量 (1-100)
     */
    @JsonProperty("top_k")
    private Integer topK = 10;

    /**
     * 是否包含推荐得分
     */
    @JsonProperty("include_scores")
    private Boolean includeScores = true;
}
