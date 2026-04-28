package com.example.meetball.global.storage;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Override
    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String safeOriginalFilename = StringUtils.cleanPath(originalFilename == null ? "file" : originalFilename);
        String storedFileName = UUID.randomUUID() + "_" + safeOriginalFilename;

        s3Template.upload(bucket, storedFileName, file.getInputStream());
        return storedFileName;
    }

    @Override
    public Resource load(String storedFileName) {
        return s3Template.download(bucket, storedFileName);
    }

    @Override
    public void delete(String storedFileName) {
        s3Template.deleteObject(bucket, storedFileName);
    }
}
