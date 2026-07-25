package com.vennhuu.TaskManagementSystem.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Service.EmailService;
import com.vennhuu.TaskManagementSystem.Utils.annotation.APIMessage;


@RequestMapping("/api/v1")
@RestController
public class EmailController {
    
    private final EmailService emailService ;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/email")
    @APIMessage("Test send mail")
    public String sendEmail() {
        // this.emailService.sendSimpleEmail("phanhuuphuoc0512@gmail.com", "Hello", "I Love You");
        // this.emailService.sendEmailSync("phanhuuphuoc0512@gmail.com", "Test Mail", "<h1> Test Mail <h1>", false, true);
        emailService.sendHtmlEmail(
            "phanhuuphuoc0512@gmail.com",
            "Bạn có Task mới",
            "Phước",
            "Bạn vừa được giao task mới.",
            false,
            true);

    return "Send success";
    }
    
}
