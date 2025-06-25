package com.example.victorymoments.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.victorymoments.entity.VisibilityStatus;
import java.time.LocalDateTime;

@Document(collection = "shares")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share {
    @Id
    private String id;

    private String originalPostId;
    private String sharedByUserId;
    private String sharedByUserEmail;
    private String sharedByUserName;

    private String content;

    private VisibilityStatus visibility;

    private String parentShareId;

    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
    private boolean isActive = true;
}