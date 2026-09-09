package com.macro.mall.ai.service.impl;

import com.macro.mall.ai.model.mongo.ConversationHistory;
import com.macro.mall.ai.service.GeminiService;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Gemini AI服务实现类
 */
@Service
public class GeminiServiceImpl implements GeminiService {

    private static final String API_URL = "http://localhost:2048/v1/chat/completions";
//    private static final String API_KEY = "sk-0c25b91d7a034da1955fb5eb269fc321"; // 替换为你的API Key

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    @Autowired
    private AICustomerServiceImpl aiCustomerService;

    @Override
    public String askQuestion(String question) throws IOException {
        String jsonBody = "{"
                + "\"model\": \"gemini-2.5-pro\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + question + "\"}],"
                + "\"stream\": false"
                + "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
//                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String answer = response.body().string();

            // 保存对话记录到 MongoDB
//            aiCustomerService.saveConversation(question, answer);

            return answer;
        }
    }
}
