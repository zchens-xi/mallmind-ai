//package com.macro.mall.ai.service;
//
//import com.macro.mall.ai.model.mongo.UserBehavior;
//import com.macro.mall.ai.model.neo4j.UserNode;
//import com.macro.mall.ai.model.neo4j.ProductNode;
//import com.macro.mall.ai.repository.mongo.UserBehaviorRepository;
//import com.macro.mall.ai.repository.neo4j.UserNodeRepository;
//import com.macro.mall.ai.repository.neo4j.ProductNodeRepository;
//import com.macro.mall.common.api.CommonResult;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Service;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//import java.util.stream.Collectors;
//
///**
// * 智能推荐服务
// * 基于用户行为和商品关系图谱进行个性化推荐
// */
//@Slf4j
//@Service
//public class IntelligentRecommendationService {
//
//    @Autowired
//    private UserBehaviorRepository userBehaviorRepository;
//
//    @Autowired
//    private UserNodeRepository userNodeRepository;
//
//    @Autowired
//    private ProductNodeRepository productNodeRepository;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Value("${recommendation.max-recommendations:10}")
//    private int maxRecommendations;
//
//    @Value("${recommendation.similarity-threshold:0.7}")
//    private double similarityThreshold;
//
//    private static final String RECOMMENDATION_CACHE_KEY = "ai:recommendation:";
//    private static final int CACHE_EXPIRE_HOURS = 2;
//
//    /**
//     * 获取用户个性化推荐
//     * 综合协同过滤、内容推荐和热门推荐
//     */
//    public CommonResult<List<ProductNode>> getPersonalizedRecommendations(Long userId) {
//        try {
//            // 1. 检查缓存
//            String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
//            List<ProductNode> cachedRecommendations = (List<ProductNode>) redisTemplate.opsForValue().get(cacheKey);
//            if (cachedRecommendations != null) {
//                return CommonResult.success(cachedRecommendations);
//            }
//
//            // 2. 获取多种推荐结果
//            List<ProductNode> collaborativeRecommendations = getCollaborativeRecommendations(userId);
//            List<ProductNode> contentRecommendations = getContentBasedRecommendations(userId);
//            List<ProductNode> popularRecommendations = getPopularRecommendations();
//
//            // 3. 融合推荐结果
//            List<ProductNode> finalRecommendations = fuseRecommendations(
//                collaborativeRecommendations,
//                contentRecommendations,
//                popularRecommendations
//            );
//
//            // 4. 缓存结果
//            redisTemplate.opsForValue().set(cacheKey, finalRecommendations, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
//
//            return CommonResult.success(finalRecommendations);
//
//        } catch (Exception e) {
//            log.error("获取个性化推荐失败：{}", e.getMessage(), e);
//            return CommonResult.failed("推荐服务暂时不可用");
//        }
//    }
//
//    /**
//     * 协同过滤推荐
//     * 基于用户行为相似性推荐
//     */
//    private List<ProductNode> getCollaborativeRecommendations(Long userId) {
//        try {
//            // 从Neo4j图谱获取协同过滤推荐
//            List<ProductNode> recommendations = userNodeRepository.findRecommendedProducts(userId, maxRecommendations);
//            log.info("协同过滤推荐用户{}获得{}个商品", userId, recommendations.size());
//            return recommendations;
//        } catch (Exception e) {
//            log.error("协同过滤推荐失败：{}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * 基于内容的推荐
//     * 基于用户历史行为和商品属性推荐
//     */
//    private List<ProductNode> getContentBasedRecommendations(Long userId) {
//        try {
//            // 1. 获取用户行为数据
//            List<UserBehavior> userBehaviors = userBehaviorRepository.findByUserIdOrderByTimestampDesc(userId);
//
//            // 2. 分析用户偏好
//            Map<String, Double> categoryPreferences = analyzeCategoryPreferences(userBehaviors);
//            Map<String, Double> brandPreferences = analyzeBrandPreferences(userBehaviors);
//
//            // 3. 根据偏好推荐商品
//            List<ProductNode> recommendations = new ArrayList<>();
//
//            for (Map.Entry<String, Double> entry : categoryPreferences.entrySet()) {
//                if (entry.getValue() > similarityThreshold) {
//                    Long categoryId = Long.valueOf(entry.getKey());
//                    List<ProductNode> categoryProducts = productNodeRepository.findProductsBygraphId(categoryId, 5);
//                    recommendations.addAll(categoryProducts);
//                }
//            }
//
//            log.info("基于内容推荐用户{}获得{}个商品", userId, recommendations.size());
//            return recommendations.stream().limit(maxRecommendations).collect(Collectors.toList());
//
//        } catch (Exception e) {
//            log.error("基于内容推荐失败：{}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * 热门商品推荐
//     * 作为冷启动和补充推荐
//     */
//    private List<ProductNode> getPopularRecommendations() {
//        try {
//            return productNodeRepository.findPopularProducts(maxRecommendations);
//        } catch (Exception e) {
//            log.error("热门推荐失败：{}", e.getMessage(), e);
//            return new ArrayList<>();
//        }
//    }
//
//    /**
//     * 融合多种推荐结果
//     * 使用加权平均和去重
//     */
//    private List<ProductNode> fuseRecommendations(List<ProductNode> collaborative,
//                                                  List<ProductNode> content,
//                                                  List<ProductNode> popular) {
//        Map<Long, ProductNode> productMap = new HashMap<>();
//        Map<Long, Double> scoreMap = new HashMap<>();
//
//        // 协同过滤权重：0.5
//        addProductsWithWeight(collaborative, productMap, scoreMap, 0.5);
//
//        // 内容推荐权重：0.3
//        addProductsWithWeight(content, productMap, scoreMap, 0.3);
//
//        // 热门推荐权重：0.2
//        addProductsWithWeight(popular, productMap, scoreMap, 0.2);
//
//        // 按分数排序并返回
//        return scoreMap.entrySet().stream()
//            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
//            .limit(maxRecommendations)
//            .map(entry -> productMap.get(entry.getKey()))
//            .collect(Collectors.toList());
//    }
//
//    /**
//     * 添加商品到融合结果中
//     */
//    private void addProductsWithWeight(List<ProductNode> products,
//                                       Map<Long, ProductNode> productMap,
//                                       Map<Long, Double> scoreMap,
//                                       double weight) {
//        for (int i = 0; i < products.size(); i++) {
//            ProductNode product = products.get(i);
//            Long productId = product.getProductId();
//
//            // 计算分数（位置越靠前分数越高）
//            double positionScore = (products.size() - i) / (double) products.size();
//            double finalScore = positionScore * weight;
//
//            productMap.put(productId, product);
//            scoreMap.merge(productId, finalScore, Double::sum);
//        }
//    }
//
//    /**
//     * 分析用户分类偏好
//     */
//    private Map<String, Double> analyzeCategoryPreferences(List<UserBehavior> behaviors) {
//        Map<String, Integer> categoryCount = new HashMap<>();
//        Map<String, Double> categoryWeight = new HashMap<>();
//
//        for (UserBehavior behavior : behaviors) {
//            String category = behavior.getProductCategory();
//            if (category != null) {
//                categoryCount.merge(category, 1, Integer::sum);
//
//                // 不同行为类型的权重
//                double weight = getActionWeight(behavior.getBehaviorType());
//                categoryWeight.merge(category, weight, Double::sum);
//            }
//        }
//
//        // 计算偏好得分
//        Map<String, Double> preferences = new HashMap<>();
//        for (Map.Entry<String, Double> entry : categoryWeight.entrySet()) {
//            String category = entry.getKey();
//            double totalWeight = entry.getValue();
//            int count = categoryCount.get(category);
//
//            // 偏好分数 = 总权重 / 总行为数
//            double preference = totalWeight / Math.max(count, 1);
//            preferences.put(category, preference);
//        }
//
//        return preferences;
//    }
//
//    /**
//     * 分析用户品牌偏好
//     */
//    private Map<String, Double> analyzeBrandPreferences(List<UserBehavior> behaviors) {
//        Map<String, Double> brandWeight = new HashMap<>();
//
//        for (UserBehavior behavior : behaviors) {
//            String brand = behavior.getProductBrand();
//            if (brand != null) {
//                double weight = getActionWeight(behavior.getBehaviorType());
//                brandWeight.merge(brand, weight, Double::sum);
//            }
//        }
//
//        return brandWeight;
//    }
//
//    /**
//     * 获取不同行为类型的权重
//     */
//    private double getActionWeight(String behaviorType) {
//        switch (behaviorType.toLowerCase()) {
//            case "purchase": return 5.0;
//            case "cart": return 3.0;
//            case "favorite": return 2.0;
//            case "click": return 1.5;
//            case "view": return 1.0;
//            default: return 0.5;
//        }
//    }
//
//    /**
//     * 记录用户行为
//     */
//    public void recordUserBehavior(Long userId, Long productId, String behaviorType, String context) {
//        try {
//            UserBehavior behavior = new UserBehavior();
//            behavior.setUserId(userId);
//            behavior.setProductId(productId);
//            behavior.setBehaviorType(behaviorType);
//            behavior.setTimestamp(LocalDateTime.now());
//            behavior.setDeviceType("web"); // 可以从请求头获取
//            behavior.setSource("recommendation");
//
//            userBehaviorRepository.save(behavior);
//
//            // 清除相关缓存
//            String cacheKey = RECOMMENDATION_CACHE_KEY + userId;
//            redisTemplate.delete(cacheKey);
//
//        } catch (Exception e) {
//            log.error("记录用户行为失败：{}", e.getMessage(), e);
//        }
//    }
//
//    /**
//     * 获取相似商品推荐
//     */
//    public CommonResult<List<ProductNode>> getSimilarProducts(Long productId, int limit) {
//        try {
//            List<ProductNode> similarProducts = productNodeRepository.findSimilarProducts(productId, limit);
//            return CommonResult.success(similarProducts);
//        } catch (Exception e) {
//            log.error("获取相似商品失败：{}", e.getMessage(), e);
//            return CommonResult.failed("获取相似商品失败");
//        }
//    }
//}
