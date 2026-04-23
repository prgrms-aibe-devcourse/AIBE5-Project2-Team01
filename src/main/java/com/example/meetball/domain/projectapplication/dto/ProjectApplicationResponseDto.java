package com.example.meetball.domain.projectapplication.dto;

import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.entity.ProjectApplicationStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ProjectApplicationResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle; // 추가: 프로젝트 이름
    private Long profileId;
    private String profileNickname; // 팀장 화면에서 지원자 식별용
    private String applicantName;
    private String position;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectApplicationResponseDto(ProjectApplication application) {
        this.id = application.getId();
        this.projectId = application.getProjectId() != null ? application.getProjectId() : (application.getProject() != null ? application.getProject().getId() : null);
        this.projectTitle = application.getProject() != null ? application.getProject().getTitle() : null;
        this.profileId = application.getProfile() != null ? application.getProfile().getId() : null;
        this.profileNickname = application.getProfile() != null ? application.getProfile().getNickname() : null;
        this.applicantName = application.getApplicantName();
        this.position = application.getRecruitPosition() != null
                ? application.getRecruitPosition().getPositionName()
                : application.getPosition();
        this.message = application.getMessage();
        this.status = normalizeStatus(application.getStatus());
        this.createdAt = application.getCreatedAt();
        this.updatedAt = application.getUpdatedAt();
    }

    private String normalizeStatus(ProjectApplicationStatus status) {
        if (status == null) {
            return null;
        }
        if (status == ProjectApplicationStatus.APPROVED) {
            return ProjectApplicationStatus.ACCEPTED.name();
        }
        return status.name();
    }
}
