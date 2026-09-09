package com.macro.mall.service.impl;

import com.macro.mall.dao.HomeDao;
import com.macro.mall.service.HomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 首页内容管理Service实现类
 */
@Service
public class HomeServiceImpl implements HomeService {
    @Autowired
    private HomeDao homeDao;

    @Override
    public Map<String, Object> getHomeData(String timeRange) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取今天和昨天的日期
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        Date yesterday = calendar.getTime();
        
        // 获取概览数据
        Map<String, Object> overview = new HashMap<>();
        
        // 获取销售额数据
        Double todaySales = homeDao.getTodaySales(today);
        Double yesterdaySales = homeDao.getYesterdaySales(yesterday);
        Double salesGrowth = 0.0;
        if (yesterdaySales > 0) {
            salesGrowth = (todaySales - yesterdaySales) / yesterdaySales * 100;
        }
        overview.put("todaySales", todaySales);
        overview.put("yesterdaySales", yesterdaySales);
        overview.put("salesGrowth", salesGrowth);
        
        // 获取订单数据
        Integer todayOrders = homeDao.getTodayOrderCount(today);
        Integer yesterdayOrders = homeDao.getYesterdayOrderCount(yesterday);
        Double orderGrowth = 0.0;
        if (yesterdayOrders > 0) {
            orderGrowth = (todayOrders - yesterdayOrders) / (double) yesterdayOrders * 100;
        }
        overview.put("todayOrders", todayOrders);
        overview.put("yesterdayOrders", yesterdayOrders);
        overview.put("orderGrowth", orderGrowth);
        
        // 获取用户数据
        Integer todayUsers = homeDao.getTodayNewUserCount(today);
        Integer yesterdayUsers = homeDao.getYesterdayNewUserCount(yesterday);
        Double userGrowth = 0.0;
        if (yesterdayUsers > 0) {
            userGrowth = (todayUsers - yesterdayUsers) / (double) yesterdayUsers * 100;
        }
        overview.put("todayUsers", todayUsers);
        overview.put("yesterdayUsers", yesterdayUsers);
        overview.put("userGrowth", userGrowth);
        
        // 获取访问量数据
        Integer todayVisits = homeDao.getTodayVisitCount(today);
        Integer yesterdayVisits = homeDao.getYesterdayVisitCount(yesterday);
        Double visitGrowth = 0.0;
        if (yesterdayVisits > 0) {
            visitGrowth = (todayVisits - yesterdayVisits) / (double) yesterdayVisits * 100;
        }
        overview.put("todayVisits", todayVisits);
        overview.put("yesterdayVisits", yesterdayVisits);
        overview.put("visitGrowth", visitGrowth);
        
        result.put("overview", overview);
        
        // 获取销售趋势数据
        Map<String, Object> salesChart = new HashMap<>();
        
        // 根据时间范围获取起止日期
        Date startDate = null;
        Date endDate = today;
        calendar.setTime(today);
        
        switch (timeRange) {
            case "week":
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startDate = calendar.getTime();
                break;
            case "month":
                calendar.add(Calendar.MONTH, -1);
                startDate = calendar.getTime();
                break;
            case "year":
                calendar.add(Calendar.YEAR, -1);
                startDate = calendar.getTime();
                break;
            default:
                calendar.add(Calendar.DAY_OF_MONTH, -7);
                startDate = calendar.getTime();
                break;
        }
        
        // 获取销售额和订单数趋势
        List<Map<String, Object>> salesTrend = homeDao.getSalesTrend(startDate, endDate);
        List<Map<String, Object>> orderTrend = homeDao.getOrderCountTrend(startDate, endDate);
        
        // 生成日期列表
        List<String> dateList = generateDateList(startDate, endDate);
        
        // 转换为前端所需格式
        List<Double> salesList = new ArrayList<>();
        List<Integer> ordersList = new ArrayList<>();
        
        Map<String, Double> salesMap = new HashMap<>();
        Map<String, Integer> ordersMap = new HashMap<>();
        
        for (Map<String, Object> item : salesTrend) {
            String date = (String) item.get("date");
            Double value = ((Number) item.get("value")).doubleValue();
            salesMap.put(date, value);
        }
        
        for (Map<String, Object> item : orderTrend) {
            String date = (String) item.get("date");
            Integer value = ((Number) item.get("value")).intValue();
            ordersMap.put(date, value);
        }
        
        // 填充数据，确保每个日期都有值
        for (String date : dateList) {
            salesList.add(salesMap.getOrDefault(date, 0.0));
            ordersList.add(ordersMap.getOrDefault(date, 0));
        }
        
        salesChart.put("dates", dateList);
        salesChart.put("sales", salesList);
        salesChart.put("orders", ordersList);
        
        result.put("salesChart", salesChart);
        
        // 获取待处理事项数据
        Map<String, Object> pendingTasks = homeDao.getPendingTasks();
        
        // 确保只有待发货和退款待处理两项数据
        Map<String, Object> filteredPendingTasks = new HashMap<>();
        filteredPendingTasks.put("pendingShipment", pendingTasks.get("pendingShipment"));
        filteredPendingTasks.put("pendingRefund", pendingTasks.get("pendingRefund"));
        
        result.put("pendingTasks", filteredPendingTasks);
        
        return result;
    }

    @Override
    public List<Map<String, Object>> getFunnelData() {
        // 获取过去一年的数据
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();
        calendar.add(Calendar.YEAR, -1);
        Date startDate = calendar.getTime();
        
        // 直接从数据库获取漏斗数据
        return homeDao.getFunnelData(startDate, endDate);
    }
    
    /**
     * 生成日期列表
     */
    private List<String> generateDateList(Date startDate, Date endDate) {
        List<String> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        
        while (!calendar.getTime().after(endDate)) {
            result.add(sdf.format(calendar.getTime()));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return result;
    }
} 