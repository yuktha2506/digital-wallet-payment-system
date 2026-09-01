package com.example.wallet.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "^[0-9]{10,15}$", message = "phone must contain 10 to 15 digits") String phone,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}
