package com.macro.mall.util;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.sql.ResultSetMetaData;

public class GenericETLService {
    private static final String CONFIG_FILE = "mybatis-config.xml";

    public <T, R> void performETL(Class<?> mapperClass, Function<T, R> transformFunction, Function<R, Void> loadFunction) {
        try (InputStream inputStream = Resources.getResourceAsStream(CONFIG_FILE)) {
            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            try (SqlSession session = sqlSessionFactory.openSession()) {
                // 获取JDBC连接
                Connection connection = session.getConnection();
                
                // 从Mapper类名推断表名
                String tableName = getTableNameFromMapperClass(mapperClass);
                String sql = "SELECT * FROM " + tableName;
                
                // 使用JDBC直接执行SQL
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {
                    
                    List<Map<String, Object>> dataList = convertResultSetToList(rs);
                    
                    for (Map<String, Object> data : dataList) {
                        @SuppressWarnings("unchecked")
                        R transformedData = transformFunction.apply((T) data);
                        loadFunction.apply(transformedData);
                    }
                }
                
                session.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private List<Map<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();
        List<Map<String, Object>> results = new ArrayList<>();
        
        while (rs.next()) {
            Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(md.getColumnLabel(i).toLowerCase(), rs.getObject(i));
            }
            results.add(row);
        }
        
        return results;
    }
    
    private String getTableNameFromMapperClass(Class<?> mapperClass) {
        // 从Mapper类名推断表名
        String className = mapperClass.getSimpleName();
        String tableName = className.replace("Mapper", "");
        
        // 转换为下划线命名格式
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < tableName.length(); i++) {
            char c = tableName.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }
}
