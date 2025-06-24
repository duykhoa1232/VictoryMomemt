package com.example.victorymoments.mapper;

import com.example.victorymoments.entity.Media;
import com.example.victorymoments.response.MediaResponse;

public class MediaMapper {
    public static MediaResponse toResponse(Media media) {
        return new MediaResponse(
                media.getId(),
                media.getPath(),
                media.getType(),
                media.getPostId()
        );
    }
}
