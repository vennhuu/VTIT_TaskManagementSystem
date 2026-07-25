package com.vennhuu.TaskManagementSystem.Service;

import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Entity.Task;

@Service
public class NotificationService {
    
     private final EmailService emailService;

    public NotificationService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void notifyAssignTask(Task task) {

        this.emailService.sendAssignTaskEmail(
                task.getAssignee().getEmail(),
                task.getAssignee().getFullName(),
                task.getProject().getName(),
                task.getTitle());

    }
}
