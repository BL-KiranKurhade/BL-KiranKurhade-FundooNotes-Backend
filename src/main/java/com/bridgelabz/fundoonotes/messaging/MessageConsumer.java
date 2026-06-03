package com.bridgelabz.fundoonotes.messaging;

import com.bridgelabz.fundoonotes.config.RabbitMQConfig;
import com.bridgelabz.fundoonotes.dto.EmailDto;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class MessageConsumer {

    @Autowired
    private JavaMailSender javaMailSender;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumeEmailMessage(EmailDto emailDto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailDto.getTo());
            message.setSubject(emailDto.getSubject());
            message.setText(emailDto.getBody());
            message.setFrom("kiran12kurhade@gmail.com");
            
            // Un-comment to send actual email if SMTP credentials are valid
            javaMailSender.send(message); 
            
            System.out.println("Email consumed and processed successfully for: " + emailDto.getTo());
        } catch (Exception e) {
            System.err.println("Error processing email message: " + e.getMessage());
        }
    }
}
