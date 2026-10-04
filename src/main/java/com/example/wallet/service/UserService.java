package com.example.wallet.service;
import com.example.wallet.dto.request.LoginRequest;
import com.example.wallet.dto.request.RegisterRequest;
import com.example.wallet.dto.response.AuthResponse;
import com.example.wallet.dto.response.UserResponse;
import com.example.wallet.entity.User;
import java.util.List;

public interface UserService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    User getByEmail(String email);
    List<UserResponse> getAllUsers();
}
