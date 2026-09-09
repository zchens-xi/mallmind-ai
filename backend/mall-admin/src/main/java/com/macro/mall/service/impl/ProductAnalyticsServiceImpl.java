package com.macro.mall.service.impl;

import com.macro.mall.dao.ProductAnalyticsDao;
import com.macro.mall.dto.*;
import com.macro.mall.service.ProductAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品分析Service实现类
 */
@Service
public class ProductAnalyticsServiceImpl implements ProductAnalyticsService {

    @Autowired
    private ProductAnalyticsDao productAnalyticsDao;

    @Autowired(required = false)
    private MongoTemplate mongoTemplate;

    @Override
    public ProductOverviewDto getProductOverview(Date startDate, Date endDate) {
        // 如果没有提供日期，使用默认的30天数据
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }

        ProductOverviewDto result = new ProductOverviewDto();

        // 获取商品基础统计
        Long totalProducts = productAnalyticsDao.getTotalProductCount();
        Long onSaleProducts = productAnalyticsDao.getOnSaleProductCount();
        result.setTotalProducts(totalProducts != null ? totalProducts : 0L);
        result.setOnSaleProducts(onSaleProducts != null ? onSaleProducts : 0L);

        // 获取浏览数据（从MongoDB）
        Long totalViews = getTotalViewsFromMongo(startDate, endDate);
        Long viewUsers = getViewUsersFromMongo(startDate, endDate);
        result.setTotalViews(totalViews != null ? totalViews : 0L);

        // 计算人均浏览量
        Double viewsPerUser = 0.0;
        if (viewUsers != null && viewUsers > 0) {
            viewsPerUser = totalViews.doubleValue() / viewUsers.doubleValue();
            viewsPerUser = Math.round(viewsPerUser * 100.0) / 100.0;
        }
        result.setViewsPerUser(viewsPerUser);

        // 获取收藏数据（从MongoDB）
        Long totalCollects = getTotalCollectsFromMongo(startDate, endDate);
        result.setTotalCollects(totalCollects != null ? totalCollects : 0L);

        // 计算收藏率
        Double collectRate = 0.0;
        if (totalViews != null && totalViews > 0 && totalCollects != null) {
            collectRate = (totalCollects.doubleValue() / totalViews.doubleValue()) * 100;
            collectRate = Math.round(collectRate * 100.0) / 100.0;
        }
        result.setCollectRate(collectRate);

        // 获取购买用户数
        Long buyUsers = productAnalyticsDao.getBuyUserCount(startDate, endDate);

        // 添加详细的调试日志
        System.out.println("=== 转化率数据源调试信息 ===");
        System.out.println("📊 查询时间范围: " + startDate + " 到 " + endDate);
        System.out.println("🔍 数据来源:");
        System.out.println("  - 浏览用户数(viewUsers): " + viewUsers + " (来源: MongoDB memberReadHistory集合)");
        System.out.println("  - 购买用户数(buyUsers): " + buyUsers + " (来源: MySQL oms_order表, status IN (2,3,4))");
        
        // 添加数据合理性检查
        if (buyUsers != null && buyUsers > 0 && (viewUsers == null || viewUsers == 0)) {
            System.out.println("🚨 严重问题：有购买用户但无浏览用户!");
            System.out.println("   可能原因:");
            System.out.println("   1. MongoDB浏览记录缺失");
            System.out.println("   2. 时间范围内MongoDB无数据");
            System.out.println("   3. MongoDB连接问题");
            System.out.println("   4. 集合名称不匹配");
        } else if (viewUsers != null && viewUsers > 0 && (buyUsers == null || buyUsers == 0)) {
            System.out.println("✅ 正常：有浏览用户但无购买用户 (说明转化率低)");
        } else if ((viewUsers == null || viewUsers == 0) && (buyUsers == null || buyUsers == 0)) {
            System.out.println("⚠️  数据缺失：两个数据源都无数据");
            System.out.println("   建议检查：");
            System.out.println("   1. 数据库连接是否正常");
            System.out.println("   2. 是否有测试数据");
            System.out.println("   3. 时间范围是否合理");
        }

