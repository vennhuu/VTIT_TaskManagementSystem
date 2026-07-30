package com.vennhuu.TaskManagementSystem.Service.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Config.RabbitMQConfig;
import com.vennhuu.TaskManagementSystem.Entity.res.AssignTaskEmailMessage;
import com.vennhuu.TaskManagementSystem.Service.EmailService;

@Service
public class RabbitMQConsumer {

    private final EmailService emailService;

    public RabbitMQConsumer( EmailService emailService ) {
        this.emailService = emailService;
    }

    @RabbitListener( queues = RabbitMQConfig.ASSIGN_TASK_QUEUE )
    public void receiveAssignTaskEmail( AssignTaskEmailMessage message ) {
        emailService.sendAssignTaskEmail(
                message.getEmail(),
                message.getFullName(),
                message.getProjectName(),
                message.getTaskTitle()
        );
    }
}