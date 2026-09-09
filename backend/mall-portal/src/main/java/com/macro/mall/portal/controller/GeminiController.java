package com.macro.mall.portal.controller;

import com.macro.mall.ai.service.GeminiService;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.ai.service.impl.AICustomerServiceImpl;
import com.macro.mall.ai.model.mongo.ConversationHistory;
import com.macro.mall.portal.domain.MemberDetails;
import com.macro.mall.portal.service.AiService;
import com.macro.mall.portal.service.UmsMemberService;
import com.macro.mall.model.UmsMember;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * DeepSeek AI 对接控制器
 * 提供AI客服接口，并自动记录对话历史
 */

@RestController
@RequestMapping("/ai/gemini")
@Api(tags = "GeminiController")
@Tag(name = "GeminiController", description = "对接 Gemini 大模型 API")
@Slf4j
public class GeminiController {

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private AICustomerServiceImpl aiCustomerService;

    @Autowired
    private AiService aiService;

    /**
     * 向 Gemini 提问
     */
    @PostMapping("/ask")
    public CommonResult<String> ask(@RequestBody String question) throws IOException {
        log.info("收到AI提问请求: {}", question);

        if (question == null || question.trim().isEmpty()) {
            return CommonResult.validateFailed("问题内容不能为空");
        }

        String answer = geminiService.askQuestion(question);
        if (answer == null || answer.isEmpty()) {
            return CommonResult.failed("AI 未返回有效回答");
        }

        Long userId = aiService.getCurrentUserId();
        if (userId == null) {
            return CommonResult.unauthorized("未登录");
        }

        // 保存对话记录
        String sessionId = "sess_" + System.currentTimeMillis();
        ConversationHistory history = aiCustomerService.buildConversationHistory(userId, sessionId, question, answer);
        aiCustomerService.saveConversation(history);

        return CommonResult.success(answer, "成功获取回答");
    }
}
