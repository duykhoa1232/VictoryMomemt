package com.example.victorymoments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir; // Đọc từ application.properties

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/media/**") // URL Path mà frontend sẽ gọi (ví dụ: http://localhost:8080/media/images/ten_file.png)
                .addResourceLocations("file:" + uploadDir + "/"); // Đường dẫn vật lý trên hệ thống file server
    }
}