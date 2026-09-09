package com.macro.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 应用启动入口
 * Created by macro on 2018/4/26.
 */
@SpringBootApplication
@EnableScheduling
@EnableFeignClients(basePackages = "com.macro.mall.recommendation.client")
@EnableMongoRepositories(basePackages = {"com.macro.mall.ai.repository.mongo", "com.macro.mall.recommendation.repository.mongo"})
//@EnableNeo4jRepositories(basePackages = "com.macro.mall.ai.repository.neo4j")
@MapperScan({"com.macro.mall.dao"})
@ComponentScan(basePackages = {"com.macro.mall", "com.macro.mall.ai"})
public class MallAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallAdminApplication.class, args);
    }
}
