package top.dream.tool;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;

public class EmailMessage {
    public static MimeMessage createMimeMessage(Session session, String sendMail, String receiveMail, String emailTitle, String emailContext) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, emailTitle, "UTF-8"));
        // 3. To: 收件人
        message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(receiveMail, "", "UTF-8"));
        // 4. Subject: 邮件主题
        message.setSubject(emailTitle, "UTF-8");
        // 5. Content: 邮件正文（可以使用html标签）
        message.setContent(emailContext, "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());
        // 7. 保存设置
        message.saveChanges();
        return message;
    }
}
