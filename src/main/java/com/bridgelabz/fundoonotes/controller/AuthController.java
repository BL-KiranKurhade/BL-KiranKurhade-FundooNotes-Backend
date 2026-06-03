package com.bridgelabz.fundoonotes.controller;

import com.bridgelabz.fundoonotes.dto.AuthResponse;
import com.bridgelabz.fundoonotes.dto.LoginDto;
import com.bridgelabz.fundoonotes.dto.UserRegistrationDto;
import com.bridgelabz.fundoonotes.dto.ForgotPasswordDto;
import com.bridgelabz.fundoonotes.dto.ResetPasswordDto;
import com.bridgelabz.fundoonotes.security.JwtUtil;
import com.bridgelabz.fundoonotes.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.concurrent.TimeUnit;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            userService.registerUser(registrationDto);
            return new ResponseEntity<>(new AuthResponse(null, null, "User registered successfully"), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(new AuthResponse(null, null, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(loginDto.getEmail());
        
        String accessToken = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);
        
        // Cache tokens in Redis
        redisTemplate.opsForValue().set(accessToken, userDetails.getUsername(), 15, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(refreshToken, userDetails.getUsername(), 7, TimeUnit.DAYS);
        
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(refreshToken))) {
                String username = jwtUtil.extractUsername(refreshToken);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtUtil.validateToken(refreshToken, userDetails) && "refresh".equals(jwtUtil.extractType(refreshToken))) {
                    String newAccessToken = jwtUtil.generateToken(userDetails);
                    redisTemplate.opsForValue().set(newAccessToken, userDetails.getUsername(), 15, TimeUnit.MINUTES);
                    return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken, "Token refreshed successfully"));
                }
            }
        }
        return new ResponseEntity<>(new AuthResponse(null, null, "Invalid refresh token"), HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            redisTemplate.delete(jwt);
            return ResponseEntity.ok(new AuthResponse(null, null, "Logout successful"));
        }
        return new ResponseEntity<>(new AuthResponse(null, null, "Invalid token"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@Valid @RequestBody ForgotPasswordDto forgotPasswordDto) {
        userService.forgotPassword(forgotPasswordDto.getEmail());
        return ResponseEntity.ok(new AuthResponse(null, null, "Password reset link sent to your email."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody ResetPasswordDto resetPasswordDto) {
        userService.resetPassword(resetPasswordDto.getToken(), resetPasswordDto.getNewPassword());
        return ResponseEntity.ok(new AuthResponse(null, null, "Password has been successfully reset."));
    }
}
