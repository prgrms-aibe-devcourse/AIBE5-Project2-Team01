package com.example.meetball.domain.application.dto;

import java.time.LocalDateTime;

public class ApplicationResponseDto {
    private Long id;
    private Long projectId;
    private String applicantName;
    private String position;
    private String message;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ApplicationResponseDto() {}

    public ApplicationResponseDto(Long id, Long projectId, String applicantName, String position,
                                  String message, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.projectId = projectId;
        this.applicantName = applicantName;
        this.position = position;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getApplicantName() { return applicantName; }
    public String getPosition() { return position; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
