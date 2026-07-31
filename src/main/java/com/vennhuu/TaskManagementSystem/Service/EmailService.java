package com.vennhuu.TaskManagementSystem.Service;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.vennhuu.TaskManagementSystem.Utils.errors.BadRequestException;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromAddress;

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailService(JavaMailSender javaMailSender, SpringTemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void sendAssignTaskEmail(String to, String fullName, String projectName, String taskTitle) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Bạn được giao một Task mới");

            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("projectName", projectName);
            context.setVariable("taskTitle", taskTitle);

            String html = templateEngine.process("template", context);
            helper.setText(html, true);

            javaMailSender.send(message);
        } catch (Exception e) {
            throw new BadRequestException("Không gửi được email: " + e.getMessage());
        }
    }
}
