package com.macro.mall.ai.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AI响应处理工具类
 * 提供AI响应的格式化和处理方法
 */
@Slf4j
public class AIResponseUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 格式化AI响应，添加时间戳
     */
    public static String formatResponse(String response, String aiProvider) {
        if (response == null || response.trim().isEmpty()) {
            return "抱歉，AI暂时无法回答您的问题。";
        }

        return String.format("[%s] %s\n\n由 %s 提供支持 | %s",
                            FORMATTER.format(LocalDateTime.now()),
                            response.trim(),
                            aiProvider,
                            "AI回答仅供参考");
    }

    /**
     * 处理AI响应中的特殊字符
     */
    public static String sanitizeResponse(String response) {
        if (response == null) {
            return "";
        }

        // 移除可能的HTML标签
        response = response.replaceAll("<[^>]*>", "");

        // 处理换行符
        response = response.replaceAll("\\\\n", "\n");

        // 移除多余的空白字符
        response = response.replaceAll("\\s+", " ").trim();

        return response;
    }

    /**
     * 检查响应是否包含敏感内容
     */
    public static boolean containsSensitiveContent(String response) {
        if (response == null) {
            return false;
        }

        String lowerResponse = response.toLowerCase();
        String[] sensitiveWords = {"error", "exception", "failed", "null"};

        for (String word : sensitiveWords) {
            if (lowerResponse.contains(word)) {
                log.warn("AI响应包含敏感词: {}", word);
                return true;
            }
        }

        return false;
    }

    /**
     * 生成会话摘要
     */
    public static String generateSessionSummary(String question, String answer) {
        String shortQuestion = question.length() > 50 ?
                              question.substring(0, 47) + "..." : question;
        String shortAnswer = answer.length() > 100 ?
                            answer.substring(0, 97) + "..." : answer;

        return String.format("Q: %s | A: %s", shortQuestion, shortAnswer);
    }
}
