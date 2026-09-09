package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.service.IncrementalSyncService;
import com.macro.mall.portal.service.PushService;
import com.macro.mall.portal.service.RecommendationCacheService;
import com.macro.mall.recommendation.dto.ProductInfo;
import com.macro.mall.recommendation.dto.RecommendationResult;
import com.macro.mall.recommendation.dto.RecommendedProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推荐结果缓存服务实现
 * 核心解决：个性化推荐数量不确定的问题
 */
@Service
@Slf4j
public class RecommendationCacheServiceImpl implements RecommendationCacheService {

    @Autowired
    private PushService pushService;

    @Autowired
    private IncrementalSyncService incrementalSyncService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    // Redis Key 设计
    private static final String USER_RECOMMENDATION_POOL_KEY = "user:recommendation:pool:{}";
    private static final String USER_RECOMMENDATION_STATS_KEY = "user:recommendation:stats:{}";
    private static final String GLOBAL_SYNC_TIME_KEY = "recommendation:sync:time";

    // 配置参数
    private static final int DEFAULT_BATCH_SIZE = 50;           // 默认批量生成数量
    private static final int MIN_CACHE_THRESHOLD = 10;          // 最小缓存阈值
    private static final int CACHE_EXPIRE_HOURS = 2;            // 缓存过期时间2小时
    private static final int MAX_CANDIDATE_SIZE = 100;          // 最大候选商品数

    @Override
    public List<ProductInfo> getRecommendationsFromCache(Long userId, int requestCount) {
        try {
            log.info("从缓存获取用户{}的{}个推荐商品", userId, requestCount);

            String poolKey = buildPoolKey(userId);

            if (redisTemplate == null) {
                log.warn("Redis不可用，直接生成推荐结果");
                return generateRecommendationsDirectly(userId, requestCount);
            }

            // 从Redis List中取出指定数量的推荐结果
            List<Object> cachedResults = redisTemplate.opsForList().range(poolKey, 0, requestCount - 1);

            if (CollectionUtils.isEmpty(cachedResults) || cachedResults.size() < requestCount) {
                log.info("缓存不足，当前缓存{}个，需要{}个，触发批量生成",
                        cachedResults != null ? cachedResults.size() : 0, requestCount);

                // 缓存不足，批量生成新的推荐结果
                generateAndCacheRecommendations(userId, null, DEFAULT_BATCH_SIZE);

                // 重新获取
                cachedResults = redisTemplate.opsForList().range(poolKey, 0, requestCount - 1);
            }

            if (!CollectionUtils.isEmpty(cachedResults)) {
                // 从缓存中移除已消费的数据
                redisTemplate.opsForList().trim(poolKey, requestCount, -1);

                // 转换为ProductInfo列表
                List<ProductInfo> result = cachedResults.stream()
                        .map(obj -> (ProductInfo) obj)
                        .collect(Collectors.toList());

                // 更新统计信息
                updateCacheStats(userId, result.size());

                log.info("成功从缓存返回{}个推荐商品", result.size());
                return result;
            }

            // 缓存依然不足，降级处理
            log.warn("缓存生成失败，使用降级策略");
            return generateRecommendationsDirectly(userId, requestCount);

        } catch (Exception e) {
            log.error("从缓存获取推荐失败，使用降级策略", e);
            return generateRecommendationsDirectly(userId, requestCount);
        }
    }

