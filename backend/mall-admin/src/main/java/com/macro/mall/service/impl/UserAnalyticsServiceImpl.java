package com.macro.mall.service.impl;

import com.macro.mall.dao.UserAnalyticsDao;
import com.macro.mall.dto.*;
import com.macro.mall.service.UserAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户分析服务实现类
 */
@Service
public class UserAnalyticsServiceImpl implements UserAnalyticsService {

    @Autowired
    private UserAnalyticsDao userAnalyticsDao;

    @Override
    public UserOverviewDto getUserOverview(Date startDate, Date endDate, String type) {
        UserOverviewDto overview = new UserOverviewDto();

        // 获取基础统计数据
        Long totalUsers = userAnalyticsDao.getTotalUserCount(endDate);
        Long newUsers = userAnalyticsDao.getNewUserCount(startDate, endDate);
        Long activeUsers = userAnalyticsDao.getActiveUserCount(startDate, endDate);
        Long paidUsers = userAnalyticsDao.getPaidUserCount(startDate, endDate);

        // 设置基础数据
        overview.setTotalUsers(totalUsers != null ? totalUsers : 0L);
        overview.setNewUsers(newUsers != null ? newUsers : 0L);
        overview.setActiveUsers(activeUsers != null ? activeUsers : 0L);
        overview.setPaidUsers(paidUsers != null ? paidUsers : 0L);

        // 计算比率
        if (totalUsers != null && totalUsers > 0) {
            overview.setActiveRate(BigDecimal.valueOf(activeUsers * 100.0 / totalUsers)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue());
            overview.setPaidRate(BigDecimal.valueOf(paidUsers * 100.0 / totalUsers)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            overview.setActiveRate(0.0);
            overview.setPaidRate(0.0);
        }

        // 计算用户增长率
        Long lastPeriodNewUsers = userAnalyticsDao.getLastPeriodNewUserCount(startDate, endDate);
        if (lastPeriodNewUsers != null && lastPeriodNewUsers > 0) {
            double growthRate = ((newUsers - lastPeriodNewUsers) * 100.0) / lastPeriodNewUsers;
            overview.setUserGrowthRate(BigDecimal.valueOf(growthRate)
                    .setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            overview.setUserGrowthRate(0.0);
        }

        // 计算日均新增用户数
        long daysBetween = (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24) + 1;
        overview.setAvgNewUsers(BigDecimal.valueOf(newUsers * 1.0 / daysBetween)
                .setScale(2, RoundingMode.HALF_UP).doubleValue());

        // 获取趋势数据
        List<Map<String, Object>> newUserTrend = userAnalyticsDao.getNewUserTrendData(startDate, endDate, type);
        List<Map<String, Object>> activeUserTrend = userAnalyticsDao.getActiveUserTrendData(startDate, endDate, type);

        // 处理趋势数据
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        List<String> dateList = new ArrayList<>();
        List<Long> newUsersList = new ArrayList<>();
        List<Long> activeUsersList = new ArrayList<>();
        List<Long> totalUsersList = new ArrayList<>();

        // 填充新增用户数据
        Map<String, Long> newUserMap = newUserTrend.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> ((Number) m.get("value")).longValue(),
                        (v1, v2) -> v1
                ));

        // 填充活跃用户数据
        Map<String, Long> activeUserMap = activeUserTrend.stream()
                .collect(Collectors.toMap(
                        m -> m.get("date").toString(),
                        m -> ((Number) m.get("value")).longValue(),
                        (v1, v2) -> v1
                ));

        // 生成完整的日期序列和对应数据
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long runningTotal = totalUsers - newUsers; // 开始时的累计用户数

        while (!calendar.getTime().after(endDate)) {
            String dateStr = dateFormat.format(calendar.getTime());
            dateList.add(dateStr);

            Long newCount = newUserMap.getOrDefault(dateStr, 0L);
            Long activeCount = activeUserMap.getOrDefault(dateStr, 0L);

            newUsersList.add(newCount);
            activeUsersList.add(activeCount);
            runningTotal += newCount;
            totalUsersList.add(runningTotal);

            if ("day".equals(type)) {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            } else if ("week".equals(type)) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
            } else if ("month".equals(type)) {
                calendar.add(Calendar.MONTH, 1);
            } else {
                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        overview.setDateList(dateList);
        overview.setNewUsersList(newUsersList);
        overview.setActiveUsersList(activeUsersList);
        overview.setTotalUsersList(totalUsersList);

        return overview;
    }

    @Override
    public List<UserRetentionDto> getUserRetention(Date startDate, Date endDate) {
        List<Map<String, Object>> retentionData = userAnalyticsDao.getRetentionData(startDate, endDate);

        return retentionData.stream().map(data -> {
            UserRetentionDto dto = new UserRetentionDto();
            dto.setDate(data.get("date").toString());
            dto.setNextDayRate(((Number) data.get("nextDayRate")).doubleValue());
            dto.setDay7Rate(((Number) data.get("day7Rate")).doubleValue());
            dto.setDay30Rate(((Number) data.get("day30Rate")).doubleValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<UserActivityDistributionDto> getUserActivityDistribution(Date startDate, Date endDate) {
        List<Map<String, Object>> distributionData = userAnalyticsDao.getUserActivityDistribution(startDate, endDate);

        return distributionData.stream().map(data -> {
            UserActivityDistributionDto dto = new UserActivityDistributionDto();
            dto.setLevel(data.get("level").toString());
            dto.setCount(((Number) data.get("count")).longValue());
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public UserBehaviorDto getUserBehavior(Date startDate, Date endDate) {
        UserBehaviorDto behavior = new UserBehaviorDto();

        // 获取各阶段用户数
        Long visitUsers = userAnalyticsDao.getVisitUserCount(startDate, endDate);
        Long viewProductUsers = userAnalyticsDao.getViewProductUserCount(startDate, endDate);
        Long addToCartUsers = userAnalyticsDao.getAddToCartUserCount(startDate, endDate);
        Long orderUsers = userAnalyticsDao.getOrderUserCount(startDate, endDate);
        Long paymentUsers = userAnalyticsDao.getPaymentUserCount(startDate, endDate);

        List<UserBehaviorDto.FunnelData> funnelData = new ArrayList<>();

        UserBehaviorDto.FunnelData visit = new UserBehaviorDto.FunnelData();
        visit.setStage("访问");
        visit.setValue(visitUsers != null ? visitUsers : 0L);
        funnelData.add(visit);

        UserBehaviorDto.FunnelData viewProduct = new UserBehaviorDto.FunnelData();
        viewProduct.setStage("浏览商品");
        viewProduct.setValue(viewProductUsers != null ? viewProductUsers : 0L);
        funnelData.add(viewProduct);

        UserBehaviorDto.FunnelData addToCart = new UserBehaviorDto.FunnelData();
        addToCart.setStage("加入购物车");
        addToCart.setValue(addToCartUsers != null ? addToCartUsers : 0L);
        funnelData.add(addToCart);

        UserBehaviorDto.FunnelData order = new UserBehaviorDto.FunnelData();
        order.setStage("下单");
        order.setValue(orderUsers != null ? orderUsers : 0L);
        funnelData.add(order);

        UserBehaviorDto.FunnelData payment = new UserBehaviorDto.FunnelData();
        payment.setStage("支付");
        payment.setValue(paymentUsers != null ? paymentUsers : 0L);
        funnelData.add(payment);

        behavior.setFunnelData(funnelData);
        return behavior;
    }

    @Override
    public Long getTotalUserCount() {
        // 获取系统中的实际用户总数
        return userAnalyticsDao.getTotalUserCount(new Date());
    }

    @Override
    public UserBehaviorAnalysisDTO getUserBehavior(String startDate, String endDate) {
        // 转换字符串日期为Date对象
        Date start = null, end = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            start = sdf.parse(startDate);
            end = sdf.parse(endDate);
            // 将结束日期设置为当天的23:59:59
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
            end = calendar.getTime();
        } catch (Exception e) {
            throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd 格式", e);
        }

        UserBehaviorAnalysisDTO result = new UserBehaviorAnalysisDTO();
        
        // 获取实际数据
        Long totalPageViews = userAnalyticsDao.getTotalPageViews(start, end);
        Long uniqueVisitors = userAnalyticsDao.getUniqueVisitors(start, end);
        
        result.setPageViews(totalPageViews != null ? totalPageViews : 0L);
        result.setVisitors(uniqueVisitors != null ? uniqueVisitors : 0L);
        
        // 计算人均浏览量
        if (uniqueVisitors != null && uniqueVisitors > 0) {
            result.setPageViewsPerVisitor((double) totalPageViews / uniqueVisitors);
        } else {
            result.setPageViewsPerVisitor(0.0);
        }
        
        // 获取跳出率和平均停留时间
        Double bounceRate = userAnalyticsDao.getBounceRate(start, end);
        Double avgStayTime = userAnalyticsDao.getAvgStayTime(start, end);
        
        result.setBounceRate(bounceRate != null ? bounceRate : 0.0);
        result.setAvgStayTime(avgStayTime != null ? avgStayTime : 0.0);
        
        // 获取转化漏斗数据 - 从实际数据源获取
        List<Map<String, Object>> funnelData = userAnalyticsDao.getFunnelData(start, end);
        
        // 数据验证 - 如果没有数据，则提供默认数据结构
        if (funnelData == null || funnelData.isEmpty()) {
            funnelData = createDefaultFunnelStructure(start, end);
        }
        
        result.setFunnelData(funnelData);
        
        return result;
    }
    
    /**
     * 创建默认的漏斗数据结构
     * 当实际数据不可用时使用
     */
    private List<Map<String, Object>> createDefaultFunnelStructure(Date startDate, Date endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 使用实际的浏览用户数
        Long viewUsers = userAnalyticsDao.getViewProductUserCount(startDate, endDate);
        if (viewUsers == null || viewUsers == 0) {
            viewUsers = 1L; // 至少有1个用户，避免除零错误
        }
        
        // 使用实际的购物车用户数
        Long cartUsers = userAnalyticsDao.getAddToCartUserCount(startDate, endDate);
        if (cartUsers == null) {
            cartUsers = 0L;
        }
        
        // 使用实际的下单用户数
        Long orderUsers = userAnalyticsDao.getOrderUserCount(startDate, endDate);
        if (orderUsers == null) {
            orderUsers = 0L;
        }
        
        // 使用实际的支付用户数
        Long payUsers = userAnalyticsDao.getPaymentUserCount(startDate, endDate);
        if (payUsers == null) {
            payUsers = 0L;
        }
        
        // 构建漏斗数据 - 按照正确的转化顺序排列
        Map<String, Object> browse = new HashMap<>();
        browse.put("name", "浏览商品");
        browse.put("value", viewUsers);
        result.add(browse);
        
        Map<String, Object> cart = new HashMap<>();
        cart.put("name", "加入购物车");
        cart.put("value", cartUsers);
        result.add(cart);
        
        Map<String, Object> order = new HashMap<>();
        order.put("name", "下单");
        order.put("value", orderUsers);
        result.add(order);
        
        Map<String, Object> pay = new HashMap<>();
        pay.put("name", "支付");
        pay.put("value", payUsers);
        result.add(pay);
        
        return result;
    }

    @Override
    public List<UserVisitTimeDistributionDto> getUserVisitTimeDistribution(Date startDate, Date endDate) {
        List<Map<String, Object>> timeData = userAnalyticsDao.getUserVisitTimeDistribution(startDate, endDate);

        // 创建24小时的完整数据（0-23小时）
        Map<Integer, Long> hourMap = timeData.stream()
                .collect(Collectors.toMap(
                        data -> ((Number) data.get("hour")).intValue(),
                        data -> ((Number) data.get("count")).longValue(),
                        (v1, v2) -> v1
                ));

        List<UserVisitTimeDistributionDto> result = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            UserVisitTimeDistributionDto dto = new UserVisitTimeDistributionDto();
            dto.setHour(hour);
            dto.setCount(hourMap.getOrDefault(hour, 0L));
            result.add(dto);
        }

        return result;
    }
}
