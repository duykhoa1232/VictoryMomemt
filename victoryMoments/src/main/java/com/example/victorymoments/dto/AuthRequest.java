package com.example.victorymoments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "{auth.email.required}")
    @Size(min = 5, max = 100, message = "{auth.email.size}")
    private String email;

    @NotBlank(message = "{auth.password.required}")
    @Size(min = 8, message = "{auth.password.size}")
    private String password;
}
