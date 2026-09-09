package com.macro.mall.ai.model.mongo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * AI对话记录模型 - 存储在MongoDB中
 * 用于AI客服功能的对话历史记录
 */
@Data
@Document(collection = "conversation_history")
public class ConversationHistory {

    @Id
    private String id;

    private Long userId;

    private String sessionId;

    private String productId; // 关联的商品ID

    private String userMessage;

    private String aiResponse;

    private String messageType; // text, image, product_link

    private LocalDateTime timestamp;

    private String sentiment; // positive, negative, neutral

    private List<String> keywords;

    private String productIds; // 相关商品ID

    private Double confidence; // AI回复置信度
}
