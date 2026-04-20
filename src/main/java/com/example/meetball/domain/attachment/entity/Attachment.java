package com.example.meetball.domain.attachment.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "attachments")
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_path")
    private String storedFilePath;

    @Column(name = "link_url") // 링크 구분을 위해 추가
    private String linkUrl;

    @Column(name = "type") // FILE 또는 LINK
    private String type;

    @Column(name = "file_size")
    private Long fileSize;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Attachment(Long projectId, String originalFileName, String storedFilePath, String linkUrl, String type, Long fileSize) {
        this.projectId = projectId;
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath;
        this.linkUrl = linkUrl;
        this.type = type;
        this.fileSize = fileSize;
    }
}
