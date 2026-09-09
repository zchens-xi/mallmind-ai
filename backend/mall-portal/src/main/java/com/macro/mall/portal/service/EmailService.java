package com.macro.mall.portal.service;

/**
 * 邮件发送服务
 * @author macro
 */
public interface EmailService {

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param authCode 验证码
     * @return 发送结果
     */
    boolean sendAuthCodeEmail(String to, String authCode);

    /**
     * 发送简单文本邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @return 发送结果
     */
    boolean sendSimpleTextEmail(String to, String subject, String content);

    /**
     * 发送HTML格式邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content HTML内容
     * @return 发送结果
     */
    boolean sendHtmlEmail(String to, String subject, String content);
}
