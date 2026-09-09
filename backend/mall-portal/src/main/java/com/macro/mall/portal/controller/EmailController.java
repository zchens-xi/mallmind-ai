package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.portal.service.EmailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件发送测试控制器
 * @author macro
 */
@Api(tags = "邮件发送管理")
@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @ApiOperation("测试发送验证码邮件")
    @PostMapping("/sendAuthCode")
    public CommonResult<String> sendAuthCode(@RequestParam String email,
                                           @RequestParam String authCode) {
        boolean result = emailService.sendAuthCodeEmail(email, authCode);
        if (result) {
            return CommonResult.success("验证码邮件发送成功");
        } else {
            return CommonResult.failed("验证码邮件发送失败");
        }
    }

    @ApiOperation("测试发送简单文本邮件")
    @PostMapping("/sendSimpleText")
    public CommonResult<String> sendSimpleTextEmail(@RequestParam String email,
                                                  @RequestParam String subject,
                                                  @RequestParam String content) {
        boolean result = emailService.sendSimpleTextEmail(email, subject, content);
        if (result) {
            return CommonResult.success("文本邮件发送成功");
        } else {
            return CommonResult.failed("文本邮件发送失败");
        }
    }

    @ApiOperation("测试发送HTML格式邮件")
    @PostMapping("/sendHtml")
    public CommonResult<String> sendHtmlEmail(@RequestParam String email,
                                            @RequestParam String subject,
                                            @RequestParam String content) {
        boolean result = emailService.sendHtmlEmail(email, subject, content);
        if (result) {
            return CommonResult.success("HTML邮件发送成功");
        } else {
            return CommonResult.failed("HTML邮件发送失败");
        }
    }
}
