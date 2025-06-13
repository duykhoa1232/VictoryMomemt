package com.example.victorymoments.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Victory Moments API")
                        .version("1.0.0")
                        .description("Tài liệu API cho ứng dụng mạng xã hội Victory Moments"));
    }
}

