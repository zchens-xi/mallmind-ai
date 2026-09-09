package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * 邮件发送服务实现类
 * @author macro
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public boolean sendAuthCodeEmail(String to, String authCode) {
        String subject = "Mall商城 - 验证码";
        String content = buildAuthCodeEmailContent(authCode);
        return sendHtmlEmail(to, subject, content);
    }

    @Override
    public boolean sendSimpleTextEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("简单文本邮件发送成功, 收件人: {}", to);
            return true;
        } catch (Exception e) {
            log.error("发送简单文本邮件失败, 收件人: {}, 错误: {}", to, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true表示HTML格式

            mailSender.send(message);
            log.info("HTML邮件发送成功, 收件人: {}", to);
            return true;
        } catch (Exception e) {
            log.error("发送HTML邮件失败, 收件人: {}, 错误: {}", to, e.getMessage());
            return false;
        }
    }

    /**
     * 构建验证码邮件内容（HTML格式）
     */
    private String buildAuthCodeEmailContent(String authCode) {
        StringBuilder content = new StringBuilder();
        content.append("<html>");
        content.append("<head><meta charset='UTF-8'></head>");
        content.append("<body>");
        content.append("<div style='padding: 20px; font-family: Arial, sans-serif;'>");
        content.append("<h2 style='color: #333; text-align: center;'>Mall商城验证码</h2>");
        content.append("<div style='background-color: #f9f9f9; padding: 20px; border-radius: 5px; margin: 20px 0;'>");
        content.append("<p style='color: #666; font-size: 14px;'>您好！</p>");
        content.append("<p style='color: #666; font-size: 14px;'>您正在进行邮箱验证，验证码为：</p>");
        content.append("<div style='text-align: center; margin: 20px 0;'>");
        content.append("<span style='font-size: 24px; font-weight: bold; color: #ff6b6b; background-color: #fff; padding: 10px 20px; border: 2px dashed #ff6b6b; border-radius: 5px;'>");
        content.append(authCode);
        content.append("</span>");
        content.append("</div>");
        content.append("<p style='color: #666; font-size: 14px;'>验证码有效期为5分钟，请及时使用。</p>");
        content.append("<p style='color: #999; font-size: 12px;'>如果这不是您的操作，请忽略此邮件。</p>");
        content.append("</div>");
        content.append("<div style='text-align: center; margin-top: 30px;'>");
        content.append("<p style='color: #999; font-size: 12px;'>此邮件由系统自动发送，请勿回复。</p>");
        content.append("<p style='color: #999; font-size: 12px;'>© 2025 Mall商城</p>");
        content.append("</div>");
        content.append("</div>");
        content.append("</body>");
        content.append("</html>");
        return content.toString();
    }
}
