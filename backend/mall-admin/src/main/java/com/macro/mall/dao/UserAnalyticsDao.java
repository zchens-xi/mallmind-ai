package com.macro.mall.dao;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户分析DAO
 */
public interface UserAnalyticsDao {

    /**
     * 获取总用户数
     */
    Long getTotalUserCount(@Param("endDate") Date endDate);

    /**
     * 获取新增用户数
     */
    Long getNewUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取活跃用户数
     */
    Long getActiveUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取付费用户数
     */
    Long getPaidUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户趋势数据
     */
    List<Map<String, Object>> getUserTrendData(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate,
                                               @Param("type") String type);

    /**
     * 获取新增用户趋势数据
     */
    List<Map<String, Object>> getNewUserTrendData(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate,
                                                  @Param("type") String type);

    /**
     * 获取活跃用户趋势数据
     */
    List<Map<String, Object>> getActiveUserTrendData(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate,
                                                     @Param("type") String type);

    /**
     * 获取留存率数据
     */
    List<Map<String, Object>> getRetentionData(@Param("startDate") Date startDate,
                                               @Param("endDate") Date endDate);

    /**
     * 获取用户活跃度分布
     */
    List<Map<String, Object>> getUserActivityDistribution(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);

    /**
     * 获取用户行为漏斗数据 - 访问用户数
     */
    Long getVisitUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户行为漏斗数据 - 浏览商品用户数
     */
    Long getViewProductUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户行为漏斗数据 - 加购物车用户数
     */
    Long getAddToCartUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户行为漏斗数据 - 下单用户数
     */
    Long getOrderUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户行为漏斗数据 - 支付用户数
     */
    Long getPaymentUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取用户访问时段分布
     */
    List<Map<String, Object>> getUserVisitTimeDistribution(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate);

    /**
     * 获取上期新增用户数（用于计算增长率）
     */
    Long getLastPeriodNewUserCount(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取总页面浏览量
     */
    Long getTotalPageViews(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取独立访客数
     */
    Long getUniqueVisitors(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取跳出率
     */
    Double getBounceRate(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取平均停留时间
     */
    Double getAvgStayTime(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /**
     * 获取转化漏斗数据
     */
    List<Map<String, Object>> getFunnelData(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}
