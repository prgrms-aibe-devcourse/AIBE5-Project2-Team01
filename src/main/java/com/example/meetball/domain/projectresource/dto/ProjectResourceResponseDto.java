package com.example.meetball.domain.projectresource.dto;

import com.example.meetball.domain.projectresource.entity.ProjectResource;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProjectResourceResponseDto {
    private Long id;
    private String originalFileName;
    private Long fileSize;
    private String type; // FILE or LINK
    private String linkUrl;
    private String tabType;
    private LocalDateTime createdAt;

    public ProjectResourceResponseDto(ProjectResource projectResource) {
        this.id = projectResource.getId();
        this.originalFileName = projectResource.getOriginalFileName();
        this.fileSize = projectResource.getFileSize();
        this.type = projectResource.getType();
        this.linkUrl = projectResource.getLinkUrl();
        this.tabType = projectResource.getTabType();
        this.createdAt = projectResource.getCreatedAt();
    }
}
