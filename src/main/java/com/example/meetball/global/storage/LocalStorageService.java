package com.example.meetball.global.storage;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!", e);
        }
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String safeOriginalFilename = StringUtils.cleanPath(originalFilename == null ? "file" : originalFilename);
        String storedFileName = UUID.randomUUID() + "_" + safeOriginalFilename;

        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadRoot.resolve(storedFileName).normalize();
        
        if (!filePath.startsWith(uploadRoot)) {
            throw new IOException("Invalid file path.");
        }

        Files.copy(file.getInputStream(), filePath);
        return storedFileName;
    }

    @Override
    public Resource load(String storedFileName) {
        try {
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + storedFileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + storedFileName, e);
        }
    }

    @Override
    public void delete(String storedFileName) throws IOException {
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadRoot.resolve(storedFileName).normalize();
        Files.deleteIfExists(filePath);
    }
}
