package com.example.meetball.domain.projectresource.entity;

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
@Table(name = "project_resource")
public class ProjectResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "saved_file_name", nullable = false)
    private String storedFilePath;

    @Column(name = "file_url", nullable = false)
    private String linkUrl;

    @Column(name = "file_type")
    private String type;

    @Column(name = "tab_type")
    private String tabType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ProjectResource(Long projectId, String originalFileName, String storedFilePath, String linkUrl, String type, Long fileSize) {
        this.projectId = projectId;
        this.originalFileName = originalFileName;
        this.storedFilePath = storedFilePath != null ? storedFilePath : originalFileName;
        this.linkUrl = linkUrl != null ? linkUrl : storedFilePath;
        this.type = type;
        this.fileSize = fileSize;
        this.tabType = "RECRUIT";
        this.displayOrder = 0;
    }
}
