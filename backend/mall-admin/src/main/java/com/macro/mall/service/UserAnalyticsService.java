package com.macro.mall.service;

import com.macro.mall.dto.*;

import java.util.Date;
import java.util.List;

/**
 * 用户分析服务接口
 */
public interface UserAnalyticsService {

    /**
     * 获取用户概览数据
     */
    UserOverviewDto getUserOverview(Date startDate, Date endDate, String type);

    /**
     * 获取用户留存率数据
     */
    List<UserRetentionDto> getUserRetention(Date startDate, Date endDate);

    /**
     * 获取用户活跃度分布
     */
    List<UserActivityDistributionDto> getUserActivityDistribution(Date startDate, Date endDate);

    /**
     * 获取用户行为分析
     */
    UserBehaviorDto getUserBehavior(Date startDate, Date endDate);

    /**
     * 获取用户行为分析（新版本）
     */
    UserBehaviorAnalysisDTO getUserBehavior(String startDate, String endDate);

    /**
     * 获取用户访问时段分布
     */
    List<UserVisitTimeDistributionDto> getUserVisitTimeDistribution(Date startDate, Date endDate);
    
    /**
     * 获取系统中的用户总数
     * @return 用户总数
     */
    Long getTotalUserCount();
}
