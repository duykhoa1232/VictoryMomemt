package com.example.victorymoments.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties
public class S3Config {

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                             @Value("${cloud.aws.credentials.secret-key}") String secretKey) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create("https://s3." + region + ".amazonaws.com"))
                .build();
    }

    @Bean
    public S3Presigner s3Presigner(@Value("${cloud.aws.credentials.access-key}") String accessKey,
                                   @Value("${cloud.aws.credentials.secret-key}") String secretKey) {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
