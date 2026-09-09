package com.macro.mall.recommendation.client;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Python模型服务Feign配置
 * @author macro
 */
@Configuration
public class PythonModelFeignConfig {

    /**
     * 配置Feign日志级别
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * 配置请求超时时间
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
            5000, TimeUnit.MILLISECONDS, // 连接超时
            30000, TimeUnit.MILLISECONDS, // 读取超时
            true // 跟随重定向
        );
    }

    /**
     * 配置重试策略
     */
    @Bean
    public Retryer retryer() {
        // 重试间隔100ms，最大间隔1s，最多重试3次
        return new Retryer.Default(100, 1000, 3);
    }
}
