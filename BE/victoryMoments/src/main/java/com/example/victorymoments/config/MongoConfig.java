package com.example.victorymoments.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing // Kích hoạt tính năng auditing cho MongoDB
public class MongoConfig {
    // Không cần thêm code gì trong class này
}