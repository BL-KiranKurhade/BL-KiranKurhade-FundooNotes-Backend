package com.bridgelabz.fundoonotes.messaging;

import com.bridgelabz.fundoonotes.config.RabbitMQConfig;
import com.bridgelabz.fundoonotes.dto.EmailDto;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendEmailMessage(EmailDto emailDto) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY_EMAIL, emailDto);
        System.out.println("Message sent to RabbitMQ for email: " + emailDto.getTo());
    }
}
