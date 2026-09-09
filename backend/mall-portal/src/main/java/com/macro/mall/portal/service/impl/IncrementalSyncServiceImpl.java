package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.service.IncrementalSyncService;
import com.macro.mall.portal.service.PmsPortalProductService;
import com.macro.mall.portal.service.PushService;
import com.macro.mall.recommendation.dto.ProductInfo;
import com.macro.mall.recommendation.dto.RecommendationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 增量同步服务实现 - 专注于用户推荐结果缓存管理
 * 核心功能：
 * 1. 管理用户个性化推荐结果缓存
 * 2. 统一个性化推荐入口
 * 3. 提供热门商品兜底策略
 */
@Service
@Slf4j
public class IncrementalSyncServiceImpl implements IncrementalSyncService {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PmsPortalProductService productService;

    @Autowired
    private PushService pushService;

    // Redis Key 设计
    private static final String POPULAR_PRODUCTS_KEY = "product:popular:list";
    private static final String USER_BROWSE_HISTORY_KEY = "user:browse:history:{}";
    private static final String USER_RECOMMENDATION_CACHE_KEY = "user:recommendation:{}"; // 用户推荐结果缓存
    private static final String GLOBAL_CANDIDATE_POOL_KEY = "global:candidate:pool"; // 全局候选商品池(所有用户共享)
    private static final String USER_CANDIDATE_OFFSET_KEY = "user:candidate:offset:{}"; // 用户候选商品偏移量

    // 配置参数
    private static final int POPULAR_PRODUCTS_COUNT = 100;       // 热门商品缓存数量
    private static final int CACHE_EXPIRE_HOURS = 4;             // 缓存过期时间4小时
    private static final int GLOBAL_CANDIDATE_POOL_SIZE = 1000;  // 全局候选商品池大小
    private static final int BATCH_FETCH_SIZE = 100;             // 每次获取候选商品数量

