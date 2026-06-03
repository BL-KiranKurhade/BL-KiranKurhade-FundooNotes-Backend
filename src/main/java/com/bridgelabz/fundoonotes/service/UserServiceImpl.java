package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.UserRegistrationDto;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.bridgelabz.fundoonotes.dto.EmailDto;
import com.bridgelabz.fundoonotes.messaging.MessageProducer;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MessageProducer messageProducer;

    @Value("${FRONTEND_URL}")
    private String frontendUrl;

    @Override
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }

        User user = new User();
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

        User savedUser = userRepository.save(user);

        String welcomeMessage = "Welcome to Fundoo Notes, " + savedUser.getFirstName() + "!\n\n" +
            "Thank you for choosing Fundoo Notes to capture what's on your mind. We're thrilled to have you on board!\n\n" +
            "With Fundoo Notes, you can:\n" +
            "- 💡 Capture notes, lists, and ideas quickly.\n" +
            "- 🎨 Organize with labels, colors, and pins.\n" +
            "- 🔔 Set reminders so you never miss a deadline.\n" +
            "- 👤 Collaborate with friends, family, or colleagues in real-time.\n" +
            "- 📥 Archive ideas you want to keep hidden or trash what you no longer need.\n\n" +
            "Your account has been successfully created and is ready to go.\n\n" +
            "Get started now: " + frontendUrl + "/dashboard\n\n" +
            "Happy note-taking!\n" +
            "The Fundoo Notes Team";

        EmailDto emailDto = new EmailDto();
        emailDto.setTo(savedUser.getEmail());
        emailDto.setSubject("Welcome to Fundoo Notes!");
        emailDto.setBody(welcomeMessage);
        messageProducer.sendEmailMessage(emailDto);

        return savedUser;
    }

    @Override
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("RESET_" + resetToken, user.getEmail(), 10, TimeUnit.MINUTES);
        
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        
        EmailDto emailDto = new EmailDto();
        emailDto.setTo(user.getEmail());
        emailDto.setSubject("Password Reset Request - Fundoo Notes");
        emailDto.setBody("Hello " + user.getFirstName() + ",\n\nYou have requested to reset your password. Please click the link below to reset it (valid for 10 minutes):\n" + resetLink);
        
        messageProducer.sendEmailMessage(emailDto);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get("RESET_" + token);
        if (email == null) {
            throw new RuntimeException("Invalid or expired password reset token!");
        }
        
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found!"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        redisTemplate.delete("RESET_" + token);
    }
}
