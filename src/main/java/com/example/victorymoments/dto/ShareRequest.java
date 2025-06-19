package com.example.victorymoments.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareRequest {
    @NotNull(message = "{share.originalPostId.required}")
    private String originalPostId;

    private String content;
}