    @Override
    public List<ProductInfo> getCandidateProducts(Long userId, int maxSize) {
        try {
            log.info("开始为用户{}获取个性化推荐商品，请求数量：{}", userId, maxSize);

            if (redisTemplate == null) {
                log.warn("Redis不可用，返回模拟候选商品");
                return generateMockProducts(maxSize);
            }

            // 1. 首先检查用户推荐结果缓存
            String userRecommendationKey = USER_RECOMMENDATION_CACHE_KEY.replace("{}", userId.toString());
            List<Object> cachedRecommendations = redisTemplate.opsForList()
                    .range(userRecommendationKey, 0, -1); // 获取所有已缓存的推荐

            List<ProductInfo> existingResults = new ArrayList<>();
            if (!CollectionUtils.isEmpty(cachedRecommendations)) {
                for (Object obj : cachedRecommendations) {
                    if (obj instanceof ProductInfo) {
                        existingResults.add((ProductInfo) obj);
                    }
                }
            }

            // 1.1 如果缓存足够，直接返回所需数量
            if (existingResults.size() >= maxSize) {
                log.info("用户{}推荐缓存充足({}/{}个)，直接返回", userId, existingResults.size(), maxSize);
                return existingResults.stream().limit(maxSize).collect(Collectors.toList());
            }

            // 1.2 缓存不足，计算还需要生成多少推荐商品
            int needMoreRecommendations = maxSize - existingResults.size();
            log.info("用户{}推荐缓存不足，已有{}个，还需要{}个", userId, existingResults.size(), needMoreRecommendations);

            // 2. 缓存不足，需要生成新的推荐
            log.info("用户{}推荐缓存不足，开始生成新推荐", userId);

            // 2.1 尝试从全局候选池获取未重复计算的候选商品
            List<ProductInfo> candidateProducts = getCandidateProductsFromGlobalPool(userId, needMoreRecommendations);

            if (CollectionUtils.isEmpty(candidateProducts)) {
                log.warn("全局候选池无可用商品，返回热门商品");
                return getPopularProducts(null, maxSize);
            }

            log.info("从全局候选池获取到{}个候选商品用于推荐计算", candidateProducts.size());

            // 2.2 调用PushService进行个性化推荐处理，传入合理的推荐数量
            // 推荐数量应该是候选商品的一个合理比例，体现筛选价值
            // maxRecommendCount：确保推荐算法有筛选价值，不能要求过多推荐
            int maxRecommendCount = Math.max(candidateProducts.size() * 30 / 100, 10); // 候选商品的30%，最少10个
            // requestCount：既要满足用户分页需求，又要控制在合理范围内
            int requestCount = Math.min(needMoreRecommendations * 2, maxRecommendCount); // 基于实际需求计算，而不是maxSize

            log.info("推荐计算参数 - 候选商品:{}个, 最大推荐数:{}, 实际请求数:{}, 实际需求:{}, Redis已有:{}个",
                    candidateProducts.size(), maxRecommendCount, requestCount, needMoreRecommendations, existingResults.size());
            RecommendationResult recommendationResult = pushService.recommendProducts(userId, candidateProducts, requestCount);

            if (recommendationResult != null && !CollectionUtils.isEmpty(recommendationResult.getRecommendations())) {
                // 2.3 将推荐结果转换为ProductInfo格式
                List<ProductInfo> newRecommendedProducts = recommendationResult.getRecommendations().stream()
                        .map(this::convertRecommendedToProductInfo)
                        .collect(Collectors.toList());

                // 2.4 将新推荐商品追加到Redis缓存
                if (!newRecommendedProducts.isEmpty()) {
                    redisTemplate.opsForList().rightPushAll(userRecommendationKey, newRecommendedProducts.toArray());
                    redisTemplate.expire(userRecommendationKey, 1, TimeUnit.HOURS); // 推荐结果缓存1小时
                    log.info("用户{}新增推荐结果已缓存，本次新增{}个，总计{}个商品",
                            userId, newRecommendedProducts.size(), existingResults.size() + newRecommendedProducts.size());
                }

                // 2.5 更新用户已使用的候选商品记录
                updateUserUsedCandidates(userId, candidateProducts, newRecommendedProducts);

                // 2.6 合并已有推荐和新推荐，返回用户所需数量
                List<ProductInfo> allRecommendations = new ArrayList<>(existingResults);
                allRecommendations.addAll(newRecommendedProducts);

                List<ProductInfo> result = allRecommendations.stream()
                        .limit(maxSize)
                        .collect(Collectors.toList());

                log.info("为用户{}返回{}个个性化推荐商品(Redis已有:{}, 新生成:{})",
                        userId, result.size(), existingResults.size(), newRecommendedProducts.size());
                return result;
            } else {
                log.warn("个性化推荐失败，为用户{}返回热门商品", userId);
                // 如果有已存在的推荐，优先返回已存在的
                if (!existingResults.isEmpty()) {
                    log.info("推荐失败，返回用户{}已有的{}个推荐商品", userId, existingResults.size());
                    return existingResults.stream().limit(maxSize).collect(Collectors.toList());
                }
                return getPopularProducts(null, maxSize);
            }

        } catch (Exception e) {
            log.error("获取用户{}个性化推荐失败", userId, e);
            return getPopularProducts(null, maxSize);
        }
    }


    /**
     * 将RecommendedProduct转换为ProductInfo
     */
    private ProductInfo convertRecommendedToProductInfo(com.macro.mall.recommendation.dto.RecommendedProduct recommended) {
        ProductInfo productInfo = new ProductInfo();
        productInfo.setProductId(recommended.getProductId());
        productInfo.setName(recommended.getName());
        productInfo.setPrice(recommended.getPrice());
        productInfo.setCategory(recommended.getCategory());
        productInfo.setBrand(recommended.getBrand());
        productInfo.setRating(recommended.getRating());
        // 可以添加推荐分数等信息
        return productInfo;
    }

    @Override
    public boolean needsDataRefresh() {
        return false;
    }

    @Override
    public void updateSyncTimestamp() {
        // 不再需要更新时间戳
    }

