package com.macro.mall.service;

import java.util.List;
import java.util.Map;

/**
 * 首页内容管理Service
 */
public interface HomeService {
    /**
     * 获取首页数据
     * @param timeRange 时间范围：week-近一周，month-近一月，year-近一年
     */
    Map<String, Object> getHomeData(String timeRange);

    /**
     * 获取转化漏斗数据
     */
    List<Map<String, Object>> getFunnelData();
} 