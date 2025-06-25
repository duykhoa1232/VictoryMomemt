package com.example.victorymoments.service;

import com.example.victorymoments.entity.Media;
import com.example.victorymoments.entity.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaService {
    Media uploadMedia(MultipartFile file, MediaType type, String postId);
    List<Media> listByPostId(String postId);
    void deleteMedia(String id);
    Media save(Media media);
}
