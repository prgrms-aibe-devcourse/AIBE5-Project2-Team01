package com.example.meetball.domain.attachment.service;

import com.example.meetball.domain.attachment.dto.AttachmentResponseDto;
import com.example.meetball.domain.attachment.entity.Attachment;
import com.example.meetball.domain.attachment.repository.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    @Value("${app.upload-dir:uploads/}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    @Transactional
    public AttachmentResponseDto uploadFile(Long projectId, MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attachment file is required.");
            }
            String originalFilename = file.getOriginalFilename();
            String safeOriginalFilename = StringUtils.cleanPath(originalFilename == null ? "attachment" : originalFilename);
            if (safeOriginalFilename.contains("..")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file name.");
            }
            String storedFileName = UUID.randomUUID() + "_" + safeOriginalFilename;
            Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
            }
            
            // 물리적 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 메타데이터 DB 저장
            Attachment attachment = Attachment.builder()
                    .projectId(projectId)
                    .originalFileName(safeOriginalFilename)
                    .storedFilePath(storedFileName) 
                    .type("FILE")
                    .fileSize(file.getSize())
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);
            return new AttachmentResponseDto(savedAttachment);
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponseDto> getProjectAttachments(Long projectId) {
        return attachmentRepository.findByProjectId(projectId).stream()
                .map(AttachmentResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Resource loadFileAsResource(Long projectId, Long attachmentId) {
        try {
            Attachment attachment = findAttachmentInProject(projectId, attachmentId);

            Path filePath = resolveStoredFilePath(attachment.getStoredFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + attachment.getOriginalFileName());
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found", ex);
        }
    }

    @Transactional(readOnly = true)
    public String getOriginalFileName(Long projectId, Long attachmentId) {
        return findAttachmentInProject(projectId, attachmentId).getOriginalFileName();
    }

    @Transactional
    public AttachmentResponseDto uploadLink(Long projectId, String title, String url) {
        String safeTitle = StringUtils.hasText(title) ? title.trim() : "관련 링크";
        String safeUrl = validateHttpUrl(url);
        Attachment attachment = Attachment.builder()
                .projectId(projectId)
                .originalFileName(safeTitle)
                .linkUrl(safeUrl)
                .type("LINK")
                .build();
        
        Attachment saved = attachmentRepository.save(attachment);
        return new AttachmentResponseDto(saved);
    }

    @Transactional
    public void deleteProjectAttachments(Long projectId) {
        List<Attachment> attachments = attachmentRepository.findByProjectId(projectId);
        for (Attachment attachment : attachments) {
            if ("FILE".equals(attachment.getType()) && StringUtils.hasText(attachment.getStoredFilePath())) {
                try {
                    Files.deleteIfExists(resolveStoredFilePath(attachment.getStoredFilePath()));
                } catch (IOException e) {
                    throw new RuntimeException("Could not delete attachment file: " + attachment.getOriginalFileName(), e);
                }
            }
        }
        attachmentRepository.deleteAll(attachments);
    }

    private String validateHttpUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link URL is required.");
        }
        try {
            URI uri = new URI(url.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only HTTP/HTTPS links are allowed.");
            }
            return uri.toString();
        } catch (URISyntaxException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid link URL.", e);
        }
    }

    private Attachment findAttachmentInProject(Long projectId, Long attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found with id: " + attachmentId));
        if (!attachment.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in this project.");
        }
        return attachment;
    }

    private Path resolveStoredFilePath(String storedFilePath) {
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = uploadRoot.resolve(storedFilePath).normalize();
        if (!filePath.startsWith(uploadRoot)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path.");
        }
        return filePath;
    }
}
