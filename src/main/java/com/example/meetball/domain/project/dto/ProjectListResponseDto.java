package com.example.meetball.domain.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectListResponseDto {

    private Long id;
    private String title;
    private Integer recruitmentCount;
    private String projectType;
    private String progressMethod;
    private LocalDate recruitmentEndAt;
    private Boolean closed;
    private LocalDateTime createdAt;

    public ProjectListResponseDto(Long id, String title, Integer recruitmentCount, 
                                  String projectType, String progressMethod, 
                                  LocalDate recruitmentEndAt, Boolean closed, 
                                  LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.recruitmentCount = recruitmentCount;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.recruitmentEndAt = recruitmentEndAt;
        this.closed = closed;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public String getProjectType() { return projectType; }
    public String getProgressMethod() { return progressMethod; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public Boolean getClosed() { return closed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
