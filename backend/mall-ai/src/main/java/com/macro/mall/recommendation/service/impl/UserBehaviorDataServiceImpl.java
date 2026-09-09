package com.macro.mall.recommendation.service.impl;

import com.macro.mall.mapper.OmsCartItemMapper;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderReturnApplyMapper;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.model.*;
import com.macro.mall.recommendation.dto.UserBehaviorData;
import com.macro.mall.recommendation.dto.UserBehaviorData.OrderData;
import com.macro.mall.recommendation.dto.UserBehaviorData.OrderProduct;
import com.macro.mall.recommendation.dto.UserBehaviorData.CartData;
import com.macro.mall.recommendation.dto.UserBehaviorData.ReturnData;
import com.macro.mall.recommendation.service.UserBehaviorDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * 用户行为数据服务实现类
 * 负责准备和格式化用户行为数据，确保包含正确的字段名称
 * @author macro
 */
@Slf4j
@Service
public class UserBehaviorDataServiceImpl implements UserBehaviorDataService {

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private OmsCartItemMapper cartItemMapper;

    @Autowired
    private OmsOrderReturnApplyMapper returnApplyMapper;

    @Autowired
    private PmsProductMapper productMapper;

    @Override
    public UserBehaviorData getUserBehaviorData(Long userId) {
        log.info("获取用户{}的行为数据", userId);
        UserBehaviorData userBehaviorData = new UserBehaviorData();

        try {
            // 获取订单数据
            List<OrderData> ordersData = getOrdersData(userId);
            log.info("用户{}的订单历史数据获取成功，有效记录{}条", userId, ordersData.size());

            // 获取购物车数据
            List<CartData> cartData = getCartData(userId);
            log.info("用户{}的购物车历史数据获取成功，有效记录{}条", userId, cartData.size());

            // 获取退货数据
            List<ReturnData> returnsData = getReturnsData(userId);
            log.info("成功获取用户{}的行为数据: 订单{}个, 购物车{}个, 退货{}个",
                     userId, ordersData.size(), cartData.size(), returnsData.size());

            // 设置用户行为数据
            userBehaviorData.setOrders(ordersData);
            userBehaviorData.setCart(cartData);
            userBehaviorData.setReturns(returnsData);
            
            // 初始化默认类别偏好，避免Python端警告
            userBehaviorData.initDefaultCategoryPreferences();
            
            // 根据用户行为数据自定义类别偏好
            customizeCategoryPreferences(userBehaviorData, ordersData, cartData);
            
            log.info("用户{}的行为数据处理完成，包含{}个类别偏好", 
                    userId, userBehaviorData.getCategoryPreferences().size());

            return userBehaviorData;
        } catch (Exception e) {
            log.error("获取用户{}行为数据失败", userId, e);
            return new UserBehaviorData();
        }
    }
    
    /**
     * 根据用户行为数据自定义类别偏好
     * 这里使用一个简单的算法，基于订单和购物车中的类别频率
     */
    private void customizeCategoryPreferences(UserBehaviorData userBehaviorData, 
                                             List<OrderData> ordersData, 
                                             List<CartData> cartData) {
        Map<String, Integer> categoryFrequency = new HashMap<>();
        
        // 统计订单中的类别频率
        for (OrderData order : ordersData) {
            if (order.getProducts() != null) {
                for (OrderProduct product : order.getProducts()) {
                    String category = product.getCategory();
                    if (category != null && !category.isEmpty()) {
                        categoryFrequency.put(category, 
                                categoryFrequency.getOrDefault(category, 0) + 1);
                    }
                }
            }
        }
        
        // 统计购物车中的类别频率
        for (CartData item : cartData) {
            String category = item.getCategory();
            if (category != null && !category.isEmpty()) {
                categoryFrequency.put(category, 
                        categoryFrequency.getOrDefault(category, 0) + 1);
            }
        }
        
        // 如果有足够的类别数据，则更新偏好
        if (categoryFrequency.size() >= 3) {
            // 计算总频率
            int totalFrequency = categoryFrequency.values().stream()
                    .mapToInt(Integer::intValue).sum();
            
            // 清除默认偏好
            userBehaviorData.getCategoryPreferences().clear();
            
            // 根据频率设置偏好
            for (Map.Entry<String, Integer> entry : categoryFrequency.entrySet()) {
                double preference = (double) entry.getValue() / totalFrequency;
                userBehaviorData.getCategoryPreferences().put(entry.getKey(), preference);
            }
            
            log.info("已根据用户行为自定义类别偏好，共{}个类别", 
                    userBehaviorData.getCategoryPreferences().size());
        } else {
            log.info("用户行为数据中类别不足，使用默认类别偏好");
        }
    }

