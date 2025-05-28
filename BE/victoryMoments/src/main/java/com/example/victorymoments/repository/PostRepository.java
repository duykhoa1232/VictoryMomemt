package com.example.victorymoments.repository;

import com.example.victorymoments.entity.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PostRepository extends MongoRepository<Post, String> {
}
