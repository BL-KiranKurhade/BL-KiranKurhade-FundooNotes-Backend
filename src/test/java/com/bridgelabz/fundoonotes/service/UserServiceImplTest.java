package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.EmailDto;
import com.bridgelabz.fundoonotes.dto.UserRegistrationDto;
import com.bridgelabz.fundoonotes.messaging.MessageProducer;
import com.bridgelabz.fundoonotes.model.User;
import com.bridgelabz.fundoonotes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private MessageProducer messageProducer;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;
    private User user;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEmail("johndoe@example.com");
        registrationDto.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("johndoe@example.com");
        user.setPassword("encodedPassword");
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User savedUser = userService.registerUser(registrationDto);

        assertNotNull(savedUser);
        assertEquals("johndoe@example.com", savedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(messageProducer, times(1)).sendEmailMessage(any(EmailDto.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyTaken() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.registerUser(registrationDto));
        
        assertEquals("Email is already taken!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(messageProducer, never()).sendEmailMessage(any(EmailDto.class));
    }

    @Test
    void testForgotPassword_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        userService.forgotPassword("johndoe@example.com");

        verify(valueOperations, times(1)).set(anyString(), eq("johndoe@example.com"), anyLong(), any());
        verify(messageProducer, times(1)).sendEmailMessage(any(EmailDto.class));
    }

    @Test
    void testForgotPassword_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> userService.forgotPassword("unknown@example.com"));
        
        assertEquals("User not found with email: unknown@example.com", exception.getMessage());
        verify(messageProducer, never()).sendEmailMessage(any(EmailDto.class));
    }

    @Test
    void testResetPassword_Success() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RESET_validToken")).thenReturn("johndoe@example.com");
        when(userRepository.findByEmail("johndoe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        userService.resetPassword("validToken", "newPassword123");

        verify(userRepository, times(1)).save(any(User.class));
        verify(redisTemplate, times(1)).delete("RESET_validToken");
    }

    @Test
    void testResetPassword_InvalidToken() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("RESET_invalidToken")).thenReturn(null);

        Exception exception = assertThrows(RuntimeException.class, () -> userService.resetPassword("invalidToken", "newPassword123"));
        
        assertEquals("Invalid or expired password reset token!", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
