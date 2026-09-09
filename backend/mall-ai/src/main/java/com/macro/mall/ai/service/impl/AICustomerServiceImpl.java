//Dao,domain,service放在mall-ai，controller放在mall-portal
package com.macro.mall.ai.service.impl;

import com.macro.mall.ai.model.mongo.ConversationHistory;
import com.macro.mall.ai.repository.mongo.ConversationHistoryRepository;
import com.macro.mall.ai.service.AICustomerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI客服服务实现类
 * 负责对话历史记录的管理和存储
 */
@Slf4j
@Service("AICustomerService")
public class AICustomerServiceImpl implements AICustomerService {

    @Autowired
    private ConversationHistoryRepository conversationRepo;

    /**
     * 保存用户与AI的对话记录
     */
    @Override
    public void saveConversation(ConversationHistory history) {
        try {
            // 参数验证
            validateConversationHistory(history);

            // 设置默认时间戳
            if (history.getTimestamp() == null) {
                history.setTimestamp(LocalDateTime.now());
            }

            log.info("准备保存对话记录: userId={}, sessionId={}, productId={}",
                    history.getUserId(), history.getSessionId(), history.getProductId());

            ConversationHistory saved = conversationRepo.save(history);

            log.info("对话记录保存成功: id={}, userId={}", saved.getId(), saved.getUserId());
        } catch (Exception e) {
            log.error("保存对话记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("保存对话记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证对话历史记录的必要字段
     */
    private void validateConversationHistory(ConversationHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("对话记录对象不能为空");
        }
        if (history.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(history.getSessionId())) {
            throw new IllegalArgumentException("会话ID不能为空");
        }
        if (!StringUtils.hasText(history.getUserMessage()) && !StringUtils.hasText(history.getAiResponse())) {
            throw new IllegalArgumentException("用户消息或AI回复至少需要一个不为空");
        }
    }

    /**
     * 获取用户的历史对话
     */
    @Override
    public List<ConversationHistory> getHistoryByUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        log.debug("查询用户历史对话: userId={}", userId);
        List<ConversationHistory> histories = conversationRepo.findByUserIdOrderByTimestampDesc(userId);
        log.debug("找到 {} 条历史对话记录", histories.size());

        return histories;
    }

    /**
     * 获取特定会话的历史对话
     */
    @Override
    public List<ConversationHistory> getHistoryBySessionId(Long userId, String sessionId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(sessionId)) {
            throw new IllegalArgumentException("会话ID不能为空");
        }

        log.debug("查询会话历史对话: userId={}, sessionId={}", userId, sessionId);
        List<ConversationHistory> histories = conversationRepo.findByUserIdAndSessionIdOrderByTimestampDesc(userId, sessionId);
        log.debug("找到 {} 条会话对话记录", histories.size());

        return histories;
    }

    /**
     * 根据产品ID和用户ID获取特定会话的历史对话
     */
    @Override
    public List<ConversationHistory> getHistoryByProductId(Long userId, Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("商品ID不能为空");
        }

        log.debug("查询商品相关历史对话: userId={}, productId={}", userId, productId);
        List<ConversationHistory> histories = conversationRepo.findByProductIds(String.valueOf(productId));
        log.debug("找到 {} 条商品相关对话记录", histories.size());

        return histories;
    }

    /**
     * 构建基础对话记录对象
     */
    @Override
    public ConversationHistory buildConversationHistory(Long userId, String sessionId, String question, String answer) {
        return buildConversationHistoryInternal(userId, sessionId, question, answer, null);
    }

    /**
     * 构建带商品ID的对话记录对象
     */
    @Override
    public ConversationHistory buildConversationHistory(Long userId, String sessionId, String question, String answer, Long productId) {
        return buildConversationHistoryInternal(userId, sessionId, question, answer, productId);
    }

    /**
     * 内部方法：构建对话记录对象
     */
    private ConversationHistory buildConversationHistoryInternal(Long userId, String sessionId, String question, String answer, Long productId) {
        ConversationHistory history = new ConversationHistory();

        // 设置基础信息
        history.setUserId(userId);
        history.setSessionId(sessionId);
        history.setUserMessage(question);
        history.setAiResponse(answer);
        history.setTimestamp(LocalDateTime.now());

        // 设置默认值
        history.setMessageType("text");
        history.setSentiment("neutral");
        history.setConfidence(0.95);

        // 设置商品相关信息
        if (productId != null) {
            history.setProductId(String.valueOf(productId));
            history.setProductIds(String.valueOf(productId));
        }

        log.debug("构建对话记录: userId={}, sessionId={}, productId={}", userId, sessionId, productId);

        return history;
    }

    /**
     * 获取用户对话数量统计
     */
    public long getConversationCount(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        long count = conversationRepo.countByUserId(userId);
        log.debug("用户 {} 的对话记录数量: {}", userId, count);

        return count;
    }
}
