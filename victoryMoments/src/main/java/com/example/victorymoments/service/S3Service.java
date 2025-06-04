package com.example.victorymoments.service;

import com.example.victorymoments.exception.S3UploadException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${application.bucket.name}")
    private String bucketName;

    @Value("${application.upload.max-size:104857600}")
    private long maxUploadSize;

    private static final List<String> SUPPORTED_TYPES = List.of(
            "image/png", "image/jpeg", "image/jpg", "image/gif",
            "video/mp4", "video/mpeg",
            "audio/mpeg", "audio/mp3", "audio/wav"
    );

    public String uploadFile(MultipartFile file, String folder) {
        validateFileType(file);
        validateFileSize(file);

        String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
        String fileExtension = originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf('.'))
                : "";
        String fileName = folder + "/" + UUID.randomUUID() + fileExtension;

        try (var inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            String fileUrl = s3Client.utilities().getUrl(builder ->
                    builder.bucket(bucketName).key(fileName)).toString();

            logger.info("Đã upload file lên S3: {}", fileUrl);
            return fileUrl;
        } catch (IOException | S3Exception e) {
            logger.error("Lỗi khi upload file lên S3", e);
            throw new S3UploadException("Lỗi khi upload file lên S3", e);
        }
    }

    public List<String> uploadFiles(List<MultipartFile> files, String folder) {
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    urls.add(uploadFile(file, folder));
                }
            }
        }
        return urls;
    }

    public void deleteFile(String key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            logger.info("Đã xóa file trong S3: {}", key);
        } catch (S3Exception e) {
            logger.error("Lỗi khi xóa file trên S3", e);
            throw new S3UploadException("Lỗi khi xóa file trên S3", e);
        }
    }

    public String generatePresignedUrl(String key, int expiryInMinutes) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(expiryInMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();

            return s3Presigner.presignGetObject(presignRequest).url().toString();
        } catch (Exception e) {
            logger.error("Lỗi khi tạo URL tạm thời", e);
            throw new RuntimeException("Lỗi khi tạo URL tạm thời", e);
        }
    }

    private void validateFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || SUPPORTED_TYPES.stream().noneMatch(contentType::equalsIgnoreCase)) {
            throw new IllegalArgumentException("Loại file không được hỗ trợ: " + contentType);
        }
    }

    private void validateFileSize(MultipartFile file) {
        if (file.getSize() > maxUploadSize) {
            throw new IllegalArgumentException("File vượt quá kích thước tối đa cho phép: " + file.getSize());
        }
    }
}
