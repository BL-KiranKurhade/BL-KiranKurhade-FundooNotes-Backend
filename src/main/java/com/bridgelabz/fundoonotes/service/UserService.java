package com.bridgelabz.fundoonotes.service;

import com.bridgelabz.fundoonotes.dto.UserRegistrationDto;
import com.bridgelabz.fundoonotes.model.User;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
}
