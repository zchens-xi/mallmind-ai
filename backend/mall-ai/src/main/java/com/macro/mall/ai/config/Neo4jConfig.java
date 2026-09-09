//package com.macro.mall.ai.config;
//
//import org.neo4j.driver.AuthTokens;
//import org.neo4j.driver.Driver;
//import org.neo4j.driver.GraphDatabase;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager;
//
//@Configuration
//public class Neo4jConfig {
//
//    @Value("${spring.neo4j.uri}")
//    private String uri;
//
//    @Value("${spring.neo4j.authentication.username}")
//    private String username;
//
//    @Value("${spring.neo4j.authentication.password}")
//    private String password;
//
//    @Bean
//    public Driver driver() {
//        return GraphDatabase.driver(uri, AuthTokens.basic(username, password));
//    }
//
//    @Bean
//    public Neo4jTransactionManager transactionManager(Driver driver) {
//        return new Neo4jTransactionManager(driver);
//    }
//}
