package com.example.meetball.domain.application.dto;

import com.example.meetball.domain.application.entity.Application;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApplicationResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle; // 추가: 프로젝트 이름
    private Long userId;
    private String userNickname; // 팀장 화면에서 지원자 식별용
    private String position;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponseDto(Application application) {
        this.id = application.getId();
        this.projectId = application.getProject() != null ? application.getProject().getId() : null;
        this.projectTitle = application.getProject() != null ? application.getProject().getTitle() : null;
        this.userId = application.getUser() != null ? application.getUser().getId() : null;
        this.userNickname = application.getUser() != null ? application.getUser().getNickname() : null;
        this.position = application.getPosition();
        this.message = application.getMessage();
        this.status = application.getStatus() != null ? application.getStatus().name() : null;
        this.createdAt = application.getCreatedAt();
        this.updatedAt = application.getUpdatedAt();
    }
}

