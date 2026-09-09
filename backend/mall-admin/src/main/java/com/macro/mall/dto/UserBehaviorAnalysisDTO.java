package com.macro.mall.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户行为分析DTO
 */
public class UserBehaviorAnalysisDTO {
    private Long pageViews; // 页面浏览量
    private Long visitors; // 独立访客数
    private Double pageViewsPerVisitor; // 人均浏览量
    private Double bounceRate; // 跳出率
    private Double avgStayTime; // 平均停留时长(秒)
    private List<Map<String, Object>> funnelData; // 转化漏斗数据
    private String dataQualityWarning; // 数据质量警告信息

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

    public String getDataQualityWarning() {
        return dataQualityWarning;
    }

    public void setDataQualityWarning(String dataQualityWarning) {
        this.dataQualityWarning = dataQualityWarning;
    }
}
