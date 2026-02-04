package com.cloud.employee.service;

import java.io.File;

public interface IMailService {
    /**
     * 发送带附件的 HTML 邮件
     * 
     * @param to         接收者
     * @param subject    主题
     * @param content    内容
     * @param attachment 附件文件
     */
    void sendHtmlMailWithAttachment(String to, String subject, String content, File attachment);

    /**
     * 发送简单 HTML 邮件
     */
    void sendHtmlMail(String to, String subject, String content);
}
