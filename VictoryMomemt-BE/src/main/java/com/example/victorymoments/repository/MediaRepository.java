package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Media;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> findByPostId(String postId);
}
