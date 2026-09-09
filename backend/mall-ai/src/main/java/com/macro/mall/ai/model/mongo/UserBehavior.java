package com.macro.mall.ai.model.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 用户行为数据模型 - 存储在MongoDB中
 * 用于AI导购和智能推荐的用户画像分析
 */
@Data
@Document(collection = "user_behavior")
public class UserBehavior {

    @Id
    private String id;

    private Long userId;

    private String behaviorType; // view, click, purchase, cart, favorite

    private Long productId;

    private String productCategory;

    private String productBrand;

    private Double productPrice;

    private LocalDateTime timestamp;

    private String deviceType; // mobile, pc, tablet

    private String source; // search, recommendation, direct

    private Long duration; // 停留时间（秒）

    private Map<String, Object> context; // 上下文信息
}
