package com.macro.mall.service.impl;

import com.macro.mall.dao.AnalyticsDao;
import com.macro.mall.dto.OrderStatusDistributionDto;
import com.macro.mall.dto.PaymentDistributionDto;
import com.macro.mall.dto.SalesAnalyticsDto;
import com.macro.mall.dto.UserPortraitDto;
import com.macro.mall.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 销售分析Service实现类
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    @Autowired
    private AnalyticsDao analyticsDao;

    @Override
    public SalesAnalyticsDto getSalesAnalytics(Date startDate, Date endDate, String type) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        SalesAnalyticsDto result = new SalesAnalyticsDto();

        // 获取基础统计数据
        BigDecimal totalSales = analyticsDao.getTotalSales(startDate, endDate);
        Long totalOrderCount = analyticsDao.getTotalOrderCount(startDate, endDate);
        BigDecimal refundAmount = analyticsDao.getRefundAmount(startDate, endDate);
        Long refundOrderCount = analyticsDao.getRefundOrderCount(startDate, endDate);

        result.setTotalSales(totalSales != null ? totalSales : BigDecimal.ZERO);
        result.setTotalOrderCount(totalOrderCount != null ? totalOrderCount : 0L);
        result.setRefundAmount(refundAmount != null ? refundAmount : BigDecimal.ZERO);

        // 计算客单价
        BigDecimal avgOrderAmount = BigDecimal.ZERO;
        if (totalOrderCount != null && totalOrderCount > 0 && totalSales != null) {
            avgOrderAmount = totalSales.divide(new BigDecimal(totalOrderCount), 2, RoundingMode.HALF_UP);
        }
        result.setAvgOrderAmount(avgOrderAmount);

        // 计算退款率
        Double refundRate = 0.0;
        if (totalOrderCount != null && totalOrderCount > 0 && refundOrderCount != null) {
            refundRate = (refundOrderCount.doubleValue() / totalOrderCount.doubleValue()) * 100;
        }
        result.setRefundRate(Math.round(refundRate * 10.0) / 10.0);

        // 计算同比增长率
        BigDecimal lastYearTotalSales = analyticsDao.getLastYearTotalSales(startDate, endDate);
        Long lastYearTotalOrderCount = analyticsDao.getLastYearTotalOrderCount(startDate, endDate);

        Double salesGrowthRate = calculateGrowthRate(totalSales, lastYearTotalSales);
        Double orderGrowthRate = calculateGrowthRate(totalOrderCount, lastYearTotalOrderCount);

        result.setSalesGrowthRate(salesGrowthRate);
        result.setOrderGrowthRate(orderGrowthRate);

        // 计算客单价同比增长率
        BigDecimal lastYearAvgOrderAmount = BigDecimal.ZERO;
        if (lastYearTotalOrderCount != null && lastYearTotalOrderCount > 0 && lastYearTotalSales != null) {
            lastYearAvgOrderAmount = lastYearTotalSales.divide(new BigDecimal(lastYearTotalOrderCount), 2, RoundingMode.HALF_UP);
        }
        Double avgOrderAmountGrowthRate = calculateGrowthRate(avgOrderAmount, lastYearAvgOrderAmount);
        result.setAvgOrderAmountGrowthRate(avgOrderAmountGrowthRate);

        // 获取时间序列数据
        List<Map<String, Object>> salesData = analyticsDao.getSalesData(startDate, endDate, type);
        List<Map<String, Object>> orderCountData = analyticsDao.getOrderCountData(startDate, endDate, type);

        List<String> dateList = new ArrayList<>();
        List<BigDecimal> salesList = new ArrayList<>();
        List<Long> orderCountList = new ArrayList<>();

        // 处理销售额数据
        Map<String, BigDecimal> salesMap = new HashMap<>();
        for (Map<String, Object> item : salesData) {
            String date = item.get("date").toString();
            BigDecimal value = (BigDecimal) item.get("value");
            salesMap.put(date, value);
        }

        // 处理订单数量数据
        Map<String, Long> orderCountMap = new HashMap<>();
        for (Map<String, Object> item : orderCountData) {
            String date = item.get("date").toString();
            Long value = ((Number) item.get("value")).longValue();
            orderCountMap.put(date, value);
        }

        // 生成完整的日期列表并填充数据
        Set<String> allDates = new HashSet<>();
        allDates.addAll(salesMap.keySet());
        allDates.addAll(orderCountMap.keySet());

        List<String> sortedDates = new ArrayList<>(allDates);
        Collections.sort(sortedDates);

        for (String date : sortedDates) {
            dateList.add(date);
            salesList.add(salesMap.getOrDefault(date, BigDecimal.ZERO));
            orderCountList.add(orderCountMap.getOrDefault(date, 0L));
        }

        result.setDateList(dateList);
        result.setSalesList(salesList);
        result.setOrderCountList(orderCountList);

        return result;
    }

    @Override
    public Map<String, Object> getSalesChannelDistribution(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        List<Map<String, Object>> channelData = analyticsDao.getSalesChannelDistribution(startDate, endDate);
        Map<String, Object> result = new HashMap<>();

        for (Map<String, Object> item : channelData) {
            String channel = (String) item.get("channel");
            BigDecimal amount = (BigDecimal) item.get("amount");
            result.put(channel, amount);
        }

        // 确保所有渠道都有数据
        result.putIfAbsent("pc", BigDecimal.ZERO);
        result.putIfAbsent("app", BigDecimal.ZERO);
        result.putIfAbsent("wechat", BigDecimal.ZERO);
        result.putIfAbsent("others", BigDecimal.ZERO);

        return result;
    }

    @Override
    public Map<String, Object> getUserRegionDistribution(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        List<Map<String, Object>> regionData = analyticsDao.getUserRegionDistribution(startDate, endDate);
        Map<String, Object> result = new HashMap<>();

        for (Map<String, Object> item : regionData) {
            String region = (String) item.get("region");
            BigDecimal amount = (BigDecimal) item.get("amount");
            result.put(region, amount);
        }

        result.put("count", regionData.size());

        return result;
    }

    @Override
    public List<PaymentDistributionDto> getPaymentDistribution(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        List<Map<String, Object>> paymentData = analyticsDao.getPaymentDistribution(startDate, endDate);
        List<PaymentDistributionDto> result = new ArrayList<>();

        // 计算总订单数
        long totalOrders = 0;
        for (Map<String, Object> item : paymentData) {
            Long value = ((Number) item.get("value")).longValue();
            totalOrders += value;
        }

        // 构建结果数据
        for (Map<String, Object> item : paymentData) {
            PaymentDistributionDto dto = new PaymentDistributionDto();
            dto.setName((String) item.get("name"));
            Long value = ((Number) item.get("value")).longValue();
            dto.setValue(value);

            // 计算百分比
            Double percentage = 0.0;
            if (totalOrders > 0) {
                percentage = (value.doubleValue() / totalOrders) * 100;
                percentage = Math.round(percentage * 10.0) / 10.0; // 保留1位小数
            }
            dto.setPercentage(percentage);

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<OrderStatusDistributionDto> getOrderStatusDistribution(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        List<Map<String, Object>> statusData = analyticsDao.getOrderStatusDistribution(startDate, endDate);
        List<OrderStatusDistributionDto> result = new ArrayList<>();

        // 计算总订单数
        long totalOrders = 0;
        for (Map<String, Object> item : statusData) {
            Long value = ((Number) item.get("value")).longValue();
            totalOrders += value;
        }

        // 构建结果数据
        for (Map<String, Object> item : statusData) {
            OrderStatusDistributionDto dto = new OrderStatusDistributionDto();
            dto.setName((String) item.get("name"));
            Long value = ((Number) item.get("value")).longValue();
            dto.setValue(value);

            // 计算百分比
            Double percentage = 0.0;
            if (totalOrders > 0) {
                percentage = (value.doubleValue() / totalOrders) * 100;
                percentage = Math.round(percentage * 10.0) / 10.0; // 保留1位小数
            }
            dto.setPercentage(percentage);

            result.add(dto);
        }

        return result;
    }

    @Override
    public UserPortraitDto getUserPortrait(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        UserPortraitDto result = new UserPortraitDto();

        // 获取基础统计数据
        Long totalUsers = analyticsDao.getTotalUserCount(startDate, endDate);
        Long analyzedUsers = analyticsDao.getAnalyzedUserCount(startDate, endDate);

        result.setAnalyzedUsers(analyzedUsers != null ? analyzedUsers : 0L);

        // 计算覆盖率
        Double coverageRate = 0.0;
        if (totalUsers != null && totalUsers > 0 && analyzedUsers != null) {
            coverageRate = (analyzedUsers.doubleValue() / totalUsers.doubleValue()) * 100;
            coverageRate = Math.round(coverageRate * 100.0) / 100.0; // 保留2位小数
        }
        result.setCoverageRate(coverageRate);

        // 获取用户消费相关数据
        BigDecimal avgOrderAmount = analyticsDao.getUserAvgOrderAmount(startDate, endDate);
        BigDecimal avgUserSpend = analyticsDao.getUserAvgSpend(startDate, endDate);

        result.setAvgOrderAmount(avgOrderAmount != null ? avgOrderAmount : BigDecimal.ZERO);
        result.setAvgUserSpend(avgUserSpend != null ? avgUserSpend : BigDecimal.ZERO);

        // 获取复购率数据
        Map<String, Object> repurchaseStats = analyticsDao.getRepurchaseRateStats(startDate, endDate);
        Double repurchaseRate = 0.0;
        if (repurchaseStats != null) {
            Long totalOrderUsers = ((Number) repurchaseStats.getOrDefault("totalUsers", 0)).longValue();
            Long repurchaseUsers = ((Number) repurchaseStats.getOrDefault("repurchaseUsers", 0)).longValue();
            if (totalOrderUsers > 0) {
                repurchaseRate = (repurchaseUsers.doubleValue() / totalOrderUsers.doubleValue()) * 100;
                repurchaseRate = Math.round(repurchaseRate * 100.0) / 100.0;
            }
        }
        result.setRepurchaseRate(repurchaseRate);

        // 计算忠诚度评分（基于复购率、平均消费等）
        Double loyaltyScore = calculateLoyaltyScore(repurchaseRate, avgUserSpend);
        result.setLoyaltyScore(loyaltyScore);

        // 获取性别分布
        List<Map<String, Object>> genderData = analyticsDao.getUserGenderDistribution(startDate, endDate);
        List<UserPortraitDto.GenderDistribution> genderDistribution = new ArrayList<>();
        String mainUserGroup = "未知用户群体";
        Double mainUserGroupRate = 0.0;
        Long maxGenderCount = 0L;

        for (Map<String, Object> item : genderData) {
            String gender = (String) item.get("gender");
            Long count = ((Number) item.get("count")).longValue();
            genderDistribution.add(new UserPortraitDto.GenderDistribution(gender, count));

            if (count > maxGenderCount) {
                maxGenderCount = count;
                mainUserGroup = gender + "用户";
            }
        }

        if (analyzedUsers > 0) {
            mainUserGroupRate = (maxGenderCount.doubleValue() / analyzedUsers.doubleValue()) * 100;
            mainUserGroupRate = Math.round(mainUserGroupRate * 100.0) / 100.0;
        }

        result.setGenderDistribution(genderDistribution);
        result.setMainUserGroup(mainUserGroup);
        result.setMainUserGroupRate(mainUserGroupRate);

        // 获取年龄分布
        List<Map<String, Object>> ageData = analyticsDao.getUserAgeDistribution(startDate, endDate);
        List<UserPortraitDto.AgeDistribution> ageDistribution = new ArrayList<>();
        for (Map<String, Object> item : ageData) {
            String ageRange = (String) item.get("ageRange");
            Long count = ((Number) item.get("count")).longValue();
            ageDistribution.add(new UserPortraitDto.AgeDistribution(ageRange, count));
        }
        result.setAgeDistribution(ageDistribution);

        // 获取职业分布
        List<Map<String, Object>> occupationData = analyticsDao.getUserOccupationDistribution(startDate, endDate);
        List<UserPortraitDto.OccupationDistribution> occupationDistribution = new ArrayList<>();
        for (Map<String, Object> item : occupationData) {
            String occupation = (String) item.get("occupation");
            Long count = ((Number) item.get("count")).longValue();
            occupationDistribution.add(new UserPortraitDto.OccupationDistribution(occupation, count));
        }
        result.setOccupationDistribution(occupationDistribution);

        // 获取地域分布
        List<Map<String, Object>> regionData = analyticsDao.getUserCityDistribution(startDate, endDate);
        Map<String, Long> regionDistribution = new HashMap<>();
        for (Map<String, Object> item : regionData) {
            String region = (String) item.get("region");
            Long count = ((Number) item.get("count")).longValue();
            regionDistribution.put(region, count);
        }
        result.setRegionDistribution(regionDistribution);

        // 获取消费能力分布
        List<Map<String, Object>> consumptionData = analyticsDao.getUserConsumptionLevelDistribution(startDate, endDate);
        List<UserPortraitDto.ConsumptionLevelDistribution> consumptionLevelDistribution = new ArrayList<>();
        Long totalConsumptionUsers = consumptionData.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .sum();

        for (Map<String, Object> item : consumptionData) {
            String level = (String) item.get("level");
            Long count = ((Number) item.get("count")).longValue();
            Double percentage = 0.0;
            if (totalConsumptionUsers > 0) {
                percentage = (count.doubleValue() / totalConsumptionUsers.doubleValue()) * 100;
                percentage = Math.round(percentage * 100.0) / 100.0;
            }
            consumptionLevelDistribution.add(new UserPortraitDto.ConsumptionLevelDistribution(level, count, percentage));
        }
        result.setConsumptionLevelDistribution(consumptionLevelDistribution);

        // 获取兴趣标签
        List<Map<String, Object>> interestData = analyticsDao.getUserInterestTags(startDate, endDate);
        List<UserPortraitDto.InterestTag> interestTags = new ArrayList<>();
        Long maxInterestCount = interestData.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .max().orElse(1L);

        for (Map<String, Object> item : interestData) {
            String tag = (String) item.get("tag");
            Long count = ((Number) item.get("count")).longValue();
            Integer weight = Math.round((count.floatValue() / maxInterestCount.floatValue()) * 100);
            interestTags.add(new UserPortraitDto.InterestTag(tag, count, weight));
        }
        result.setInterestTags(interestTags);

        // 获取消费偏好
        List<Map<String, Object>> preferenceData = analyticsDao.getUserConsumptionPreferences(startDate, endDate);
        List<UserPortraitDto.ConsumptionPreference> consumptionPreferences = new ArrayList<>();
        BigDecimal totalPreferenceAmount = preferenceData.stream()
                .map(item -> (BigDecimal) item.get("amount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map<String, Object> item : preferenceData) {
            String category = (String) item.get("category");
            BigDecimal amount = (BigDecimal) item.get("amount");
            Double percentage = 0.0;
            if (totalPreferenceAmount.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalPreferenceAmount, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue();
                percentage = Math.round(percentage * 100.0) / 100.0;
            }
            consumptionPreferences.add(new UserPortraitDto.ConsumptionPreference(category, amount, percentage));
        }
        result.setConsumptionPreferences(consumptionPreferences);

        // 获取设备分布
        List<Map<String, Object>> deviceData = analyticsDao.getUserDeviceDistribution(startDate, endDate);
        List<UserPortraitDto.DeviceDistribution> deviceDistribution = new ArrayList<>();
        Long totalDeviceUsers = deviceData.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .sum();

        for (Map<String, Object> item : deviceData) {
            String device = (String) item.get("device");
            Long count = ((Number) item.get("count")).longValue();
            Double percentage = 0.0;
            if (totalDeviceUsers > 0) {
                percentage = (count.doubleValue() / totalDeviceUsers.doubleValue()) * 100;
                percentage = Math.round(percentage * 100.0) / 100.0;
            }
            deviceDistribution.add(new UserPortraitDto.DeviceDistribution(device, count, percentage));
        }
        result.setDeviceDistribution(deviceDistribution);

        // 获取活跃时段分布
        List<Map<String, Object>> activeTimeData = analyticsDao.getUserActiveTimeDistribution(startDate, endDate);
        List<UserPortraitDto.ActiveTimeDistribution> activeTimeDistribution = new ArrayList<>();
        for (Map<String, Object> item : activeTimeData) {
            Object hourObj = item.get("hour");
            Integer hour = hourObj != null ? ((Number) hourObj).intValue() : 0;
            Long count = ((Number) item.get("count")).longValue();
            activeTimeDistribution.add(new UserPortraitDto.ActiveTimeDistribution(hour.toString(), count));
        }
        result.setActiveTimeDistribution(activeTimeDistribution);

        return result;
    }


    /**
     * 根据库存区间判断库存状态
     */
    private String getStockStatus(String range) {
        switch (range) {
            case "0-10":
                return "低库存";
            case "10-50":
            case "50-100":
                return "正常";
            case "100-500":
            case "500+":
                return "充足";
            default:
                return "未知";
        }
    }

    /**
     * 计算用户忠诚度评分
     * @param repurchaseRate 复购率
     * @param avgUserSpend 人均消费
     * @return 忠诚度评分 (0-10)
     */
    private Double calculateLoyaltyScore(Double repurchaseRate, BigDecimal avgUserSpend) {
        double score = 0.0;

        // 复购率权重 60%
        if (repurchaseRate != null) {
            score += (repurchaseRate / 100.0) * 6.0;
        }

        // 人均消费权重 40% (假设1000元为满分基准)
        if (avgUserSpend != null) {
            double spendScore = Math.min(avgUserSpend.doubleValue() / 1000.0, 1.0) * 4.0;
            score += spendScore;
        }

        return Math.round(score * 10.0) / 10.0; // 保留1位小数
    }

    /**
     * 计算增长率
     */
    private Double calculateGrowthRate(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        BigDecimal growthRate = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
        return Math.round(growthRate.doubleValue() * 10.0) / 10.0; // 保留1位小数
    }

    /**
     * 计算增长率（Long类型）
     */
    private Double calculateGrowthRate(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        double growthRate = ((current.doubleValue() - previous.doubleValue()) / previous.doubleValue()) * 100;
        return Math.round(growthRate * 10.0) / 10.0; // 保留1位小数
    }
}
