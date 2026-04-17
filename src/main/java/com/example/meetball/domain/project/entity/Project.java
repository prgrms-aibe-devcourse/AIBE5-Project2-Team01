package com.example.meetball.domain.project.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    
    private String description;
    
    @Column(name = "project_type")
    private String projectType;
    
    @Column(name = "progress_method")
    private String progressMethod;
    
    @Column(name = "recruitment_count")
    private Integer recruitmentCount;
    
    @Column(name = "recruitment_start_at")
    private LocalDate recruitmentStartAt;
    
    @Column(name = "recruitment_end_at")
    private LocalDate recruitmentEndAt;
    
    @Column(name = "project_start_at")
    private LocalDate projectStartAt;
    
    @Column(name = "project_end_at")
    private LocalDate projectEndAt;
    
    private Boolean closed;
    
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Project() {
    }

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
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getProgressMethod() { return progressMethod; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public LocalDate getRecruitmentStartAt() { return recruitmentStartAt; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public LocalDate getProjectStartAt() { return projectStartAt; }
    public LocalDate getProjectEndAt() { return projectEndAt; }
    public Boolean getClosed() { return closed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

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
    }
}
