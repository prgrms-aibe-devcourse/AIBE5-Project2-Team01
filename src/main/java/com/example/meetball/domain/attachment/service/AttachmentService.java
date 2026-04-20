package com.example.meetball.domain.attachment.service;

import com.example.meetball.domain.attachment.dto.AttachmentResponseDto;
import com.example.meetball.domain.attachment.entity.Attachment;
import com.example.meetball.domain.attachment.repository.AttachmentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
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
    private final String uploadDir = "uploads/";

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
            String originalFilename = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
            Path filePath = Paths.get(uploadDir + storedFileName);
            
            // 물리적 파일 저장
            Files.copy(file.getInputStream(), filePath);

            // 메타데이터 DB 저장
            Attachment attachment = Attachment.builder()
                    .projectId(projectId)
                    .originalFileName(originalFilename)
                    .storedFilePath(storedFileName) 
                    .type("FILE")
                    .fileSize(file.getSize())
                    .build();

            Attachment savedAttachment = attachmentRepository.save(attachment);
            return new AttachmentResponseDto(savedAttachment);

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
    public Resource loadFileAsResource(Long attachmentId) {
        try {
            Attachment attachment = attachmentRepository.findById(attachmentId)
                    .orElseThrow(() -> new IllegalArgumentException("File not found with id: " + attachmentId));
            
            Path filePath = Paths.get(uploadDir).resolve(attachment.getStoredFilePath()).normalize();
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
    public String getOriginalFileName(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"))
                .getOriginalFileName();
    }

    @Transactional
    public AttachmentResponseDto uploadLink(Long projectId, String title, String url) {
        Attachment attachment = Attachment.builder()
                .projectId(projectId)
                .originalFileName(title)
                .linkUrl(url)
                .type("LINK")
                .build();
        
        Attachment saved = attachmentRepository.save(attachment);
        return new AttachmentResponseDto(saved);
    }
}
