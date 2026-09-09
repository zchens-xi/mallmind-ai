package com.macro.mall.dao;

import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 数据分析Dao
 */
public interface AnalyticsDao {
    
    /**
     * 获取销售额数据
     */
    List<Map<String, Object>> getSalesData(@Param("startDate") Date startDate, 
                                          @Param("endDate") Date endDate, 
                                          @Param("type") String type);
    
    /**
     * 获取订单数量数据
     */
    List<Map<String, Object>> getOrderCountData(@Param("startDate") Date startDate, 
                                               @Param("endDate") Date endDate, 
                                               @Param("type") String type);
    
    /**
     * 获取销售总额
     */
    BigDecimal getTotalSales(@Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    /**
     * 获取订单总数
     */
    Long getTotalOrderCount(@Param("startDate") Date startDate, 
                           @Param("endDate") Date endDate);
    
    /**
     * 获取退款金额
     */
    BigDecimal getRefundAmount(@Param("startDate") Date startDate,
                              @Param("endDate") Date endDate);

    /**
     * 获取退款订单数
     */
    Long getRefundOrderCount(@Param("startDate") Date startDate,
                            @Param("endDate") Date endDate);

    /**
     * 获取销售渠道分布
     */
    List<Map<String, Object>> getSalesChannelDistribution(@Param("startDate") Date startDate,
                                                          @Param("endDate") Date endDate);

    /**
     * 获取用户地域分布
     */
    List<Map<String, Object>> getUserRegionDistribution(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate);

    /**
     * 获取支付方式分布
     */
    List<Map<String, Object>> getPaymentDistribution(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    /**
     * 获取订单状态分布
     */
    List<Map<String, Object>> getOrderStatusDistribution(@Param("startDate") Date startDate,
                                                         @Param("endDate") Date endDate);

    /**
     * 获取去年同期销售总额（用于计算增长率）
     */
    BigDecimal getLastYearTotalSales(@Param("startDate") Date startDate,
                                    @Param("endDate") Date endDate);

    /**
     * 获取去年同期订单总数（用于计算增长率）
     */
    Long getLastYearTotalOrderCount(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    // ============== 用户画像相关查询 ==============

    /**
     * 获取用户总数
     */
    Long getTotalUserCount(@Param("startDate") Date startDate,
                          @Param("endDate") Date endDate);

    /**
     * 获取有画像信息的用户数
     */
    Long getAnalyzedUserCount(@Param("startDate") Date startDate,
                             @Param("endDate") Date endDate);

    /**
     * 获取用户性别分布
     */
    List<Map<String, Object>> getUserGenderDistribution(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate);

    /**
     * 获取用户年龄分布
     */
    List<Map<String, Object>> getUserAgeDistribution(@Param("startDate") Date startDate,
                                                     @Param("endDate") Date endDate);

    /**
     * 获取用户职业分布
     */
    List<Map<String, Object>> getUserOccupationDistribution(@Param("startDate") Date startDate,
                                                            @Param("endDate") Date endDate);

    /**
     * 获取用户城市分布
     */
    List<Map<String, Object>> getUserCityDistribution(@Param("startDate") Date startDate,
                                                      @Param("endDate") Date endDate);

    /**
     * 获取用户消费能力分布
     */
    List<Map<String, Object>> getUserConsumptionLevelDistribution(@Param("startDate") Date startDate,
                                                                 @Param("endDate") Date endDate);

    /**
     * 获取用户兴趣标签（基于商品类别）
     */
    List<Map<String, Object>> getUserInterestTags(@Param("startDate") Date startDate,
                                                  @Param("endDate") Date endDate);

    /**
     * 获取用户消费偏好（按商品类别）
     */
    List<Map<String, Object>> getUserConsumptionPreferences(@Param("startDate") Date startDate,
                                                            @Param("endDate") Date endDate);

    /**
     * 获取用户设备分布
     */
    List<Map<String, Object>> getUserDeviceDistribution(@Param("startDate") Date startDate,
                                                        @Param("endDate") Date endDate);

    /**
     * 获取用户活跃时段分布
     */
    List<Map<String, Object>> getUserActiveTimeDistribution(@Param("startDate") Date startDate,
                                                           @Param("endDate") Date endDate);

    /**
     * 获取用户平均客单价
     */
    BigDecimal getUserAvgOrderAmount(@Param("startDate") Date startDate,
                                   @Param("endDate") Date endDate);

    /**
     * 获取用户人均消费
     */
    BigDecimal getUserAvgSpend(@Param("startDate") Date startDate,
                              @Param("endDate") Date endDate);

    /**
     * 获取复购率统计
     */
    Map<String, Object> getRepurchaseRateStats(@Param("startDate") Date startDate,
                                              @Param("endDate") Date endDate);
}
