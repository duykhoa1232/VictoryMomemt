package com.example.victorymoments.service.impl;

import com.example.victorymoments.entity.Media;
import com.example.victorymoments.entity.MediaType;
import com.example.victorymoments.exception.S3UploadException;
import com.example.victorymoments.repository.MediaRepository;
import com.example.victorymoments.service.MediaService;
import com.example.victorymoments.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImpl implements MediaService {

    private final MediaRepository mediaRepository;
    private final S3Service s3Service;

    @Override
    public Media uploadMedia(MultipartFile file, MediaType type, String postId) {
        log.debug("Uploading file of type {} for post {}", type, postId);
        String fileUrl = s3Service.uploadFile(file, "media");
        Media media = new Media();
        media.setPath(fileUrl);
        media.setType(type);
        media.setPostId(postId);
        return mediaRepository.save(media);
    }

    @Override
    public List<Media> listByPostId(String postId) {
        return mediaRepository.findByPostId(postId);
    }

    @Transactional
    @Override
    public void deleteMedia(String id) {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new S3UploadException("Media not found with id: " + id));
        try {
            String fileKey = extractS3KeyFromUrl(media.getPath());
            log.debug("Deleting file from S3: {}", fileKey);
            s3Service.deleteFile(fileKey);
            mediaRepository.deleteById(id);
        } catch (Exception e) {
            throw new S3UploadException("Error deleting file for media id " + id, e);
        }
    }

    @Override
    public Media save(Media media) {
        return mediaRepository.save(media);
    }

    private String extractS3KeyFromUrl(String s3Url) {
        try {
            URI uri = new URI(s3Url);
            return uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
        } catch (Exception e) {
            throw new S3UploadException("Error extracting S3 key from URL: " + s3Url, e);
        }
    }
}
