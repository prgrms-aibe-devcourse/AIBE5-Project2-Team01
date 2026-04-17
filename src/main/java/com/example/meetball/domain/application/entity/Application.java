package com.example.meetball.domain.application.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import java.time.LocalDateTime;

@Entity
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "applicant_name")
    private String applicantName;

    private String position;
    private String message;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Application() {
    }

    public Application(Long projectId, String applicantName, String position, String message, 
                       ApplicationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.projectId = projectId;
        this.applicantName = applicantName;
        this.position = position;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void updateStatus(ApplicationStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getProjectId() { return projectId; }
    public String getApplicantName() { return applicantName; }
    public String getPosition() { return position; }
    public String getMessage() { return message; }
    public ApplicationStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
