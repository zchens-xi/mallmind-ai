package com.macro.mall.ai.config;

import com.macro.mall.ai.service.impl.AICustomerServiceImpl;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class})
@ComponentScan(basePackages = "com.macro.mall.ai")
public class TestConfiguration {

    @MockBean
    private AICustomerServiceImpl aiCustomerService;
}
