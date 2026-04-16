package com.example.meetball.domain.attachment.dto;

import com.example.meetball.domain.attachment.entity.Attachment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AttachmentResponseDto {
    private Long id;
    private String originalFileName;
    private Long fileSize;
    private LocalDateTime createdAt;

    public AttachmentResponseDto(Attachment attachment) {
        this.id = attachment.getId();
        this.originalFileName = attachment.getOriginalFileName();
        this.fileSize = attachment.getFileSize();
        this.createdAt = attachment.getCreatedAt();
    }
}
