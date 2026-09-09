package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.recommendation.service.MomentumCalculationService;
import com.macro.mall.recommendation.dto.MomentumData;
import com.macro.mall.recommendation.dto.ProductInfo;
import com.macro.mall.recommendation.dto.RecommendationResult;
import com.macro.mall.recommendation.dto.RecommendedProduct;
import com.macro.mall.recommendation.dto.UserBehaviorData;
import com.macro.mall.portal.service.PushService;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.recommendation.service.RecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashMap;

@Slf4j
@Service
public class PushServiceImpl implements PushService {

    @Autowired
    private UmsMemberService memberService; // 用于用户服务

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private MomentumCalculationService momentumCalculationService;

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private OmsOrderItemMapper omsOrderItemMapper;

    @Autowired
    private OmsCartItemMapper omsCartItemMapper;

    @Autowired
    private OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;

    @Autowired
    private PmsProductMapper pmsProductMapper;

    @Autowired
    private PmsProductCategoryMapper pmsProductCategoryMapper;

    /**
     * 更新推送的用户行为数据
     * 定时任务，或者叫守护进程，扫描所有用户，循环进行。
     * @param userId 用户ID
     */
//    @Override
    public void renewLongTermMoenment(Long userId){
        try {
            log.info("开始更新用户{}的长期动量", userId);

            // 获取用户行为数据
            UserBehaviorData behaviorData = getUserBehaviorData(userId);

            // 使用MomentumCalculationService计算长期动量
            MomentumData longTermMomentum = momentumCalculationService.calculateLongTermMomentum(userId, behaviorData);

            if (longTermMomentum != null) {
                // 保存长期动量数据到MongoDB
                MomentumData savedData = recommendationService.saveLongTermMomentum(userId, longTermMomentum);
                log.info("长期动量数据已保存到MongoDB: userId={}, id={}", userId, savedData.getId());
            } else {
                log.warn("计算长期动量失败，userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("更新用户长期动量失败", e);
        }
    }

    /**
     * 更新短期动量
     * 当用户点击商品时调用，用于更新短期行为动量
     * @param userId 用户ID
     * @param clickedProduct 点击的商品信息
     * @param clickStrength 点击强度 (0.1-2.0)
     */
    @Override
    public void renewShortTermMomentum(Long userId, ProductInfo clickedProduct, Double clickStrength) {
        //TODO: 需要配置点击触发
        try {
            log.info("开始更新用户{}的短期动量，商品ID: {}, 点击强度: {}",
                    userId, clickedProduct.getProductId(), clickStrength);

            // 准备点击商品列表和强度列表
            List<ProductInfo> clickedProducts = new ArrayList<>();
            clickedProducts.add(clickedProduct);
            List<Double> clickStrengths = new ArrayList<>();
            clickStrengths.add(clickStrength);

            // 使用MomentumCalculationService计算短期动量
            MomentumData shortTermMomentum = momentumCalculationService.calculateShortTermMomentum(
                    userId, clickedProducts, clickStrengths);

            if (shortTermMomentum != null) {
                // 保存短期动量数据到MongoDB
                MomentumData savedData = recommendationService.saveShortTermMomentum(userId, shortTermMomentum);
                log.info("短期动量数据已保存到MongoDB: userId={}, id={}",
                        userId, savedData.getId());
            } else {
                log.error("计算短期动量失败，userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("更新用户{}短期动量失败", userId, e);
        }
    }

    /**
     * 更新推送动量
     * 融合长期动量和短期动量，生成用于推荐的累积动量
     * @param userId 用户ID
     * @param clickedProduct 点击的商品信息
     * @param clickStrength 点击强度 (0.1-2.0)
     */
    @Override
    public void renewPushMomentum(Long userId, ProductInfo clickedProduct, Double clickStrength) {
        try {
            log.info("开始更新用户{}的推送动量，商品ID: {}, 点击强度: {}",
                    userId, clickedProduct.getProductId(), clickStrength);

            // 1. 获取或计算长期动量
            MomentumData longTermMomentum = recommendationService.getLatestLongTermMomentum(userId);
            if (longTermMomentum == null) {
                log.warn("用户{}没有长期动量数据，先计算长期动量", userId);
                UserBehaviorData behaviorData = getUserBehaviorData(userId);
                longTermMomentum = momentumCalculationService.calculateLongTermMomentum(userId, behaviorData);
                if (longTermMomentum != null) {
                    recommendationService.saveLongTermMomentum(userId, longTermMomentum);
                }
            }

            // 2. 计算短期动量（基于当前点击）
            List<ProductInfo> clickedProducts = new ArrayList<>();
            clickedProducts.add(clickedProduct);
            List<Double> clickStrengths = new ArrayList<>();
            clickStrengths.add(clickStrength);

            MomentumData shortTermMomentum = momentumCalculationService.calculateShortTermMomentum(
                    userId, clickedProducts, clickStrengths);

            if (shortTermMomentum != null) {
                // 保存短期动量
                recommendationService.saveShortTermMomentum(userId, shortTermMomentum);
            }

            // 3. 获取当前推送动量（如果存在）
            MomentumData currentPushMomentum = recommendationService.getLatestPushMomentum(userId);

            // 4. 融合计算新的推送动量
            if (longTermMomentum != null && shortTermMomentum != null) {
                MomentumData newPushMomentum = momentumCalculationService.calculatePushMomentum(
                        longTermMomentum, shortTermMomentum, currentPushMomentum);

                if (newPushMomentum != null) {
                    // 保存新的推送动量
                    MomentumData savedData = recommendationService.savePushMomentum(userId, newPushMomentum);
                    log.info("推送动量数据已保存到MongoDB: userId={}, id={}, 累积点击: {}",
                            userId, savedData.getId(), savedData.getCumulativeClicks());
                } else {
                    log.error("计算推送动量失败，userId: {}", userId);
                }
            } else {
                log.error("无法计算推送动量，缺少必要的基础动量数据，userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("更新用户{}推送动量失败", userId, e);
        }
    }

    /**
     * 获取用户行为数据，从mysql和mongoDB中获取用户行为数据
     * 包括用户的浏览记录、购买记录、收藏记录等
     * @param userId 用户ID
     * @return UserBehaviorData
     */
    @Override
    public UserBehaviorData getUserBehaviorData(Long userId) {
        log.info("获取用户{}的行为数据", userId);

        UserBehaviorData behaviorData = new UserBehaviorData();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // 1. 获取订单历史数据
            List<UserBehaviorData.OrderData> orders = getOrderHistory(userId, dateFormat);
            behaviorData.setOrders(orders);

            // 2. 获取购物车历史数据
            List<UserBehaviorData.CartData> cart = getCartHistory(userId, dateFormat);
            behaviorData.setCart(cart);

            // 3. 获取退货历史数据
            List<UserBehaviorData.ReturnData> returns = getReturnHistory(userId, dateFormat);
            behaviorData.setReturns(returns);

            log.info("成功获取用户{}的行为数据: 订单{}个, 购物车{}个, 退货{}个",
                    userId, orders.size(), cart.size(), returns.size());

        } catch (Exception e) {
            log.error("获取用户{}行为数据失败", userId, e);
        }

        return behaviorData;
    }

    /**
     * 获取用户订单历史
     */
    private List<UserBehaviorData.OrderData> getOrderHistory(Long userId, SimpleDateFormat dateFormat) {
        List<UserBehaviorData.OrderData> orderDataList = new ArrayList<>();

        // 查询用户的所有订单
        OmsOrderExample orderExample = new OmsOrderExample();
        orderExample.createCriteria().andMemberIdEqualTo(userId);
        orderExample.setOrderByClause("create_time desc");

        List<OmsOrder> orders = omsOrderMapper.selectByExample(orderExample);

        for (OmsOrder order : orders) {
            UserBehaviorData.OrderData orderData = new UserBehaviorData.OrderData();
            orderData.setOrderId(order.getId().toString());
            orderData.setOrderDate(dateFormat.format(order.getCreateTime()));
            orderData.setTotalAmount(order.getTotalAmount().doubleValue());

            // 获取订单商品详情
            List<UserBehaviorData.OrderProduct> products = getOrderProducts(order.getId());
            orderData.setProducts(products);

            orderDataList.add(orderData);
        }

        return orderDataList;
    }

    /**
     * 获取订单商品详情
     */
    private List<UserBehaviorData.OrderProduct> getOrderProducts(Long orderId) {
        List<UserBehaviorData.OrderProduct> productList = new ArrayList<>();

        // 查询订单项
        OmsOrderItemExample itemExample = new OmsOrderItemExample();
        itemExample.createCriteria().andOrderIdEqualTo(orderId);

        List<OmsOrderItem> orderItems = omsOrderItemMapper.selectByExample(itemExample);

        for (OmsOrderItem item : orderItems) {
            UserBehaviorData.OrderProduct product = new UserBehaviorData.OrderProduct();
            product.setProductId(item.getProductId().toString());
            product.setPrice(item.getProductPrice().doubleValue());
            product.setQuantity(item.getProductQuantity());

            // 获取商品类别
            String category = getProductCategory(item.getProductCategoryId());
            product.setCategory(category);

            productList.add(product);
        }

        return productList;
    }

    /**
     * 获取购物车历史
     */
    private List<UserBehaviorData.CartData> getCartHistory(Long userId, SimpleDateFormat dateFormat) {
        List<UserBehaviorData.CartData> cartDataList = new ArrayList<>();

        // 查询用户购物车数据（包括已删除的）
        OmsCartItemExample cartExample = new OmsCartItemExample();
        // 添加关键约束：过滤掉脏数据
        cartExample.createCriteria()
                .andMemberIdEqualTo(userId)
                .andPriceIsNotNull()        // 价格不能为空
                .andCreateDateIsNotNull();  // 创建时间不能为空
        cartExample.setOrderByClause("create_date desc");

        List<OmsCartItem> cartItems = omsCartItemMapper.selectByExample(cartExample);
        if (CollectionUtils.isEmpty(cartItems)) {
            log.warn("用户{}没有有效的购物车数据", userId);
            return cartDataList;
        }

        // 批量获取商品信息
        List<Long> productIds = cartItems.stream().map(OmsCartItem::getProductId).distinct().collect(Collectors.toList());
        PmsProductExample productExample = new PmsProductExample();
        productExample.createCriteria().andIdIn(productIds);
        List<PmsProduct> products = pmsProductMapper.selectByExample(productExample);
        Map<Long, PmsProduct> productMap = products.stream().collect(Collectors.toMap(PmsProduct::getId, Function.identity()));

        // 批量获取商品分类信息
        List<Long> categoryIds = products.stream()
                .map(PmsProduct::getProductCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryMap = getCategoryNameMap(categoryIds);

        for (OmsCartItem cartItem : cartItems) {
            // 检查关键字段是否为空
            if (cartItem.getProductId() == null) {
                log.warn("发现购物车记录的product_id为空，跳过该记录: cartItemId={}", cartItem.getId());
                continue;
            }

            UserBehaviorData.CartData cartData = new UserBehaviorData.CartData();
            cartData.setProductId(cartItem.getProductId().toString());

            // 由于查询时已经过滤了NULL值，这里可以安全使用
            cartData.setPrice(cartItem.getPrice().doubleValue());
            cartData.setAddTime(dateFormat.format(cartItem.getCreateDate()));

            // 获取商品类别
            PmsProduct product = productMap.get(cartItem.getProductId());
            if (product != null) {
                String category = categoryMap.getOrDefault(product.getProductCategoryId(), "未分类");
                cartData.setCategory(category);
            } else {
                cartData.setCategory("未分类");
            }

            cartDataList.add(cartData);
        }

        log.info("用户{}的购物车历史数据获取成功，有效记录{}条", userId, cartDataList.size());
        return cartDataList;
    }

    /**
     * 获取退货历史
     */
    private List<UserBehaviorData.ReturnData> getReturnHistory(Long userId, SimpleDateFormat dateFormat) {
        List<UserBehaviorData.ReturnData> returnDataList = new ArrayList<>();
        String username = getUsernameById(userId);
        if (username == null) {
            log.warn("无法获取用户{}的用户名，无法查询退货历史", userId);
            return returnDataList;
        }

        // 查询用户退货申请数据
        OmsOrderReturnApplyExample returnExample = new OmsOrderReturnApplyExample();
        returnExample.createCriteria().andMemberUsernameEqualTo(username);
        returnExample.setOrderByClause("create_time desc");

        List<OmsOrderReturnApply> returnApplies = omsOrderReturnApplyMapper.selectByExample(returnExample);
        if (CollectionUtils.isEmpty(returnApplies)) {
            return returnDataList;
        }

        // 批量获取商品信息
        List<Long> productIds = returnApplies.stream().map(OmsOrderReturnApply::getProductId).distinct().collect(Collectors.toList());
        PmsProductExample productExample = new PmsProductExample();
        productExample.createCriteria().andIdIn(productIds);
        List<PmsProduct> products = pmsProductMapper.selectByExample(productExample);
        Map<Long, PmsProduct> productMap = products.stream().collect(Collectors.toMap(PmsProduct::getId, Function.identity()));

        // 批量获取商品分类信息
        List<Long> categoryIds = products.stream()
                .map(PmsProduct::getProductCategoryId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> categoryMap = getCategoryNameMap(categoryIds);


        for (OmsOrderReturnApply returnApply : returnApplies) {
            UserBehaviorData.ReturnData returnData = new UserBehaviorData.ReturnData();
            returnData.setProductId(returnApply.getProductId().toString());
            returnData.setReturnReason(returnApply.getReason());
            returnData.setReturnDate(dateFormat.format(returnApply.getCreateTime()));

            // 获取商品类别
            PmsProduct product = productMap.get(returnApply.getProductId());
            if (product != null) {
                String category = categoryMap.getOrDefault(product.getProductCategoryId(), "未分类");
                returnData.setCategory(category);
            } else {
                returnData.setCategory("未分类");
            }

            returnDataList.add(returnData);
        }

        return returnDataList;
    }

    /**
     * 根据商品类别ID获取类别名称
     */
    private String getProductCategory(Long categoryId) {
        if (categoryId == null) {
            return "未分类";
        }

        try {
            PmsProductCategory category = pmsProductCategoryMapper.selectByPrimaryKey(categoryId);
            return category != null ? category.getName() : "未分类";
        } catch (Exception e) {
            log.warn("获取商品类别失败，categoryId: {}", categoryId, e);
            return "未分类";
        }
    }

    private Map<Long, String> getCategoryNameMap(List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return Collections.emptyMap();
        }
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        example.createCriteria().andIdIn(categoryIds);
        List<PmsProductCategory> categories = pmsProductCategoryMapper.selectByExample(example);
        return categories.stream()
                .collect(Collectors.toMap(PmsProductCategory::getId, PmsProductCategory::getName));
    }

    /**
     * 根据用户ID获取用户名（简单实现，实际应该有更好的方式）
     */
    private String getUsernameById(Long userId) {
        // 这里简化处理，实际项目中应该查询用户表
        // 由于退货表中是通过用户名关联的，这里需要获取用户名
        try {
            // 从订单表中获取用户名
            OmsOrderExample orderExample = new OmsOrderExample();
            orderExample.createCriteria().andMemberIdEqualTo(userId);
            try (com.github.pagehelper.Page<Object> page = com.github.pagehelper.PageHelper.startPage(1, 1)) {
                List<OmsOrder> orders = omsOrderMapper.selectByExample(orderExample);
                if (!CollectionUtils.isEmpty(orders)) {
                    return orders.get(0).getMemberUsername();
                }
            }
        } catch (Exception e) {
            log.warn("获取用户名失败，userId: {}", userId, e);
        }

        return null;
    }

    /**
     * 基于动量推荐商品
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @param requestCount 请求的推荐商品数量
     * @return 推荐结果，包含排序后的商品列表
     */
    @Override
    public RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts, int requestCount) {
        try {
            log.info("开始为用户{}推荐商品，候选商品数量: {}，请求数量: {}", userId, candidateProducts.size(), requestCount);

            // 1. 获取用户最新的推送动量
            MomentumData pushMomentum = recommendationService.getLatestPushMomentum(userId);

            if (pushMomentum == null) {
                log.warn("用户{}没有推送动量数据，尝试生成", userId);

                // 获取长期动量，如果没有则先计算
                MomentumData longTermMomentum = recommendationService.getLatestLongTermMomentum(userId);
                if (longTermMomentum == null) {
                    UserBehaviorData behaviorData = getUserBehaviorData(userId);
                    longTermMomentum = momentumCalculationService.calculateLongTermMomentum(userId, behaviorData);
                    if (longTermMomentum != null) {
                        recommendationService.saveLongTermMomentum(userId, longTermMomentum);
                    }
                }

                // 使用长期动量作为初始推送动量
                if (longTermMomentum != null) {
                    // 创建一个新的推送动量对象，而不是直接使用长期动量对象
                    pushMomentum = new MomentumData();
                    pushMomentum.setUserId(userId);
                    pushMomentum.setType("push_momentum");
                    pushMomentum.setCategoryPreferences(new HashMap<>(longTermMomentum.getCategoryPreferences()));
                    pushMomentum.setPriceModel(longTermMomentum.getPriceModel());
                    pushMomentum.setQualityModel(longTermMomentum.getQualityModel());
                    pushMomentum.setCumulativeClicks(0);
                    pushMomentum.setDataSource("initialized_from_long_term");
                    recommendationService.savePushMomentum(userId, pushMomentum);
                    log.info("已基于长期动量为用户{}创建新的推送动量", userId);
                } else {
                    log.error("无法为用户{}生成推送动量，使用默认推荐", userId);
                    return createDefaultRecommendation(candidateProducts, requestCount);
                }
            }

            // 2. 使用MomentumCalculationService进行商品推荐
            RecommendationResult result = momentumCalculationService.recommendProducts(
                    userId, candidateProducts, pushMomentum, requestCount);

            if (result != null) {
                log.info("用户{}个性化推荐成功，推荐商品数量: {}",
                        userId, result.getRecommendations().size());
                return result;
            } else {
                log.warn("个性化推荐失败，为用户{}返回默认推荐", userId);
                return createDefaultRecommendation(candidateProducts, requestCount);
            }

        } catch (Exception e) {
            log.error("为用户{}推荐商品失败", userId, e);
            return createDefaultRecommendation(candidateProducts, requestCount);
        }
    }

    /**
     * 创建默认推荐结果（当个性化推荐失败时使用）
     */
    private RecommendationResult createDefaultRecommendation(List<ProductInfo> candidateProducts, int requestCount) {
        RecommendationResult result = new RecommendationResult();

        // 简单按价格排序作为默认推荐，使用传入的requestCount参数
        List<RecommendedProduct> defaultRecommended = candidateProducts.stream()
                .limit(requestCount) // 使用请求的数量而不是固定的10
                .map(product -> {
                    RecommendedProduct recommended = new RecommendedProduct();
                    // 直接设置RecommendedProduct的字段，而不是嵌套的product对象
                    recommended.setProductId(product.getProductId());
                    recommended.setName(product.getName());
                    recommended.setPrice(product.getPrice());
                    recommended.setCategory(product.getCategory());
                    recommended.setBrand(product.getBrand());
                    recommended.setRating(product.getRating());
                    recommended.setRecommendationScore(1.0); // 默认分数
                    return recommended;
                })
                .collect(Collectors.toList());

        result.setRecommendations(defaultRecommended);
        result.setTotalCount(defaultRecommended.size());
        result.setMomentumType("default");
        result.setAlgorithmVersion("1.0");

        return result;
    }

    /**
     * 更新长期动量
     * 定时任务，扫描所有用户进行长期动量更新
     */
    @Override
    public void renewLongTermMomentum(Long userId) {
        try {
            log.info("开始更新用户{}的长期动量", userId);

            // 获取用户行为数据
            UserBehaviorData behaviorData = getUserBehaviorData(userId);

            // 使用MomentumCalculationService计算长期动量
            MomentumData longTermMomentum = momentumCalculationService.calculateLongTermMomentum(userId, behaviorData);

            if (longTermMomentum != null) {
                // 保存长期动量数据到MongoDB
                MomentumData savedData = recommendationService.saveLongTermMomentum(userId, longTermMomentum);
                log.info("长期动量数据已保存到MongoDB: userId={}, id={}", userId, savedData.getId());
            } else {
                log.warn("计算长期动量失败，userId: {}", userId);
            }

        } catch (Exception e) {
            log.error("更新用户{}长期动量失败", userId, e);
        }
    }

    /**
     * 基于动量推荐商品（默认版本，不指定数量）
     * @param userId 用户ID
     * @param candidateProducts 候选商品列表
     * @return 推荐结果，包含排序后的商品列表
     */
    @Override
    public RecommendationResult recommendProducts(Long userId, List<ProductInfo> candidateProducts) {
        // 默认推荐10个商品
        return recommendProducts(userId, candidateProducts, 10);
    }
}
