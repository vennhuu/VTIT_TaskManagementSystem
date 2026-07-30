package com.vennhuu.TaskManagementSystem.Service.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.vennhuu.TaskManagementSystem.Config.RabbitMQConfig;
import com.vennhuu.TaskManagementSystem.Entity.res.AssignTaskEmailMessage;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendAssignTaskEmail(AssignTaskEmailMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ASSIGN_TASK_EXCHANGE,
                RabbitMQConfig.ASSIGN_TASK_ROUTING_KEY,
                message
        );
    }
}