    @Override
    public List<ProductInfo> getPopularProducts(List<Long> excludeIds, int count) {
        try {
            log.debug("获取热门商品，排除{}个商品，需要{}个",
                    excludeIds != null ? excludeIds.size() : 0, count);

            if (redisTemplate == null) {
                return generateMockProducts(count);
            }

            List<Object> popularList = redisTemplate.opsForList()
                    .range(POPULAR_PRODUCTS_KEY, 0, POPULAR_PRODUCTS_COUNT - 1);

            List<ProductInfo> result = new ArrayList<>();
            Set<Long> excludeSet = excludeIds != null ? new HashSet<>(excludeIds) : new HashSet<>();

            if (!CollectionUtils.isEmpty(popularList)) {
                for (Object obj : popularList) {
                    if (obj instanceof ProductInfo) {
                        ProductInfo product = (ProductInfo) obj;
                        if (!excludeSet.contains(Long.valueOf(product.getProductId()))) {
                            result.add(product);
                            if (result.size() >= count) {
                                break;
                            }
                        }
                    }
                }
            }

            // 如果热门商品不足，生成模拟数据补充
            if (result.size() < count) {
                List<ProductInfo> mockProducts = generateMockProducts(count - result.size());
                result.addAll(mockProducts);
            }

            log.info("获取到{}个热门商品", result.size());
            return result;

        } catch (Exception e) {
            log.error("获取热门商品失败", e);
            return generateMockProducts(count);
        }
    }

    @Override
    public void refreshProductDataCache(boolean forceRefresh) {

    }

    /**
     * 刷新热门商品缓存（作为兜底策略）
     */
    private void refreshPopularProductsCache() {
        try {
            List<ProductInfo> popularProducts = productService.listPopularProducts(POPULAR_PRODUCTS_COUNT);

            if (!popularProducts.isEmpty()) {
                redisTemplate.delete(POPULAR_PRODUCTS_KEY);
                redisTemplate.opsForList().rightPushAll(POPULAR_PRODUCTS_KEY, popularProducts.toArray());
                redisTemplate.expire(POPULAR_PRODUCTS_KEY, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                log.debug("热门商品缓存已更新，共{}个", popularProducts.size());
            }
        } catch (Exception e) {
            log.error("刷新热门商品缓存失败", e);
        }
    }

    /**
     * 获取用户浏览历史
     */
    private Set<Long> getUserBrowseHistory(Long userId) {
        try {
            if (redisTemplate == null) {
                return new HashSet<>();
            }

            String historyKey = USER_BROWSE_HISTORY_KEY.replace("{}", userId.toString());
            Set<Object> history = redisTemplate.opsForSet().members(historyKey);

            if (CollectionUtils.isEmpty(history)) {
                return new HashSet<>();
            }

            return history.stream()
                    .map(obj -> Long.valueOf(obj.toString()))
                    .collect(HashSet::new, HashSet::add, HashSet::addAll);

        } catch (Exception e) {
            log.error("获取用户浏览历史失败", e);
            return new HashSet<>();
        }
    }

    /**
     * 生成模拟商品数据（实际项目中应该从数据库查询）
     */
    private List<ProductInfo> generateMockProducts(int count) {
        List<ProductInfo> products = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            ProductInfo product = new ProductInfo();
            product.setProductId(String.valueOf(random.nextInt(10000) + 1000));
            product.setCategory("电子产品");
            product.setPrice(random.nextDouble() * 1000 + 10);
            product.setRating(random.nextDouble() * 5);
            product.setStock(random.nextInt(100) + 1);
            product.setBrand("品牌" + (random.nextInt(10) + 1));
            products.add(product);
        }

        return products;
    }

