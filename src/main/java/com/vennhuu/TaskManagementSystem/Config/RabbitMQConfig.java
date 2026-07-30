package com.vennhuu.TaskManagementSystem.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    public static final String ASSIGN_TASK_QUEUE = "assign-task-email.queue";
    public static final String ASSIGN_TASK_EXCHANGE = "assign-task.exchange";
    public static final String ASSIGN_TASK_ROUTING_KEY = "assign-task.email";

    @Bean
    public Queue assignTaskQueue() {
        return QueueBuilder
                .durable(ASSIGN_TASK_QUEUE)
                .build();
    }

    @Bean
    public DirectExchange assignTaskExchange() {
        return new DirectExchange(ASSIGN_TASK_EXCHANGE);
    }

    @Bean
    public Binding assignTaskBinding( Queue assignTaskQueue, DirectExchange assignTaskExchange ) {
        return BindingBuilder
                .bind(assignTaskQueue)
                .to(assignTaskExchange)
                .with(ASSIGN_TASK_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
