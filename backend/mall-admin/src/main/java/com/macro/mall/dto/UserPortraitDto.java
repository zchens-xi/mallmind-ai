package com.macro.mall.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户画像分析DTO
 */
public class UserPortraitDto {

    // 概览数据
    private Long analyzedUsers;                    // 分析用户数
    private Double coverageRate;                   // 覆盖率(%)
    private String mainUserGroup;                  // 主要用户群体
    private Double mainUserGroupRate;              // 主要用户群体占比(%)
    private BigDecimal avgOrderAmount;             // 平均客单价
    private BigDecimal avgUserSpend;               // 人均消费
    private Double loyaltyScore;                   // 用户忠诚度评分(0-10)
    private Double repurchaseRate;                 // 复购率(%)

    // 用户性别分布
    private List<GenderDistribution> genderDistribution;

    // 用户年龄分布
    private List<AgeDistribution> ageDistribution;

    // 用户职业分布
    private List<OccupationDistribution> occupationDistribution;

    // 用户地域分布 (地图数据格式)
    private Map<String, Long> regionDistribution;

    // 用户消费能力分布
    private List<ConsumptionLevelDistribution> consumptionLevelDistribution;

    // 用户兴趣标签 (词云数据)
    private List<InterestTag> interestTags;

    // 用户消费偏好
    private List<ConsumptionPreference> consumptionPreferences;

    // 用户设备分布
    private List<DeviceDistribution> deviceDistribution;

    // 用户活跃时段分布
    private List<ActiveTimeDistribution> activeTimeDistribution;

    public static class GenderDistribution {
        private String gender;
        private Long count;

        public GenderDistribution() {}

