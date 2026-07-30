package com.vennhuu.TaskManagementSystem.Service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String mail ; 

    private final MailSender mailSender ; 
    private final JavaMailSender javaMailSender ;
    private final SpringTemplateEngine templateEngine ;

    public EmailService(MailSender mailSender, JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.javaMailSender = javaMailSender ;
        this.templateEngine = templateEngine ;
    }

    // public void sendSimpleEmail(String to, String subject, String body) {
    //     SimpleMailMessage message = new SimpleMailMessage();
    //     message.setFrom(mail);
    //     message.setTo(to);
    //     message.setSubject(subject);
    //     message.setText(body);

    //     mailSender.send(message);
    // }

    // @Async
    // public void sendEmailSync(String to, String subject, String content, boolean isMultipart, boolean isHTML) {
        
    //     MimeMessage message = javaMailSender.createMimeMessage();

    //     try {
    //          MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart, StandardCharsets.UTF_8.name());
        
    //         helper.setFrom(mail);
    //         helper.setTo(to);
    //         helper.setSubject(subject);
            
    //         // Tham số true thứ hai xác nhận nội dung là HTML
    //         helper.setText(content, isHTML); 

    //         javaMailSender.send(message);
    //     } catch (Exception e) {
    //         System.out.println("ERROR MAIL: " + e);
    //     }

    // }

    // public void sendHtmlEmail(String to, String subject, String name, String messageContent, boolean isMultipart, boolean isHtml) {
    //     MimeMessage message = javaMailSender.createMimeMessage();

    //     try {
    //         MimeMessageHelper helper = new MimeMessageHelper(message, isMultipart, StandardCharsets.UTF_8.name());

    //         Context context = new Context();
    //         context.setVariable("name", name);
    //         context.setVariable("message", messageContent);

    //         String htmlContent = templateEngine.process("email-template", context);

    //         helper.setTo(to);
    //         helper.setSubject(subject);
    //         helper.setText(htmlContent, isHtml); // true nghĩa là gửi dạng HTML

    //         // 4. Gửi email
    //         javaMailSender.send(message);
    //     } catch (Exception e) {
    //         System.out.println("ERROR MAIL: " + e);
    //     }

    // }

    public void sendAssignTaskEmail(String to, String fullName, String projectName, String taskTitle) {

        try {

            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper( message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(mail);
            helper.setTo(to);
            helper.setSubject("Bạn được giao một Task mới");

            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("projectName", projectName);
            context.setVariable("taskTitle", taskTitle);

            String html = templateEngine.process( "template", context);

            helper.setText(html, true);

            this.javaMailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Không gửi được email", e);
        }
    }
}
