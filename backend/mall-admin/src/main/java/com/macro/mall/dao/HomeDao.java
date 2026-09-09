package com.macro.mall.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 首页内容管理Dao
 */
public interface HomeDao {
    /**
     * 获取今日销售额
     */
    Double getTodaySales(@Param("today") Date today);
    
    /**
     * 获取昨日销售额
     */
    Double getYesterdaySales(@Param("yesterday") Date yesterday);
    
    /**
     * 获取今日订单数
     */
    Integer getTodayOrderCount(@Param("today") Date today);
    
    /**
     * 获取昨日订单数
     */
    Integer getYesterdayOrderCount(@Param("yesterday") Date yesterday);
    
    /**
     * 获取今日新增用户数
     */
    Integer getTodayNewUserCount(@Param("today") Date today);
    
    /**
     * 获取昨日新增用户数
     */
    Integer getYesterdayNewUserCount(@Param("yesterday") Date yesterday);
    
    /**
     * 获取今日访问量
     */
    Integer getTodayVisitCount(@Param("today") Date today);
    
    /**
     * 获取昨日访问量
     */
    Integer getYesterdayVisitCount(@Param("yesterday") Date yesterday);
    
    /**
     * 获取销售额趋势
     */
    List<Map<String, Object>> getSalesTrend(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取订单数趋势
     */
    List<Map<String, Object>> getOrderCountTrend(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取转化漏斗数据
     */
    List<Map<String, Object>> getFunnelData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取商品浏览量
     */
    Integer getProductViewCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取加入购物车数量
     */
    Integer getCartItemCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取下单数量
     */
    Integer getOrderCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取支付订单数量
     */
    Integer getPaidOrderCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 获取待处理事项数据
     */
    Map<String, Object> getPendingTasks();
    
    /**
     * 获取待付款订单数
     */
    Integer getPendingPaymentCount();
    
    /**
     * 获取待发货订单数
     */
    Integer getPendingDeliveryCount();
    
    /**
     * 获取待收货订单数
     */
    Integer getPendingReceiveCount();
    
    /**
     * 获取待评价订单数
     */
    Integer getPendingCommentCount();
    
    /**
     * 获取退款申请数
     */
    Integer getRefundApplyCount();
} 