package com.cloud.employee.service.impl;

import com.cloud.employee.service.IMailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class MailServiceImpl implements IMailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:none}")
    private String from;

    @Override
    public void sendHtmlMailWithAttachment(String to, String subject, String content, File attachment) {
        if (mailSender == null || "none".equals(from)) {
            log.warn("邮件服务器未配置，模拟发送邮件至: {}, 主题: {}", to, subject);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            if (attachment != null) {
                helper.addAttachment(attachment.getName(), attachment);
            }
            mailSender.send(message);
            log.info("邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("发送邮件失败: ", e);
            throw new RuntimeException("邮件发送失败");
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        sendHtmlMailWithAttachment(to, subject, content, null);
    }
}
