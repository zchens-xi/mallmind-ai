package com.macro.mall.portal.controller;

import com.macro.mall.ai.model.mongo.ConversationHistory;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.AiService;
import io.swagger.annotations.Api;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DeepSeek AI 对接控制器
 * 提供AI客服接口，并自动记录对话历史
 *
 * 实际是用的Kimi，更名有点麻烦，能跑暂时不改。
 */
@RestController
@RequestMapping("/ai/deepseek")
@Api(tags = "DeepSeekController")
@Tag(name = "DeepSeekController", description = "对接 DeepSeek 大模型 API")
@Slf4j
public class DeepSeekController {

    @Autowired
    private AiService aiService;

    /**
     * 向 DeepSeek 提问
     */
    @PostMapping("/ask")
    public CommonResult<String> ask(@RequestParam(value = "message") String question) {
        try {
            String answer = aiService.askDeepSeek(question);
            return CommonResult.success(answer, "成功获取回答");
        } catch (IllegalArgumentException e) {
            return CommonResult.validateFailed(e.getMessage());
        } catch (IllegalStateException e) {
            return CommonResult.unauthorized(e.getMessage());
        } catch (Exception e) {
            log.error("处理 AI 请求失败", e);
            return CommonResult.failed("系统内部错误，请稍后再试");
        }
    }

    @PostMapping("/ask/customer")
    public CommonResult<String> askCustomer(@RequestParam String question,@RequestParam Long productId) {
        try {
            String answer = aiService.askCustomer(question,productId);
            return CommonResult.success(answer, "成功获取回答");
        } catch (IllegalArgumentException e) {
            return CommonResult.validateFailed(e.getMessage());
        } catch (IllegalStateException e) {
            return CommonResult.unauthorized(e.getMessage());
        } catch (Exception e) {
            log.error("处理 AI 请求失败", e);
            return CommonResult.failed("系统内部错误，请稍后再试");
        }
    }

    /**
     * 获取对话历史
     */
    @PostMapping("/history")
    public CommonResult<List<ConversationHistory>> getHistory(@RequestParam Long productId) {
        try {
            List<ConversationHistory> history = aiService.getHistory(productId);
            return CommonResult.success(history);
        } catch (IllegalStateException e) {
            return CommonResult.failed(e.getMessage());
        } catch (Exception e) {
            log.error("获取聊天记录失败", e);
            return CommonResult.failed("系统内部错误，请稍后再试");
        }
    }
}
