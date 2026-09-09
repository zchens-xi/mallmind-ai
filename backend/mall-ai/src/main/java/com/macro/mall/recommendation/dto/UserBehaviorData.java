package com.macro.mall.recommendation.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户行为数据模型
 * @author macro
 */
@Data
@Setter
@Getter
public class UserBehaviorData {

    /**
     * 订单历史
     */
    private List<OrderData> orders;

    /**
     * 购物车历史
     */
    private List<CartData> cart;

    /**
     * 退货历史
     */
    private List<ReturnData> returns;
    
    /**
     * 类别偏好，确保Python端能正确处理
     */
    private Map<String, Double> categoryPreferences;
    
    /**
     * 构造函数，初始化空列表
     */
    public UserBehaviorData() {
        this.orders = new ArrayList<>();
        this.cart = new ArrayList<>();
        this.returns = new ArrayList<>();
        this.categoryPreferences = new HashMap<>();
    }
    
    /**
     * 初始化默认类别偏好，避免Python端警告
     */
    public void initDefaultCategoryPreferences() {
        if (this.categoryPreferences == null) {
            this.categoryPreferences = new HashMap<>();
        }
        
        // 添加一些默认的类别偏好，与config.py中的默认配置保持一致
        this.categoryPreferences.put("手机通讯", 0.15);
        this.categoryPreferences.put("笔记本", 0.15);
        this.categoryPreferences.put("家用电器", 0.1);
        this.categoryPreferences.put("服装", 0.1);
        this.categoryPreferences.put("休闲裤", 0.05);
        this.categoryPreferences.put("T恤", 0.05);
        this.categoryPreferences.put("洗衣机", 0.05);
        this.categoryPreferences.put("冰箱", 0.05);
        this.categoryPreferences.put("空调", 0.05);
        this.categoryPreferences.put("厨房小电", 0.05);
        this.categoryPreferences.put("食品", 0.05);
        this.categoryPreferences.put("图书", 0.05);
        this.categoryPreferences.put("美妆", 0.05);
        this.categoryPreferences.put("家居", 0.05);
    }

    @Data
    public static class OrderData {
        /**
         * 订单ID
         */
        private String orderId;

        /**
         * 订单商品列表
         */
        private List<OrderProduct> products;

        /**
         * 订单日期
         */
        private String orderDate;

        /**
         * 订单总金额
         */
        private Double totalAmount;

    }

    @Data
    public static class OrderProduct {
        /**
         * 商品ID
         */
        private String productId;

        /**
         * 商品类别
         */
        private String category;

        /**
         * 商品价格
         */
        private Double price;

        /**
         * 购买数量
         */
        private Integer quantity;
    }

    @Data
    public static class CartData {
        /**
         * 商品ID
         */
        private String productId;

        /**
         * 商品类别
         */
        private String category;

        /**
         * 商品价格
         */
        private Double price;

        /**
         * 添加时间
         */
        private String addTime;
    }

    @Data
    public static class ReturnData {
        /**
         * 商品ID
         */
        private String productId;

        /**
         * 商品类别
         */
        private String category;

        /**
         * 退货原因
         */
        private String returnReason;

        /**
         * 退货日期
         */
        private String returnDate;
    }
}
