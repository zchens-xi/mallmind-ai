package com.macro.mall.util;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.OmsOrderReturnApplyMapper;
import com.macro.mall.mapper.UmsMemberStatisticsInfoMapper;
import com.macro.mall.mapper.UmsMemberProfileMapper;
import com.macro.mall.model.*;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class MemberETLProcessor {
    private static final String CONFIG_FILE = "mybatis-config.xml";
    
    public static void main(String[] args) {
        GenericETLService etlService = new GenericETLService();
        etlService.performETL(
                UmsMemberStatisticsInfoMapper.class,
                MemberETLProcessor::transformMemberStatistics,
                MemberETLProcessor::loadMemberProfile
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> transformMemberStatistics(Object statisticsData) {
        // 将数据转换为Map
        Map<String, Object> statistics;
        if (statisticsData instanceof Map) {
            statistics = (Map<String, Object>) statisticsData;
        } else {
            // 如果是UmsMemberStatisticsInfo对象，则转换为Map
            UmsMemberStatisticsInfo info = (UmsMemberStatisticsInfo) statisticsData;
            statistics = new HashMap<>();
            statistics.put("member_id", info.getMemberId());
            // 添加其他需要的字段...
        }
        
        Long memberId = getLongValue(statistics, "member_id");
        if (memberId == null) {
            return new HashMap<>(); // 如果没有会员ID，返回空结果
        }

        // 计算总订单数和总消费金额
        int totalOrder = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        Date firstOrderTime = null;
        Date lastOrderTime = null;
        Set<Long> orderIds = new HashSet<>();
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                // 使用JDBC直接查询
                Connection conn = session.getConnection();
                String sql = "SELECT * FROM oms_order WHERE member_id = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setLong(1, memberId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            totalOrder++;
                            BigDecimal amount = rs.getBigDecimal("total_amount");
                            if (amount != null) {
                                totalAmount = totalAmount.add(amount);
                            }
                            
                            Long orderId = rs.getLong("id");
                            orderIds.add(orderId);
                            
                            Date createTime = rs.getTimestamp("create_time");
                            if (createTime != null) {
                                if (firstOrderTime == null || createTime.before(firstOrderTime)) {
                                    firstOrderTime = createTime;
                                }
                                if (lastOrderTime == null || createTime.after(lastOrderTime)) {
                                    lastOrderTime = createTime;
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 计算平均订单价格
        BigDecimal avgPricePerOrder = totalOrder > 0 ? totalAmount.divide(BigDecimal.valueOf(totalOrder), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 计算平均商品价格
        int totalItemCount = 0;
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                if (!orderIds.isEmpty()) {
                    // 使用JDBC直接查询
                    Connection conn = session.getConnection();
                    
                    // 构建IN查询的参数占位符
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < orderIds.size(); i++) {
                        if (i > 0) {
                            placeholders.append(",");
                        }
                        placeholders.append("?");
                    }
                    
                    String sql = "SELECT COUNT(*) as count FROM oms_order_item WHERE order_id IN (" + placeholders.toString() + ")";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        int index = 1;
                        for (Long orderId : orderIds) {
                            pstmt.setLong(index++, orderId);
                        }
                        
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                totalItemCount = rs.getInt("count");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigDecimal avgPricePerItem = totalItemCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalItemCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 计算复购率
        double repurchaseRate = 0;
        if (totalOrder > 1 && firstOrderTime != null) {
            Calendar now = Calendar.getInstance();
            Calendar firstOrderCal = Calendar.getInstance();
            firstOrderCal.setTime(firstOrderTime);
            int months = (now.get(Calendar.YEAR) - firstOrderCal.get(Calendar.YEAR)) * 12 +
                    (now.get(Calendar.MONTH) - firstOrderCal.get(Calendar.MONTH));
            repurchaseRate = (double) (totalOrder - 1) / months;
        }
        repurchaseRate = BigDecimal.valueOf(repurchaseRate).setScale(2, RoundingMode.HALF_UP).doubleValue();

        // 计算退货率
        int returnItemCount = 0;
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                if (!orderIds.isEmpty()) {
                    // 使用JDBC直接查询
                    Connection conn = session.getConnection();
                    
                    // 构建IN查询的参数占位符
                    StringBuilder placeholders = new StringBuilder();
                    for (int i = 0; i < orderIds.size(); i++) {
                        if (i > 0) {
                            placeholders.append(",");
                        }
                        placeholders.append("?");
                    }
                    
                    String sql = "SELECT COUNT(*) as count FROM oms_order_return_apply WHERE order_id IN (" + 
                            placeholders.toString() + ") AND return_amount > 0";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        int index = 1;
                        for (Long orderId : orderIds) {
                            pstmt.setLong(index++, orderId);
                        }
                        
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                returnItemCount = rs.getInt("count");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigDecimal returnRate = totalItemCount > 0 ? BigDecimal.valueOf((double) returnItemCount / totalItemCount).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 格式化首次和最后下单时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String firstOrderTimeStr = firstOrderTime != null ? sdf.format(firstOrderTime) : null;
        String lastOrderTimeStr = lastOrderTime != null ? sdf.format(lastOrderTime) : null;

        // 构建 order_stats 数据
        Map<String, Object> orderStats = new HashMap<>();
        orderStats.put("total_order", totalOrder);
        orderStats.put("total_amount", totalAmount);
        orderStats.put("avg_price_per_order", avgPricePerOrder);
        orderStats.put("avg_price_per_item", avgPricePerItem);
        orderStats.put("first_order_time", firstOrderTimeStr);
        orderStats.put("last_order_time", lastOrderTimeStr);
        orderStats.put("repurchase_rate", repurchaseRate);
        orderStats.put("return_rate", returnRate);
        orderStats.put("memberId", memberId);

        return orderStats;
    }

    private static Void loadMemberProfile(Map<String, Object> orderStats) {
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                // 使用JDBC直接执行插入操作
                Connection conn = session.getConnection();
                String sql = "INSERT INTO ums_member_profile (member_id, order_stats) VALUES (?, ?)";
                
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    Long memberId = (Long) orderStats.get("memberId");
                    String orderStatsStr = orderStats.toString();
                    
                    pstmt.setLong(1, memberId);
                    pstmt.setString(2, orderStatsStr);
                    
                    pstmt.executeUpdate();
                    conn.commit(); // 提交事务
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // 辅助方法：安全获取Long值
    private static Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // 辅助方法：安全获取BigDecimal值
    private static BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(((Number) value).toString());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    // 辅助方法：安全获取Date值
    private static Date getDateValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            return (Date) value;
        }
        return null;
    }
}
