package com.example.meetball.domain.application.dto;

import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ApplicationResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle; // 추가: 프로젝트 이름
    private Long userId;
    private String userNickname; // 팀장 화면에서 지원자 식별용
    private String applicantName;
    private String position;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponseDto(Application application) {
        this.id = application.getId();
        this.projectId = application.getProjectId() != null ? application.getProjectId() : (application.getProject() != null ? application.getProject().getId() : null);
        this.projectTitle = application.getProject() != null ? application.getProject().getTitle() : null;
        this.userId = application.getUser() != null ? application.getUser().getId() : null;
        this.userNickname = application.getUser() != null ? application.getUser().getNickname() : null;
        this.applicantName = application.getApplicantName();
        this.position = application.getProjectPosition() != null
                ? application.getProjectPosition().getPositionName()
                : application.getPosition();
        this.message = application.getMessage();
        this.status = normalizeStatus(application.getStatus());
        this.createdAt = application.getCreatedAt();
        this.updatedAt = application.getUpdatedAt();
    }

    private String normalizeStatus(ApplicationStatus status) {
        if (status == null) {
            return null;
        }
        if (status == ApplicationStatus.APPROVED) {
            return ApplicationStatus.ACCEPTED.name();
        }
        return status.name();
    }
}
