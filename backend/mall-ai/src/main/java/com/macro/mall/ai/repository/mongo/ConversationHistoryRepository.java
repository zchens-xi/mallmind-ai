package com.macro.mall.ai.repository.mongo;

import com.macro.mall.ai.model.mongo.ConversationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史数据访问层
 * 用于AI客服功能的对话记录操作
 */
@Repository
public interface ConversationHistoryRepository extends MongoRepository<ConversationHistory, String> {

    /**
     * 根据用户ID和会话ID查询对话历史
     */
    List<ConversationHistory> findByUserIdAndSessionIdOrderByTimestampDesc(Long userId, String sessionId);

    /**
     * 根据用户ID查询最近的对话历史
     */
    List<ConversationHistory> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * 根据时间范围查询对话记录
     */
    @Query("{'timestamp': {$gte: ?0, $lte: ?1}}")
    List<ConversationHistory> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据商品ID查询相关对话
     */
    List<ConversationHistory> findByProductIds(String productId);
    /**
     * 根据情感分析结果查询对话
     */
    List<ConversationHistory> findBySentiment(String sentiment);

    /**
     * 统计用户对话数量
     */
    long countByUserId(Long userId);
}