    /**
     * 获取订单数据
     * 确保包含必要的字段名称：category, price, rating
     */
    private List<OrderData> getOrdersData(Long userId) {
        List<OrderData> ordersData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取用户订单项
        OmsOrderItemExample example = new OmsOrderItemExample();
        example.createCriteria().andIdEqualTo(userId);
        List<OmsOrderItem> orderItems = orderItemMapper.selectByExample(example);

        Map<Long, OrderData> orderMap = new HashMap<>();

        for (OmsOrderItem item : orderItems) {
            // 获取商品信息
            PmsProduct product = null;
            if (item.getProductId() != null) {
                product = productMapper.selectByPrimaryKey(item.getProductId());
            }

            // 获取或创建订单数据
            OrderData orderData = orderMap.computeIfAbsent(item.getOrderId(), k -> {
                OrderData newOrder = new OrderData();
                newOrder.setOrderId(String.valueOf(item.getOrderId()));
                newOrder.setOrderDate(dateFormat.format(new Date())); // 实际应从订单表获取
                newOrder.setTotalAmount(0.0);
                newOrder.setProducts(new ArrayList<>());
                return newOrder;
            });

            // 创建订单商品
            OrderProduct orderProduct = new OrderProduct();
            
            // 设置商品ID
            orderProduct.setProductId(String.valueOf(item.getProductId()));
            
            // 设置商品类别
            String categoryName = "";
            if (product != null && product.getProductCategoryName() != null) {
                categoryName = product.getProductCategoryName();
            }
            orderProduct.setCategory(categoryName);
            
            // 设置商品价格
            orderProduct.setPrice(item.getProductPrice() != null ? item.getProductPrice().doubleValue() : 0.0);
            
            // 设置购买数量
            orderProduct.setQuantity(item.getProductQuantity());
            
            // 添加商品到订单
            orderData.getProducts().add(orderProduct);
            
            // 更新订单总金额
            double itemTotal = orderProduct.getPrice() * orderProduct.getQuantity();
            orderData.setTotalAmount(orderData.getTotalAmount() + itemTotal);
        }

        // 将Map转换为List
        return new ArrayList<>(orderMap.values());
    }

    /**
     * 获取购物车数据
     * 确保包含必要的字段名称：category, price
     */
    private List<CartData> getCartData(Long userId) {
        List<CartData> cartData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取用户购物车项
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andMemberIdEqualTo(userId).andDeleteStatusEqualTo(0);
        List<OmsCartItem> cartItems = cartItemMapper.selectByExample(example);

        for (OmsCartItem item : cartItems) {
            // 获取商品信息
            PmsProduct product = null;
            if (item.getProductId() != null) {
                product = productMapper.selectByPrimaryKey(item.getProductId());
            }

            CartData cartItemData = new CartData();

            // 设置商品ID
            cartItemData.setProductId(String.valueOf(item.getProductId()));
            
            // 设置商品类别
            String categoryName = "";
            if (product != null && product.getProductCategoryName() != null) {
                categoryName = product.getProductCategoryName();
            }
            cartItemData.setCategory(categoryName);
            
            // 设置商品价格
            cartItemData.setPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0.0);
            
            // 设置添加时间
            cartItemData.setAddTime(item.getCreateDate() != null ? 
                dateFormat.format(item.getCreateDate()) : dateFormat.format(new Date()));

            cartData.add(cartItemData);
        }

        return cartData;
    }

    /**
     * 获取退货数据
     * 确保包含必要的字段名称：category, price
     */
    private List<ReturnData> getReturnsData(Long userId) {
        List<ReturnData> returnsData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // 获取用户名
        String username = getUsernameByUserId(userId);
        if (username == null || username.isEmpty()) {
            return returnsData;
        }

        // 获取用户退货申请
        OmsOrderReturnApplyExample example = new OmsOrderReturnApplyExample();
        example.createCriteria().andMemberUsernameEqualTo(username);
        List<OmsOrderReturnApply> returnApplies = returnApplyMapper.selectByExample(example);

        for (OmsOrderReturnApply returnApply : returnApplies) {
            ReturnData returnData = new ReturnData();

            // 设置商品ID
            returnData.setProductId(String.valueOf(returnApply.getProductId()));
            
            // 设置商品类别
            String categoryName = getCategoryNameByProductId(returnApply.getProductId());
            returnData.setCategory(categoryName);
            
            // 设置退货原因
            returnData.setReturnReason(returnApply.getReason());
            
            // 设置退货日期
            returnData.setReturnDate(returnApply.getCreateTime() != null ? 
                dateFormat.format(returnApply.getCreateTime()) : dateFormat.format(new Date()));

            returnsData.add(returnData);
        }

        return returnsData;
    }

    /**
     * 根据用户ID获取用户名
     */
    private String getUsernameByUserId(Long userId) {
        // 这里应该根据实际情况实现，例如查询用户表
        // 为简化示例，这里返回一个固定值
        return "test";
    }

    /**
     * 根据商品ID获取类别名称
     */
    private String getCategoryNameByProductId(Long productId) {
        if (productId == null) {
            return "";
        }

        PmsProduct product = productMapper.selectByPrimaryKey(productId);
        return product != null ? product.getProductCategoryName() : "";
    }
}
