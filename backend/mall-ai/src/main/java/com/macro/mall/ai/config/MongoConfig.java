package com.macro.mall.ai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

/**
 * MongoDB配置类
 * 用于存储AI对话记录和用户行为数据
 */
@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/mall_ai}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:mall_ai}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    protected void configureClientSettings(MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new ConnectionString(mongoUri));

        // 设置连接池配置
        builder.applyToConnectionPoolSettings(poolBuilder -> {
            poolBuilder.maxSize(50)  // 最大连接数
                      .minSize(5)    // 最小连接数
                      .maxWaitTime(30000, java.util.concurrent.TimeUnit.MILLISECONDS); // 获取连接超时时间
        });

        // 设置Socket配置
        builder.applyToSocketSettings(socketBuilder -> {
            socketBuilder.connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS) // 连接超时
                        .readTimeout(10000, java.util.concurrent.TimeUnit.MILLISECONDS);    // 读取超时
        });
    }
    
    /**
     * 自定义MongoDB转换器，禁用类型信息的写入
     * 避免在MongoDB文档中存储Java类名（_class字段）
     */
    @Bean
    public MappingMongoConverter mappingMongoConverter(MongoDatabaseFactory factory, 
                                                     MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(
            new DefaultDbRefResolver(factory), context);
        
        // 禁用类型信息的写入，默认会添加"_class"字段
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        
        return converter;
    }
}