        // 数据一致性检查和修复
        if (buyUsers != null && viewUsers != null && buyUsers > viewUsers) {
            System.out.println("⚠️  数据一致性问题：购买用户数(" + buyUsers + ") > 浏览用户数(" + viewUsers + ")");
            
            // 方案1：使用购买用户数作为最小浏览用户数（假设购买用户都浏览过）
            Long adjustedViewUsers = Math.max(viewUsers, buyUsers);
            System.out.println("📝 数据修正：将浏览用户数调整为 " + adjustedViewUsers);
            
            // 方案2：或者可以选择将buyUsers调整为不超过viewUsers
            // buyUsers = Math.min(buyUsers, viewUsers);
            // System.out.println("📝 数据修正：将购买用户数调整为 " + buyUsers);
            
            viewUsers = adjustedViewUsers; // 采用方案1
            System.out.println("💡 建议：检查数据同步机制，确保用户浏览记录正确记录到MongoDB");
            System.out.println("💡 或执行: mongodb_diagnosis.js 和 create_mongodb_test_data.js 来诊断和修复数据");
        }

        // 计算平均转化率（返回0-100的百分比数值，前端直接显示）
        Double avgConversionRate = 0.0;
        if (viewUsers != null && viewUsers > 0 && buyUsers != null) {
            // 原始转化率计算
            double rawRate = (buyUsers.doubleValue() / viewUsers.doubleValue()) * 100;
            System.out.println("- 原始转化率: " + rawRate + "%");
            
            // 确保转化率不超过100%，防止数据异常
            double rate = Math.min(rawRate, 100.0);
            avgConversionRate = Math.round(rate * 100.0) / 100.0;
            
            System.out.println("- 最终转化率: " + avgConversionRate + "%");
            
            // 如果转化率异常，输出警告
            if (rawRate > 100.0) {
                System.out.println("⚠️  警告：转化率超过100%，已自动调整为100%");
                System.out.println("   原因：数据不一致，建议检查数据同步机制");
            }
        }
        result.setAvgConversionRate(avgConversionRate);

        // 计算加购率（从购物车数据获取）
        Long cartCount = productAnalyticsDao.getCartCount(startDate, endDate);
        Double cartRate = 0.0;
        if (totalViews != null && totalViews > 0 && cartCount != null) {
            cartRate = (cartCount.doubleValue() / totalViews.doubleValue()) * 100;
            cartRate = Math.round(cartRate * 100.0) / 100.0;
        }
        result.setCartRate(cartRate);

        // 获取分类销售占比
        List<Map<String, Object>> categorySalesData = productAnalyticsDao.getCategorySalesPercent(startDate, endDate);
        List<ProductOverviewDto.CategorySalesPercent> categorySalesPercent = new ArrayList<>();

        BigDecimal totalSales = categorySalesData.stream()
                .map(item -> (BigDecimal) item.get("salesAmount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (Map<String, Object> item : categorySalesData) {
            Long categoryId = ((Number) item.get("categoryId")).longValue();
            String categoryName = (String) item.get("categoryName");
            BigDecimal salesAmount = (BigDecimal) item.get("salesAmount");

            Double percent = 0.0;
            if (totalSales.compareTo(BigDecimal.ZERO) > 0) {
                percent = salesAmount.divide(totalSales, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal(100)).doubleValue();
                percent = Math.round(percent * 10.0) / 10.0;
            }

            categorySalesPercent.add(new ProductOverviewDto.CategorySalesPercent(
                    categoryId, categoryName, salesAmount, percent));
        }
        result.setCategorySalesPercent(categorySalesPercent);

        return result;
    }

