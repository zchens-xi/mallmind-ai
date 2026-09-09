package com.macro.mall.ai.repository.mongo;

import com.macro.mall.ai.model.mongo.UserBehavior;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户行为数据访问层
 * 用于AI导购和智能推荐的用户行为分析
 */
@Repository
public interface UserBehaviorRepository extends MongoRepository<UserBehavior, String> {

    /**
     * 根据用户ID查询行为记录
     */
    List<UserBehavior> findByUserIdOrderByTimestampDesc(Long userId);

    /**
     * 根据用户ID和行为类型查询
     */
    List<UserBehavior> findByUserIdAndBehaviorType(Long userId, String behaviorType);

    /**
     * 根据商品ID查询相关行为
     */
    List<UserBehavior> findByProductIdOrderByTimestampDesc(Long productId);

    /**
     * 根据商品分类查询用户行为
     */
    List<UserBehavior> findByProductCategoryAndBehaviorType(String productCategory, String behaviorType);

    /**
     * 查询用户在指定时间范围内的行为
     */
    @Query("{'userId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<UserBehavior> findByUserIdAndTimestampBetween(Long userId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计用户对某个商品的行为次数
     */
    long countByUserIdAndProductIdAndBehaviorType(Long userId, Long productId, String behaviorType);

    /**
     * 查询热门商品（基于行为频次）
     */
    @Query(value = "{'behaviorType': ?0}", fields = "{'productId': 1}")
    List<UserBehavior> findPopularProducts(String behaviorType);
}
