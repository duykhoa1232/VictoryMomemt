package com.example.victorymoments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileRequest {
    private String name;

    @NotBlank(message = "{profile.phone.required}")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,15}$", message = "{profile.phone.invalid}")
    private String phoneNumber;

    //@NotBlank(message = "{profile.email.required}")
    //@Size(min = 5, max = 100, message = "{profile.email.length}")
    //private String email;
}