    @Override
    public List<ProductSalesRankDto> getProductSalesRank(Date startDate, Date endDate, Integer limit) {
        // 设置默认值
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }
        if (limit == null || limit <= 0 || limit > 100) {
            limit = 10;
        }

        List<Map<String, Object>> salesData = productAnalyticsDao.getProductSalesRank(startDate, endDate, limit);
        List<ProductSalesRankDto> result = new ArrayList<>();

        int rank = 1;
        for (Map<String, Object> item : salesData) {
            Long productId = ((Number) item.get("productId")).longValue();
            String productName = (String) item.get("productName");
            BigDecimal salesAmount = (BigDecimal) item.get("salesAmount");
            Integer salesQuantity = ((Number) item.get("salesQuantity")).intValue();

            result.add(new ProductSalesRankDto(productId, productName, salesAmount, salesQuantity, rank++));
        }

        return result;
    }

    @Override
    public List<ProductViewRankDto> getProductViewRank(Date startDate, Date endDate, Integer limit) {
        // 设置默认值
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }
        if (limit == null || limit <= 0 || limit > 100) {
            limit = 10;
        }

        // 从MongoDB获取商品浏览数据
        List<Map<String, Object>> viewData = getProductViewDataFromMongo(startDate, endDate, limit);
        List<ProductViewRankDto> result = new ArrayList<>();

        int rank = 1;
        for (Map<String, Object> item : viewData) {
            Long productId = ((Number) item.get("productId")).longValue();
            String productName = (String) item.get("productName");
            Long viewCount = ((Number) item.get("viewCount")).longValue();
            Long uniqueVisitors = ((Number) item.get("uniqueVisitors")).longValue();

            result.add(new ProductViewRankDto(productId, productName, viewCount, uniqueVisitors, rank++));
        }

        return result;
    }

    @Override
    public List<ProductConversionRankDto> getProductConversionRank(Date startDate, Date endDate, Integer limit) {
        // 设置默认值
        if (startDate == null || endDate == null) {
            Calendar calendar = Calendar.getInstance();
            endDate = calendar.getTime();
            calendar.add(Calendar.DAY_OF_MONTH, -30);
            startDate = calendar.getTime();
        }
        if (limit == null || limit <= 0 || limit > 100) {
            limit = 10;
        }

        // 获取商品浏览用户数统计
        List<Map<String, Object>> viewUserStats = getProductViewUserStatsFromMongo(startDate, endDate);
        Map<Long, Long> viewUserMap = viewUserStats.stream()
                .collect(Collectors.toMap(
                        item -> ((Number) item.get("productId")).longValue(),
                        item -> ((Number) item.get("viewUsers")).longValue()
                ));

        // 获取商品购买用户数统计
        List<Map<String, Object>> buyUserStats = productAnalyticsDao.getProductBuyUserStats(startDate, endDate);
        Map<Long, Long> buyUserMap = buyUserStats.stream()
                .collect(Collectors.toMap(
                        item -> ((Number) item.get("productId")).longValue(),
                        item -> ((Number) item.get("buyUsers")).longValue()
                ));

        // 计算转化率并排序
        List<ProductConversionRankDto> result = new ArrayList<>();
        Set<Long> allProductIds = new HashSet<>();
        allProductIds.addAll(viewUserMap.keySet());
        allProductIds.addAll(buyUserMap.keySet());

        for (Long productId : allProductIds) {
            Long viewUsers = viewUserMap.getOrDefault(productId, 0L);
            Long buyUsers = buyUserMap.getOrDefault(productId, 0L);

            if (viewUsers > 0) {
                // 确保转化率不超过100%，防止数据异常
                double rate = Math.min((buyUsers.doubleValue() / viewUsers.doubleValue()) * 100, 100.0);
                Double conversionRate = Math.round(rate * 100.0) / 100.0;

                // 从浏览数据中获取商品名称
                String productName = getProductNameFromViewStats(productId, viewUserStats);

                result.add(new ProductConversionRankDto(productId, productName, viewUsers, buyUsers, conversionRate, 0));
            }
        }

        // 按转化率降序排序
        result.sort((a, b) -> Double.compare(b.getConversionRate(), a.getConversionRate()));

        // 限制返回数量并设置排名
        result = result.stream().limit(limit).collect(Collectors.toList());
        for (int i = 0; i < result.size(); i++) {
            result.get(i).setRank(i + 1);
        }

        return result;
    }

    @Override
    public List<ProductPriceDistributionDto> getProductPriceDistribution() {
        List<Map<String, Object>> priceData = productAnalyticsDao.getProductPriceDistribution();
        List<ProductPriceDistributionDto> result = new ArrayList<>();

        // 计算总商品数
        Long totalCount = priceData.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .sum();

        for (Map<String, Object> item : priceData) {
            String range = (String) item.get("range");
            Long count = ((Number) item.get("count")).longValue();

            Double percent = 0.0;
            if (totalCount > 0) {
                percent = (count.doubleValue() / totalCount.doubleValue()) * 100;
                percent = Math.round(percent * 10.0) / 10.0;
            }

            result.add(new ProductPriceDistributionDto(range, count, percent));
        }

        return result;
    }

    @Override
    public List<ProductStockDistributionDto> getProductStockDistribution() {
        List<Map<String, Object>> stockData = productAnalyticsDao.getProductStockDistribution();
        List<ProductStockDistributionDto> result = new ArrayList<>();

        // 计算总商品数
        Long totalCount = stockData.stream()
                .mapToLong(item -> ((Number) item.get("count")).longValue())
                .sum();

        for (Map<String, Object> item : stockData) {
            String range = (String) item.get("range");
            Long count = ((Number) item.get("count")).longValue();

            Double percent = 0.0;
            if (totalCount > 0) {
                percent = (count.doubleValue() / totalCount.doubleValue()) * 100;
                percent = Math.round(percent * 10.0) / 10.0;
            }

            String status = getStockStatus(range);

            result.add(new ProductStockDistributionDto(range, count, percent, status));
        }

        return result;
    }

    /**
     * 从MongoDB获取总浏览量
     */
    private Long getTotalViewsFromMongo(Date startDate, Date endDate) {
        if (mongoTemplate == null) {
            System.out.println("MongoDB mongoTemplate is null for total views");
            return 0L; // 如果MongoDB不可用，返回0
        }

        try {
            // 调试MongoDB连接和数据
            System.out.println("=== MongoDB详细调试信息 ===");
            System.out.println("MongoDB连接状态: " + (mongoTemplate != null ? "已连接" : "未连接"));
            
            if (mongoTemplate != null) {
                // 检查数据库名称
                String dbName = mongoTemplate.getDb().getName();
                System.out.println("当前连接的数据库名称: " + dbName);
                
                // 检查所有集合名称
                Set<String> collectionNames = mongoTemplate.getDb().listCollectionNames().into(new HashSet<>());
                System.out.println("数据库中所有集合: " + collectionNames);
                
                // 检查memberReadHistory集合
                long totalRecords = mongoTemplate.count(new Query(), "memberReadHistory");
                System.out.println("memberReadHistory集合记录数: " + totalRecords);
                
                // 如果memberReadHistory为空，检查其他可能的集合
                for (String collectionName : collectionNames) {
                    if (collectionName.toLowerCase().contains("read") || 
                        collectionName.toLowerCase().contains("history") || 
                        collectionName.toLowerCase().contains("member")) {
                        long count = mongoTemplate.count(new Query(), collectionName);
                        System.out.println("集合 " + collectionName + " 记录数: " + count);
                        
                        if (count > 0) {
                            // 查看样本数据
                            Query sampleQuery = new Query();
                            sampleQuery.limit(1);
                            List<Map> sampleData = mongoTemplate.find(sampleQuery, Map.class, collectionName);
                            if (!sampleData.isEmpty()) {
                                System.out.println("集合 " + collectionName + " 样本数据: " + sampleData.get(0));
                            }
                        }
                    }
                }
            }
            
            // 执行原来的查询（即使没有数据也要测试）
            System.out.println("开始执行时间范围查询...");
            
            // 先尝试不带时间条件的查询，获取最新数据
            Query allQuery = new Query();
            allQuery.with(org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createTime"));
            allQuery.limit(1);
            List<Map> latestData = mongoTemplate.find(allQuery, Map.class, "memberReadHistory");
            if (!latestData.isEmpty()) {
                Map latestRecord = latestData.get(0);
                Object createTimeObj = latestRecord.get("createTime");
                System.out.println("最新记录的createTime: " + createTimeObj);
                System.out.println("createTime类型: " + (createTimeObj != null ? createTimeObj.getClass().getName() : "null"));
            }
            
            // 原来的查询
            Query query = new Query();
            query.addCriteria(Criteria.where("createTime").gte(startDate).lte(endDate));
            long count = mongoTemplate.count(query, "memberReadHistory");
            System.out.println("时间范围查询结果: " + count);
            System.out.println("查询时间范围: " + startDate + " 到 " + endDate);
            
            // 如果时间范围查询没有结果，尝试更宽泛的时间范围
            if (count == 0) {
                System.out.println("尝试扩大时间范围...");
                Calendar widerCal = Calendar.getInstance();
                Date widerEndDate = widerCal.getTime();
                widerCal.add(Calendar.MONTH, -6); // 6个月前
                Date widerStartDate = widerCal.getTime();
                
                Query widerQuery = new Query();
                widerQuery.addCriteria(Criteria.where("createTime").gte(widerStartDate).lte(widerEndDate));
                long widerCount = mongoTemplate.count(widerQuery, "memberReadHistory");
                System.out.println("扩大时间范围查询结果: " + widerCount);
                System.out.println("扩大后时间范围: " + widerStartDate + " 到 " + widerEndDate);
                
                // 如果还是没有，尝试不带时间条件
                if (widerCount == 0) {
                    Query noTimeQuery = new Query();
                    long totalCount = mongoTemplate.count(noTimeQuery, "memberReadHistory");
                    System.out.println("无时间条件查询结果: " + totalCount);
                    
                    if (totalCount > 0) {
                        System.out.println("数据存在但时间字段可能有问题，返回总数...");
                        return totalCount;
                    }
                }
            }
            
            return count;
        } catch (Exception e) {
            System.err.println("MongoDB查询总浏览量异常: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 从MongoDB获取浏览用户数
     */
    private Long getViewUsersFromMongo(Date startDate, Date endDate) {
        if (mongoTemplate == null) {
            System.out.println("MongoDB mongoTemplate is null for view users count");
            return 0L;
        }

        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("createTime").gte(startDate).lte(endDate)),
                    Aggregation.group("memberId"),
                    Aggregation.count().as("count")
            );

            AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "memberReadHistory", Map.class);
            long userCount = results.getMappedResults().size();
            System.out.println("MongoDB浏览用户数查询结果: " + userCount + ", 时间范围: " + startDate + " 到 " + endDate);
            return userCount;
        } catch (Exception e) {
            System.err.println("MongoDB查询浏览用户数异常: " + e.getMessage());
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 从MongoDB获取总收藏量
     */
    private Long getTotalCollectsFromMongo(Date startDate, Date endDate) {
        if (mongoTemplate == null) {
            return 0L;
        }

        try {
            // 由于收藏记录没有时间字段，返回总收藏数
            return mongoTemplate.count(new Query(), "memberProductCollection");
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 从MongoDB获取商品浏览数据
     */
    private List<Map<String, Object>> getProductViewDataFromMongo(Date startDate, Date endDate, Integer limit) {
        if (mongoTemplate == null) {
            System.out.println("MongoDB mongoTemplate is null");
            return new ArrayList<>();
        }

        try {
            System.out.println("查询MongoDB浏览数据 - 时间范围: " + startDate + " 到 " + endDate);
            
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("createTime").gte(startDate).lte(endDate)),
                    Aggregation.group("productId")
                            .count().as("viewCount")
                            .addToSet("memberId").as("uniqueMembers")
                            .first("productName").as("productName"),
                    Aggregation.project()
                            .and("_id").as("productId")
                            .and("productName").as("productName")
                            .and("viewCount").as("viewCount")
                            .and("uniqueMembers").size().as("uniqueVisitors"),
                    Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "viewCount"),
                    Aggregation.limit(limit)
            );

            AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "memberReadHistory", Map.class);
            System.out.println("MongoDB查询结果数量: " + results.getMappedResults().size());
            
            // 修复类型转换问题
            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Map rawMap : results.getMappedResults()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedMap = (Map<String, Object>) rawMap;
                mappedResults.add(typedMap);
                System.out.println("浏览数据: " + typedMap);
            }
            return mappedResults;
        } catch (Exception e) {
            System.err.println("MongoDB查询浏览数据异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 从MongoDB获取商品浏览用户数统计
     */
    private List<Map<String, Object>> getProductViewUserStatsFromMongo(Date startDate, Date endDate) {
        if (mongoTemplate == null) {
            System.out.println("MongoDB mongoTemplate is null for view user stats");
            return new ArrayList<>();
        }

        try {
            System.out.println("查询MongoDB浏览用户统计 - 时间范围: " + startDate + " 到 " + endDate);
            
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("createTime").gte(startDate).lte(endDate)),
                    Aggregation.group("productId")
                            .addToSet("memberId").as("uniqueMembers")
                            .first("productName").as("productName"),
                    Aggregation.project()
                            .and("_id").as("productId")
                            .and("productName").as("productName")
                            .and("uniqueMembers").size().as("viewUsers")
            );

            AggregationResults<Map> results = mongoTemplate.aggregate(aggregation, "memberReadHistory", Map.class);
            System.out.println("MongoDB浏览用户统计结果数量: " + results.getMappedResults().size());
            
            // 修复类型转换问题
            List<Map<String, Object>> mappedResults = new ArrayList<>();
            for (Map rawMap : results.getMappedResults()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> typedMap = (Map<String, Object>) rawMap;
                mappedResults.add(typedMap);
                System.out.println("浏览用户统计数据: " + typedMap);
            }
            return mappedResults;
        } catch (Exception e) {
            System.err.println("MongoDB查询浏览用户统计异常: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * 从浏览统计中获取商品名称
     */
    private String getProductNameFromViewStats(Long productId, List<Map<String, Object>> viewUserStats) {
        for (Map<String, Object> item : viewUserStats) {
            Long itemProductId = ((Number) item.get("productId")).longValue();
            if (productId.equals(itemProductId)) {
                Object productName = item.get("productName");
                if (productName != null) {
                    return productName.toString();
                }
                break;
            }
        }
        return "商品" + productId;
    }

    /**
     * 从统计数据中获取商品名称（浏览统计或购买统计）
     */
    private String getProductNameFromStats(Long productId, List<Map<String, Object>> viewUserStats, List<Map<String, Object>> buyUserStats) {
        // 优先从浏览统计获取商品名称
        String productName = getProductNameFromViewStats(productId, viewUserStats);
        String defaultName = "商品" + productId;
        if (!defaultName.equals(productName)) {
            return productName;
        }
        
        // 如果浏览统计没有，从购买统计获取
        for (Map<String, Object> item : buyUserStats) {
            Long itemProductId = ((Number) item.get("productId")).longValue();
            if (productId.equals(itemProductId)) {
                // 购买统计数据中没有商品名称，需要查询数据库
                // 为简化，直接返回默认名称
                break;
            }
        }
        return defaultName;
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
}
