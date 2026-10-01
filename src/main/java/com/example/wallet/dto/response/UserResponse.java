package com.example.wallet.dto.response;
import com.example.wallet.entity.Role;
import java.time.Instant;

public record UserResponse(Long id, String name, String email, String phone, Role role, Instant createdAt) {
}
