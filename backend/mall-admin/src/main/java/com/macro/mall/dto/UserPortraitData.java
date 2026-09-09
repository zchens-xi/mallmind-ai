package com.macro.mall.dto;

import java.util.List;
import java.util.Map;

/**
 * 用户画像数据封装
 */
public class UserPortraitData {
    private Map<String, Object> genderDistribution; // 性别分布
    private Map<String, Object> ageDistribution; // 年龄分布
    private Map<String, Object> consumptionDistribution; // 消费水平分布
    private List<Map<String, Object>> interestDistribution; // 兴趣分布
    private Map<String, Object> deviceDistribution; // 设备分布
    private Map<String, Object> activeTimeDistribution; // 活跃时间分布
    private List<Map<String, Object>> regionDistribution; // 地区分布
    private List<Map<String, Object>> memberLevelDistribution; // 会员等级分布
    private List<Map<String, Object>> purchaseFrequency; // 购买频次分布
    private List<Map<String, Object>> lifeCycle; // 用户生命周期分布

    public Map<String, Object> getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(Map<String, Object> genderDistribution) {
        this.genderDistribution = genderDistribution;
    }

    public Map<String, Object> getAgeDistribution() {
        return ageDistribution;
    }

    public void setAgeDistribution(Map<String, Object> ageDistribution) {
        this.ageDistribution = ageDistribution;
    }

    public Map<String, Object> getConsumptionDistribution() {
        return consumptionDistribution;
    }

    public void setConsumptionDistribution(Map<String, Object> consumptionDistribution) {
        this.consumptionDistribution = consumptionDistribution;
    }

    public List<Map<String, Object>> getInterestDistribution() {
        return interestDistribution;
    }

    public void setInterestDistribution(List<Map<String, Object>> interestDistribution) {
        this.interestDistribution = interestDistribution;
    }

    public Map<String, Object> getDeviceDistribution() {
        return deviceDistribution;
    }

    public void setDeviceDistribution(Map<String, Object> deviceDistribution) {
        this.deviceDistribution = deviceDistribution;
    }

    public Map<String, Object> getActiveTimeDistribution() {
        return activeTimeDistribution;
    }

    public void setActiveTimeDistribution(Map<String, Object> activeTimeDistribution) {
        this.activeTimeDistribution = activeTimeDistribution;
    }

    public List<Map<String, Object>> getRegionDistribution() {
        return regionDistribution;
    }

    public void setRegionDistribution(List<Map<String, Object>> regionDistribution) {
        this.regionDistribution = regionDistribution;
    }

    public List<Map<String, Object>> getMemberLevelDistribution() {
        return memberLevelDistribution;
    }

    public void setMemberLevelDistribution(List<Map<String, Object>> memberLevelDistribution) {
        this.memberLevelDistribution = memberLevelDistribution;
    }

    public List<Map<String, Object>> getPurchaseFrequency() {
        return purchaseFrequency;
    }

    public void setPurchaseFrequency(List<Map<String, Object>> purchaseFrequency) {
        this.purchaseFrequency = purchaseFrequency;
    }

    public List<Map<String, Object>> getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(List<Map<String, Object>> lifeCycle) {
        this.lifeCycle = lifeCycle;
    }
} 