    @Override
    public RecommendationResult generateAndCacheRecommendations(Long userId, List<ProductInfo> candidateProducts, int batchSize) {
        try {
            log.info("为用户{}生成并缓存{}个推荐结果", userId, batchSize);

            // 1. 获取候选商品池
            if (candidateProducts == null) {
                candidateProducts = getCandidateProducts();
            }

            if (CollectionUtils.isEmpty(candidateProducts)) {
                log.warn("候选商品池为空，无法生成推荐");
                return null;
            }

            log.info("使用{}个候选商品生成推荐", candidateProducts.size());

            // 2. 调用个性化推荐服务
            RecommendationResult recommendationResult = pushService.recommendProducts(userId, candidateProducts);

            if (recommendationResult == null || CollectionUtils.isEmpty(recommendationResult.getRecommendations())) {
                log.warn("个性化推荐返回空结果");
                return null;
            }

            List<RecommendedProduct> recommendations = recommendationResult.getRecommendations();
            log.info("个性化推荐返回{}个商品", recommendations.size());

            // 3. 转换为ProductInfo并缓存
            List<ProductInfo> productInfos = recommendations.stream()
                    .map(this::convertToProductInfo)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(productInfos)) {
                cacheRecommendations(userId, productInfos);
                log.info("成功缓存{}个推荐结果", productInfos.size());
            }

            return recommendationResult;

        } catch (Exception e) {
            log.error("生成并缓存推荐失败", e);
            return null;
        }
    }

    @Override
    public boolean isCacheSufficient(Long userId, int minThreshold) {
        try {
            if (redisTemplate == null) {
                return false;
            }

            String poolKey = buildPoolKey(userId);
            Long cacheSize = redisTemplate.opsForList().size(poolKey);
            boolean sufficient = cacheSize != null && cacheSize >= minThreshold;

            log.debug("用户{}缓存检查：当前{}个，阈值{}个，充足：{}",
                    userId, cacheSize, minThreshold, sufficient);

            return sufficient;

        } catch (Exception e) {
            log.warn("检查缓存充足性失败", e);
            return false;
        }
    }

    @Override
    public void clearUserRecommendationCache(Long userId) {
        try {
            if (redisTemplate == null) {
                return;
            }

            String poolKey = buildPoolKey(userId);
            String statsKey = buildStatsKey(userId);

            redisTemplate.delete(poolKey);
            redisTemplate.delete(statsKey);

            log.info("清理用户{}的推荐缓存", userId);

        } catch (Exception e) {
            log.error("清理用户缓存失败", e);
        }
    }

    @Override
    public CacheStats getUserCacheStats(Long userId) {
        try {
            if (redisTemplate == null) {
                return new CacheStats();
            }

            String poolKey = buildPoolKey(userId);
            String statsKey = buildStatsKey(userId);

            Long totalCached = redisTemplate.opsForList().size(poolKey);
            Object statsObj = redisTemplate.opsForValue().get(statsKey);

            CacheStats stats = new CacheStats();
            stats.setTotalCached(totalCached != null ? totalCached.intValue() : 0);
            stats.setLastUpdateTime(System.currentTimeMillis());

            if (statsObj instanceof Map) {
                Map<String, Object> statsMap = (Map<String, Object>) statsObj;
                stats.setConsumedCount((Integer) statsMap.getOrDefault("consumed", 0));
                stats.setLastUpdateTime((Long) statsMap.getOrDefault("lastUpdate", System.currentTimeMillis()));
            }

            return stats;

        } catch (Exception e) {
            log.error("获取用户缓存统计失败", e);
            return new CacheStats();
        }
    }

    /**
     * 缓存推荐结果到Redis
     */
    private void cacheRecommendations(Long userId, List<ProductInfo> productInfos) {
        try {
            if (redisTemplate == null || CollectionUtils.isEmpty(productInfos)) {
                return;
            }

            String poolKey = buildPoolKey(userId);

            // 将推荐结果添加到Redis List尾部
            for (ProductInfo productInfo : productInfos) {
                redisTemplate.opsForList().rightPush(poolKey, productInfo);
            }

            // 设置过期时间
            redisTemplate.expire(poolKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            // 限制缓存大小，防止无限增长
            Long totalSize = redisTemplate.opsForList().size(poolKey);
            if (totalSize != null && totalSize > DEFAULT_BATCH_SIZE * 2) {
                // 保留最新的批量大小的数据
                redisTemplate.opsForList().trim(poolKey, -DEFAULT_BATCH_SIZE, -1);
            }

            log.debug("缓存{}个推荐结果到Redis，Key: {}", productInfos.size(), poolKey);

        } catch (Exception e) {
            log.error("缓存推荐结果失败", e);
        }
    }

    /**
     * 更新缓存统计信息
     */
    private void updateCacheStats(Long userId, int consumedCount) {
        try {
            if (redisTemplate == null) {
                return;
            }

            String statsKey = buildStatsKey(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("consumed", consumedCount);
            stats.put("lastUpdate", System.currentTimeMillis());

            redisTemplate.opsForValue().set(statsKey, stats, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

        } catch (Exception e) {
            log.warn("更新缓存统计失败", e);
        }
    }

    /**
     * 获取候选商品池
     */
    private List<ProductInfo> getCandidateProducts() {
        try {
            // 使用增量同步服务获取候选商品
            return incrementalSyncService.getCandidateProducts(1L, MAX_CANDIDATE_SIZE);

        } catch (Exception e) {
            log.error("获取候选商品池失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 降级策略：直接生成推荐结果
     */
    private List<ProductInfo> generateRecommendationsDirectly(Long userId, int requestCount) {
        try {
            List<ProductInfo> candidateProducts = getCandidateProducts();
            if (CollectionUtils.isEmpty(candidateProducts)) {
                return new ArrayList<>();
            }

            // 直接调用推荐服务，不缓存
            RecommendationResult result = pushService.recommendProducts(userId, candidateProducts);
            if (result != null && !CollectionUtils.isEmpty(result.getRecommendations())) {
                return result.getRecommendations().stream()
                        .limit(requestCount)
                        .map(this::convertToProductInfo)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }

            return new ArrayList<>();

        } catch (Exception e) {
            log.error("直接生成推荐失败", e);
            return new ArrayList<>();
        }
    }

    /**
     * 转换RecommendedProduct为ProductInfo
     */
    private ProductInfo convertToProductInfo(RecommendedProduct recommendedProduct) {
        try {
            ProductInfo productInfo = new ProductInfo();
            productInfo.setProductId(recommendedProduct.getProductId());
            // 这里需要根据实际的RecommendedProduct结构进行转换
            // 可能需要额外查询商品详细信息
            return productInfo;
        } catch (Exception e) {
            log.warn("转换商品信息失败：{}", recommendedProduct, e);
            return null;
        }
    }

    private String buildPoolKey(Long userId) {
        return USER_RECOMMENDATION_POOL_KEY.replace("{}", userId.toString());
    }

    private String buildStatsKey(Long userId) {
        return USER_RECOMMENDATION_STATS_KEY.replace("{}", userId.toString());
    }
}
