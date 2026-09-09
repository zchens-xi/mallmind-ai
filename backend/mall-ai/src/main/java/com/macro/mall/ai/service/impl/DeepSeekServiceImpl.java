package com.macro.mall.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.macro.mall.ai.service.DeepSeekService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Kimi AI服务实现类（原DeepSeek替换为Kimi）
 * 专注于AI API调用，不负责数据存储
 */
@Slf4j
@Service
public class DeepSeekServiceImpl implements DeepSeekService {

    // Kimi API 地址
    private static final String API_URL = "https://api.moonshot.cn/v1/chat/completions";

    // 替换为你自己的 Moonshot Kimi API Key
    private static final String API_KEY = System.getenv("DEEPSEEK_API_KEY");

    private final OkHttpClient httpClient = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String askQuestion(String question) throws IOException {
        log.info("开始调用Kimi AI API，问题: {}", question);

        if (question == null || question.trim().isEmpty()) {
            throw new IllegalArgumentException("问题内容不能为空");
        }

        // 正确构建JSON，处理字符串转义
        String escapedQuestion = escapeJsonString(question);
        String jsonBody = buildJsonRequest(escapedQuestion);

        RequestBody body = RequestBody.create(jsonBody, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("API调用失败，状态码: {}, 错误信息: {}", response.code(), errorBody);
                throw new IOException("API request failed with code " + response.code() + ": " + errorBody);
            }

            String responseBody = response.body().string();
            log.debug("API响应: {}", responseBody);

            // 解析JSON响应，提取实际的AI回答内容
            String answer = parseAiResponse(responseBody);
            log.info("AI回答获取成功，长度: {}", answer.length());

            return answer;
        }
    }

    /**
     * 转义JSON字符串中的特殊字符
     */
    private String escapeJsonString(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * 构建JSON请求体
     */
    private String buildJsonRequest(String escapedQuestion) {
        return "{"
                + "\"model\": \"moonshot-v1-8k\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + escapedQuestion + "\"}],"
                + "\"temperature\": 0.7,"
                + "\"max_tokens\": 2048,"
                + "\"stream\": false"
                + "}";
    }

    /**
     * 解析AI API响应，提取实际的回答内容
     */
    private String parseAiResponse(String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            JsonNode choicesNode = rootNode.get("choices");

            if (choicesNode != null && choicesNode.isArray() && !choicesNode.isEmpty()) {
                JsonNode firstChoice = choicesNode.get(0);
                JsonNode messageNode = firstChoice.get("message");
                if (messageNode != null) {
                    JsonNode contentNode = messageNode.get("content");
                    if (contentNode != null) {
                        return contentNode.asText();
                    }
                }
            }

            log.warn("无法解析AI响应，返回原始内容");
            return responseBody;
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage(), e);
            return responseBody;
        }
    }
}
