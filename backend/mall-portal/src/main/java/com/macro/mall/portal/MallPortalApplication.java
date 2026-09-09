package com.macro.mall.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration;
import org.springframework.boot.autoconfigure.data.neo4j.Neo4jReactiveDataAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
//import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication(scanBasePackages = {"com.macro.mall.portal", "com.macro.mall.common", "com.macro.mall.security", "com.macro.mall.ai", "com.macro.mall.recommendation"},
                      exclude = {Neo4jDataAutoConfiguration.class, Neo4jReactiveDataAutoConfiguration.class})
@EnableMongoRepositories(basePackages = {
        "com.macro.mall.ai.repository.mongo",
        "com.macro.mall.portal.repository",
        "com.macro.mall.recommendation.repository.mongo"
})
//@EnableNeo4jRepositories(basePackages = "com.macro.mall.ai.repository.neo4j")
@EnableFeignClients(basePackages = "com.macro.mall.recommendation.client")
public class MallPortalApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallPortalApplication.class, args);
    }

}
