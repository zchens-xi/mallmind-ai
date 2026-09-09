package com.macro.mall.service;

import com.macro.mall.dto.OrderStatusDistributionDto;
import com.macro.mall.dto.PaymentDistributionDto;
import com.macro.mall.dto.SalesAnalyticsDto;
import com.macro.mall.dto.UserPortraitDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 销售分析Service
 */
public interface AnalyticsService {
    
    /**
     * 获取销售数据分析
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param type 时间粒度：day/week/month
     * @return 销售分析数据
     */
    SalesAnalyticsDto getSalesAnalytics(Date startDate, Date endDate, String type);

    /**
     * 获取销售渠道分布
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 渠道分布数据
     */
    Map<String, Object> getSalesChannelDistribution(Date startDate, Date endDate);

    /**
     * 获取用户地域分布
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 地域分布数据
     */
    Map<String, Object> getUserRegionDistribution(Date startDate, Date endDate);

    /**
     * 获取支付方式分布
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 支付方式分布列表
     */
    List<PaymentDistributionDto> getPaymentDistribution(Date startDate, Date endDate);

    /**
     * 获取订单状态分布
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 订单状态分布列表
     */
    List<OrderStatusDistributionDto> getOrderStatusDistribution(Date startDate, Date endDate);

    /**
     * 获取用户画像数据
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 用户画像分析数据
     */
    UserPortraitDto getUserPortrait(Date startDate, Date endDate);
}
