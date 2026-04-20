package com.example.meetball.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // --- Merged fields from HEAD and front2 ---

    @Column(length = 120)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(length = 4000)
    private String description;

    @Column(name = "project_type", length = 40)
    private String projectType;

    @Column(length = 40)
    private String position;

    @Column(name = "progress_method")
    private String progressMethod;

    @Column(length = 60)
    private String leaderName;

    @Column(length = 60)
    private String leaderRole;

    private String leaderAvatarUrl;

    private String thumbnailUrl;

    private Integer currentRecruitment;

    private Integer totalRecruitment;

    @Column(name = "recruitment_count")
    private Integer recruitmentCount;

    @Column(name = "recruitment_start_at")
    private LocalDate recruitmentStartAt;

    private LocalDate recruitmentDeadline;

    @Column(name = "recruitment_end_at")
    private LocalDate recruitmentEndAt;

    @Column(name = "project_start_at")
    private LocalDate projectStartAt;

    @Column(name = "project_end_at")
    private LocalDate projectEndAt;

    private Boolean closed;

    private LocalDate createdDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String techStackCsv;

    protected Project() {
    }

    // --- Constructor from HEAD ---
    public Project(String title, String description, String projectType, String progressMethod,
                   Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                   LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.recruitmentCount = recruitmentCount;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
        this.projectStartAt = projectStartAt;
        this.projectEndAt = projectEndAt;
        this.closed = closed;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        
        // Sync fields for safety
        this.createdDate = createdAt != null ? createdAt.toLocalDate() : null;
        this.recruitmentDeadline = recruitmentEndAt;
        this.totalRecruitment = recruitmentCount;
        this.currentRecruitment = 0;
        this.position = "";
        this.leaderName = "Unknown";
        this.leaderRole = "Member";
        this.thumbnailUrl = "";
        this.leaderAvatarUrl = "";
        this.summary = description != null && description.length() > 50 ? description.substring(0, 50) : description;
    }

    // --- Constructor from front2 ---
    public Project(
            String title,
            String summary,
            String description,
            String projectType,
            String position,
            String leaderName,
            String leaderRole,
            String leaderAvatarUrl,
            String thumbnailUrl,
            Integer currentRecruitment,
            Integer totalRecruitment,
            LocalDate recruitmentDeadline,
            LocalDate createdDate,
            String techStackCsv
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.projectType = projectType;
        this.position = position;
        this.leaderName = leaderName;
        this.leaderRole = leaderRole;
        this.leaderAvatarUrl = leaderAvatarUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.recruitmentDeadline = recruitmentDeadline;
        this.createdDate = createdDate;
        this.techStackCsv = techStackCsv;
        
        // Sync fields for safety
        this.recruitmentCount = totalRecruitment;
        this.recruitmentEndAt = recruitmentDeadline;
        this.createdAt = createdDate != null ? createdDate.atStartOfDay() : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.closed = false;
        this.progressMethod = "ONLINE"; // default
    }

    public void update(String title, String description, String projectType, String progressMethod,
                       Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                       LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.recruitmentCount = recruitmentCount;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
        this.projectStartAt = projectStartAt;
        this.projectEndAt = projectEndAt;
        this.closed = closed;
        this.updatedAt = updatedAt;
        
        this.recruitmentDeadline = recruitmentEndAt;
        this.totalRecruitment = recruitmentCount;
        this.summary = description != null && description.length() > 50 ? description.substring(0, 50) : description;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getPosition() { return position; }
    public String getProgressMethod() { return progressMethod; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getLeaderRole() { return leaderRole; }
    public String getLeaderAvatarUrl() { return leaderAvatarUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getCurrentRecruitment() { return currentRecruitment; }
    public Integer getTotalRecruitment() { return totalRecruitment; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public LocalDate getRecruitmentStartAt() { return recruitmentStartAt; }
    public LocalDate getRecruitmentDeadline() { return recruitmentDeadline; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public LocalDate getProjectStartAt() { return projectStartAt; }
    public LocalDate getProjectEndAt() { return projectEndAt; }
    public Boolean getClosed() { return closed; }
    public LocalDate getCreatedDate() { return createdDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getTechStackCsv() { return techStackCsv; }
}
