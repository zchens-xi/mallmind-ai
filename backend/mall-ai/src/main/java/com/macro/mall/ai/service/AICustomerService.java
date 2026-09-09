package com.macro.mall.ai.service;

import com.macro.mall.ai.model.mongo.ConversationHistory;

import java.util.List;

/**
 * AI客服服务接口
 * 定义了与AI对话历史记录相关的操作
 */
public interface AICustomerService {

    /**
     * 保存用户与AI的对话记录
     *
     * @param history 对话历史记录对象
     */
    public void saveConversation(ConversationHistory history);
    /**
     * 获取用户的历史对话
     *
     * @param userId 用户ID
     * @return 该用户的所有对话历史记录
     */
    public List<ConversationHistory> getHistoryByUserId(Long userId);

    /**
     * 获取特定会话的历史对话
     *
     * @param userId    用户ID
     * @param sessionId 会话ID
     * @return 特定会话的对话历史记录
     */
    public List<ConversationHistory> getHistoryBySessionId(Long userId, String sessionId);

    public List<ConversationHistory> getHistoryByProductId(Long userId, Long productId);

//    public ConversationHistory buildConversationHistory(Long userId, String sessionId, String question, String answer);

    ConversationHistory buildConversationHistory(Long userId, String sessionId, String question, String answer);

    /**
     * 构建对话记录对象（带商品ID）
     */
    public ConversationHistory buildConversationHistory(Long userId, String sessionId, String question, String answer, Long productId);
}
