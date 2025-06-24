package com.example.victorymoments.service;

import com.example.victorymoments.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3CleanupService {

    private final S3Service s3Service;
    private final PostRepository postRepository;

    public void cleanUnusedImages() {
        Set<String> referencedKeys = postRepository.findAll().stream()
                .map(post -> post.getMediaIds() != null ? post.getMediaIds() : List.<String>of())
                .flatMap(List::stream)
                .map(url -> {
                    try {
                        URI uri = new URI(url);
                        String path = uri.getPath();
                        return path.startsWith("/") ? path.substring(1) : path;
                    } catch (Exception e) {
                        log.error("Error parsing S3 URL: {}", url, e);
                        return null;
                    }
                })
                .filter(k -> k != null && !k.trim().isEmpty())
                .collect(Collectors.toSet());

        List<String> allS3Keys = s3Service.listAllKeys("images/");

        log.info("Found {} referenced keys and {} total S3 keys", referencedKeys.size(), allS3Keys.size());

        for (String key : allS3Keys) {
            if (!referencedKeys.contains(key)) {
                log.info("Deleting unused S3 file: {}", key);
                s3Service.deleteFile(key);
            }
        }
    }

    private String extractS3KeyFromUrl(String s3Url) {
        try {
            URI uri = new URI(s3Url);
            String path = uri.getPath();
            return (path != null && path.startsWith("/")) ? path.substring(1) : path;
        } catch (Exception e) {
            log.error("Error parsing S3 URL: {}", s3Url, e);
            return null;
        }
    }
}


