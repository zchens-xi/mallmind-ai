package com.macro.mall.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户行为分析结果封装
 */
public class BehaviorAnalyticsResult {
    private Long pageViews; // 页面浏览量
    private Long visitors; // 独立访客数
    private Double pageViewsPerVisitor; // 人均浏览量
    private Double bounceRate; // 跳出率
    private Double avgStayTime; // 平均停留时长(秒)
    private List<Map<String, Object>> funnelData; // 转化漏斗数据
    private List<Map<String, Object>> visitTrend; // 访问趋势
    private List<Map<String, Object>> pagePvData; // 页面访问量数据
    private List<Map<String, Object>> deviceData; // 设备分布数据
    private List<Map<String, Object>> channelData; // 渠道分布数据

    public Long getPageViews() {
        return pageViews;
    }

    public void setPageViews(Long pageViews) {
        this.pageViews = pageViews;
    }

    public Long getVisitors() {
        return visitors;
    }

    public void setVisitors(Long visitors) {
        this.visitors = visitors;
    }

    public Double getPageViewsPerVisitor() {
        return pageViewsPerVisitor;
    }

    public void setPageViewsPerVisitor(Double pageViewsPerVisitor) {
        this.pageViewsPerVisitor = pageViewsPerVisitor;
    }

    public Double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(Double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public Double getAvgStayTime() {
        return avgStayTime;
    }

    public void setAvgStayTime(Double avgStayTime) {
        this.avgStayTime = avgStayTime;
    }

    public List<Map<String, Object>> getFunnelData() {
        return funnelData;
    }

    public void setFunnelData(List<Map<String, Object>> funnelData) {
        this.funnelData = funnelData;
    }

    public List<Map<String, Object>> getVisitTrend() {
        return visitTrend;
    }

    public void setVisitTrend(List<Map<String, Object>> visitTrend) {
        this.visitTrend = visitTrend;
    }

    public List<Map<String, Object>> getPagePvData() {
        return pagePvData;
    }

    public void setPagePvData(List<Map<String, Object>> pagePvData) {
        this.pagePvData = pagePvData;
    }

    public List<Map<String, Object>> getDeviceData() {
        return deviceData;
    }

    public void setDeviceData(List<Map<String, Object>> deviceData) {
        this.deviceData = deviceData;
    }

    public List<Map<String, Object>> getChannelData() {
        return channelData;
    }

    public void setChannelData(List<Map<String, Object>> channelData) {
        this.channelData = channelData;
    }
} 