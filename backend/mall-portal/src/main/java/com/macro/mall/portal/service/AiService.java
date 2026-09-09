package com.macro.mall.portal.service;

import com.macro.mall.ai.model.mongo.ConversationHistory;

import java.util.List;

public interface AiService {
    Long getCurrentUserId();

    String askDeepSeek(String question) throws Exception;
    // TODO： gemini的先不改

    List<ConversationHistory> hello();

    String getProductInfo(Long productId);

    String prompt(String question, Long productId);

    List<ConversationHistory> getHistory(Long productId);

    // TODO：暂时默认使用Kimi
    String askCustomer(String question, Long productId) throws Exception;
}
