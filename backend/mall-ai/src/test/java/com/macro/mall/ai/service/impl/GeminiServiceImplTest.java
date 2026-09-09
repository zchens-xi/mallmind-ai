package com.macro.mall.ai.service.impl;

import com.macro.mall.ai.config.TestConfiguration;
import com.macro.mall.ai.service.GeminiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest(classes = TestConfiguration.class)
class GeminiServiceImplTest {

    @Autowired
    private GeminiService geminiService;

    @Test
    public void askQuestion() throws IOException {
        geminiService.askQuestion("What is the capital of France?");
    }
}