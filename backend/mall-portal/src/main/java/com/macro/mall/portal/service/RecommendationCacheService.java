package com.macro.mall.portal.service;

import com.macro.mall.recommendation.dto.ProductInfo;
import com.macro.mall.recommendation.dto.RecommendationResult;

import java.util.List;

/**
 * 推荐结果缓存服务
 * 解决个性化推荐数量不确定的问题
 */
public interface RecommendationCacheService {

    /**
     * 从缓存获取推荐商品
     * @param userId 用户ID
     * @param requestCount 请求数量
     * @return 推荐商品列表
     */
    List<ProductInfo> getRecommendationsFromCache(Long userId, int requestCount);

    /**
     * 批量生成推荐结果并缓存
     * @param userId 用户ID
     * @param candidateProducts 候选商品池
     * @param batchSize 批量生成数量
     * @return 生成的推荐结果
     */
    RecommendationResult generateAndCacheRecommendations(Long userId, List<ProductInfo> candidateProducts, int batchSize);

    /**
     * 检查用户推荐缓存是否充足
     * @param userId 用户ID
     * @param minThreshold 最小阈值
     * @return true表示缓存充足
     */
    boolean isCacheSufficient(Long userId, int minThreshold);

    /**
     * 清理用户推荐缓存
     * @param userId 用户ID
     */
    void clearUserRecommendationCache(Long userId);

    /**
     * 获取用户缓存统计信息
     * @param userId 用户ID
     * @return 缓存统计
     */
    CacheStats getUserCacheStats(Long userId);

    /**
     * 缓存统计信息
     */
    class CacheStats {
        private int totalCached;        // 总缓存数量
        private int consumedCount;      // 已消费数量
        private long lastUpdateTime;    // 上次更新时间
        private long expireTime;        // 过期时间

        // getters and setters
        public int getTotalCached() { return totalCached; }
        public void setTotalCached(int totalCached) { this.totalCached = totalCached; }
        public int getConsumedCount() { return consumedCount; }
        public void setConsumedCount(int consumedCount) { this.consumedCount = consumedCount; }
        public long getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(long lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        public long getExpireTime() { return expireTime; }
        public void setExpireTime(long expireTime) { this.expireTime = expireTime; }
    }
}
