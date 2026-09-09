package com.macro.mall.ai.util;

import lombok.extern.slf4j.Slf4j;

/**
 * AI请求验证工具类
 * 提供AI相关的通用验证方法
 */
@Slf4j
public class AIValidationUtils {

    /**
     * 验证AI问题内容是否有效
     */
    public static boolean isValidQuestion(String question) {
        return question != null &&
               !question.trim().isEmpty() &&
               question.trim().length() <= 4000; // 限制问题长度
    }

    /**
     * 验证AI回答内容是否有效
     */
    public static boolean isValidAnswer(String answer) {
        return answer != null && !answer.trim().isEmpty();
    }

    /**
     * 清理和标准化问题内容
     */
    public static String sanitizeQuestion(String question) {
        if (question == null) {
            return "";
        }

        // 去除前后空格
        question = question.trim();

        // 移除特殊字符，防止注入攻击
        question = question.replaceAll("[<>\"'&]", "");

        // 限制长度
        if (question.length() > 4000) {
            question = question.substring(0, 4000);
            log.warn("问题内容过长，已截断到4000字符");
        }

        return question;
    }

    /**
     * 生成错误消息
     */
    public static String generateErrorMessage(String aiProvider, String errorType) {
        return String.format("%s AI服务%s，请稍后再试", aiProvider, errorType);
    }

    /**
     * 验证用户ID是否有效
     */
    public static boolean isValidUserId(Long userId) {
        return userId != null && userId > 0;
    }
}