        public GenderDistribution(String gender, Long count) {
            this.gender = gender;
            this.count = count;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public static class AgeDistribution {
        private String ageRange;
        private Long count;

        public AgeDistribution() {}

        public AgeDistribution(String ageRange, Long count) {
            this.ageRange = ageRange;
            this.count = count;
        }

        public String getAgeRange() {
            return ageRange;
        }

        public void setAgeRange(String ageRange) {
            this.ageRange = ageRange;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public static class OccupationDistribution {
        private String occupation;
        private Long count;

        public OccupationDistribution() {}

        public OccupationDistribution(String occupation, Long count) {
            this.occupation = occupation;
            this.count = count;
        }

        public String getOccupation() {
            return occupation;
        }

        public void setOccupation(String occupation) {
            this.occupation = occupation;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    public static class ConsumptionLevelDistribution {
        private String level;
        private Long count;
        private Double percentage;

        public ConsumptionLevelDistribution() {}

        public ConsumptionLevelDistribution(String level, Long count, Double percentage) {
            this.level = level;
            this.count = count;
            this.percentage = percentage;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class InterestTag {
        private String tag;
        private Long count;
        private Integer weight;

        public InterestTag() {}

        public InterestTag(String tag, Long count, Integer weight) {
            this.tag = tag;
            this.count = count;
            this.weight = weight;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Integer getWeight() {
            return weight;
        }

        public void setWeight(Integer weight) {
            this.weight = weight;
        }
    }

    public static class ConsumptionPreference {
        private String category;
        private BigDecimal amount;
        private Double percentage;

        public ConsumptionPreference() {}

        public ConsumptionPreference(String category, BigDecimal amount, Double percentage) {
            this.category = category;
            this.amount = amount;
            this.percentage = percentage;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class DeviceDistribution {
        private String device;
        private Long count;
        private Double percentage;

        public DeviceDistribution() {}

        public DeviceDistribution(String device, Long count, Double percentage) {
            this.device = device;
            this.count = count;
            this.percentage = percentage;
        }

        public String getDevice() {
            return device;
        }

        public void setDevice(String device) {
            this.device = device;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public Double getPercentage() {
            return percentage;
        }

        public void setPercentage(Double percentage) {
            this.percentage = percentage;
        }
    }

    public static class ActiveTimeDistribution {
        private String hour;
        private Long count;

        public ActiveTimeDistribution() {}

        public ActiveTimeDistribution(String hour, Long count) {
            this.hour = hour;
            this.count = count;
        }

        public String getHour() {
            return hour;
        }

        public void setHour(String hour) {
            this.hour = hour;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    // Getters and Setters for main fields
    public Long getAnalyzedUsers() {
        return analyzedUsers;
    }

    public void setAnalyzedUsers(Long analyzedUsers) {
        this.analyzedUsers = analyzedUsers;
    }

    public Double getCoverageRate() {
        return coverageRate;
    }

    public void setCoverageRate(Double coverageRate) {
        this.coverageRate = coverageRate;
    }

    public String getMainUserGroup() {
        return mainUserGroup;
    }

    public void setMainUserGroup(String mainUserGroup) {
        this.mainUserGroup = mainUserGroup;
    }

    public Double getMainUserGroupRate() {
        return mainUserGroupRate;
    }

    public void setMainUserGroupRate(Double mainUserGroupRate) {
        this.mainUserGroupRate = mainUserGroupRate;
    }

    public BigDecimal getAvgOrderAmount() {
        return avgOrderAmount;
    }

    public void setAvgOrderAmount(BigDecimal avgOrderAmount) {
        this.avgOrderAmount = avgOrderAmount;
    }

    public BigDecimal getAvgUserSpend() {
        return avgUserSpend;
    }

    public void setAvgUserSpend(BigDecimal avgUserSpend) {
        this.avgUserSpend = avgUserSpend;
    }

    public Double getLoyaltyScore() {
        return loyaltyScore;
    }

    public void setLoyaltyScore(Double loyaltyScore) {
        this.loyaltyScore = loyaltyScore;
    }

    public Double getRepurchaseRate() {
        return repurchaseRate;
    }

    public void setRepurchaseRate(Double repurchaseRate) {
        this.repurchaseRate = repurchaseRate;
    }

    public List<GenderDistribution> getGenderDistribution() {
        return genderDistribution;
    }

    public void setGenderDistribution(List<GenderDistribution> genderDistribution) {
        this.genderDistribution = genderDistribution;
    }

    public List<AgeDistribution> getAgeDistribution() {
        return ageDistribution;
    }

    public void setAgeDistribution(List<AgeDistribution> ageDistribution) {
        this.ageDistribution = ageDistribution;
    }

    public List<OccupationDistribution> getOccupationDistribution() {
        return occupationDistribution;
    }

    public void setOccupationDistribution(List<OccupationDistribution> occupationDistribution) {
        this.occupationDistribution = occupationDistribution;
    }

    public Map<String, Long> getRegionDistribution() {
        return regionDistribution;
    }

    public void setRegionDistribution(Map<String, Long> regionDistribution) {
        this.regionDistribution = regionDistribution;
    }

    public List<ConsumptionLevelDistribution> getConsumptionLevelDistribution() {
        return consumptionLevelDistribution;
    }

    public void setConsumptionLevelDistribution(List<ConsumptionLevelDistribution> consumptionLevelDistribution) {
        this.consumptionLevelDistribution = consumptionLevelDistribution;
    }

    public List<InterestTag> getInterestTags() {
        return interestTags;
    }

    public void setInterestTags(List<InterestTag> interestTags) {
        this.interestTags = interestTags;
    }

    public List<ConsumptionPreference> getConsumptionPreferences() {
        return consumptionPreferences;
    }

    public void setConsumptionPreferences(List<ConsumptionPreference> consumptionPreferences) {
        this.consumptionPreferences = consumptionPreferences;
    }

    public List<DeviceDistribution> getDeviceDistribution() {
        return deviceDistribution;
    }

    public void setDeviceDistribution(List<DeviceDistribution> deviceDistribution) {
        this.deviceDistribution = deviceDistribution;
    }

    public List<ActiveTimeDistribution> getActiveTimeDistribution() {
        return activeTimeDistribution;
    }

    public void setActiveTimeDistribution(List<ActiveTimeDistribution> activeTimeDistribution) {
        this.activeTimeDistribution = activeTimeDistribution;
    }
}