    /**
     * 尝试从全局候选池获取未重复计算的候选商品
     */
    private List<ProductInfo> getCandidateProductsFromGlobalPool(Long userId, int maxSize) {
        try {
            // 1. 确保全局候选池已初始化
            if (!isGlobalCandidatePoolReady()) {
                refreshGlobalCandidatePool();
            }

            String userCandidateOffsetKey = USER_CANDIDATE_OFFSET_KEY.replace("{}", userId.toString());

            // 2. 获取用户当前的候选商品偏移量
            String offsetStr = (String) redisTemplate.opsForValue().get(userCandidateOffsetKey);
            Long offset = offsetStr != null ? Long.parseLong(offsetStr) : 0L;

            // 3. 从全局候选池中按偏移量获取候选商品
            long endIndex = offset + BATCH_FETCH_SIZE - 1;
            List<Object> candidateObjects = redisTemplate.opsForList().range(GLOBAL_CANDIDATE_POOL_KEY, offset, endIndex);

            List<ProductInfo> availableCandidates = new ArrayList<>();
            if (!CollectionUtils.isEmpty(candidateObjects)) {
                for (Object obj : candidateObjects) {
                    if (obj instanceof ProductInfo) {
                        availableCandidates.add((ProductInfo) obj);
                    }
                }
            }

            // 4. 如果可用候选商品不足，检查是否需要重置偏移量或刷新候选池
            if (availableCandidates.size() < BATCH_FETCH_SIZE) {
                log.info("用户{}从偏移量{}获取到{}个候选商品，不足{}个",
                        userId, offset, availableCandidates.size(), BATCH_FETCH_SIZE);

                // 检查候选池总大小
                Long poolSize = redisTemplate.opsForList().size(GLOBAL_CANDIDATE_POOL_KEY);
                if (poolSize != null && offset >= poolSize) {
                    // 偏移量已超过候选池大小，重置偏移量从头开始
                    log.info("用户{}候选商品偏移量已达到池尾，重置为0", userId);
                    offset = 0L;
                    redisTemplate.opsForValue().set(userCandidateOffsetKey, "0");

                    // 重新获取
                    endIndex = offset + BATCH_FETCH_SIZE - 1;
                    candidateObjects = redisTemplate.opsForList().range(GLOBAL_CANDIDATE_POOL_KEY, offset, endIndex);
                    availableCandidates.clear();

                    if (!CollectionUtils.isEmpty(candidateObjects)) {
                        for (Object obj : candidateObjects) {
                            if (obj instanceof ProductInfo) {
                                availableCandidates.add((ProductInfo) obj);
                            }
                        }
                    }
                } else {
                    // 候选池本身可能需要刷新
                    refreshGlobalCandidatePool();
                }
            }

            // 5. 更新用户候选商品偏移量
            long newOffset = offset + availableCandidates.size();
            redisTemplate.opsForValue().set(userCandidateOffsetKey, String.valueOf(newOffset));
            redisTemplate.expire(userCandidateOffsetKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

            log.info("用户{}从偏移量{}获取{}个候选商品，下次偏移量：{}",
                    userId, offset, availableCandidates.size(), newOffset);
            return availableCandidates;

        } catch (Exception e) {
            log.error("从全局候选池获取用户{}候选商品失败", userId, e);
            // 降级策略：直接从数据库获取
            return productService.listRecommendedProductsBatch(0, BATCH_FETCH_SIZE);
        }
    }

    /**
     * 检查全局候选池是否就绪
     */
    private boolean isGlobalCandidatePoolReady() {
        try {
            Long poolSize = redisTemplate.opsForList().size(GLOBAL_CANDIDATE_POOL_KEY);
            return poolSize != null && poolSize > 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 刷新全局候选池
     */
    private void refreshGlobalCandidatePool() {
        try {
            log.info("开始刷新全局候选池...");

            // 从数据库获取候选商品
            List<ProductInfo> candidateProducts = productService.listRecommendedProductsBatch(0, GLOBAL_CANDIDATE_POOL_SIZE);

            if (!CollectionUtils.isEmpty(candidateProducts)) {
                // 清空原有的全局候选池
                redisTemplate.delete(GLOBAL_CANDIDATE_POOL_KEY);

                // 重新填充全局候选池
                redisTemplate.opsForList().rightPushAll(GLOBAL_CANDIDATE_POOL_KEY, candidateProducts.toArray());

                // 设置过期时间
                redisTemplate.expire(GLOBAL_CANDIDATE_POOL_KEY, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);

                log.info("全局候选池已刷新，共{}个商品", candidateProducts.size());
            } else {
                log.warn("从数据库获取候选商品为空，无法刷新全局候选池");
            }

        } catch (Exception e) {
            log.error("刷新全局候选池失败", e);
        }
    }

    /**
     * 更新用户已使用的候选商品记录（简化版本）
     */
    private void updateUserUsedCandidates(Long userId, List<ProductInfo> usedCandidates, List<ProductInfo> newRecommendations) {
        // 偏移量已经在getCandidateProductsFromGlobalPool中更新了，这里不需要额外操作
        log.debug("用户{}本次使用了{}个候选商品", userId, usedCandidates.size());
    }
}
