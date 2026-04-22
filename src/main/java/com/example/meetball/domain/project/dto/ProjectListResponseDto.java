package com.example.meetball.domain.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectListResponseDto {

    private Long id;
    private String title;
    private String summary;
    private String thumbnailUrl;
    private Integer currentRecruitment;
    private Integer totalRecruitment;
    private String projectType;
    private String progressMethod;
    private String position;
    private List<String> techStacks;
    private LocalDate recruitmentEndAt;
    private String dDay;
    private Boolean closed;
    private LocalDateTime createdAt;

    public ProjectListResponseDto(Long id, String title, String summary, String thumbnailUrl,
                                  Integer currentRecruitment, Integer totalRecruitment, String projectType,
                                  String progressMethod, String position, List<String> techStacks,
                                  LocalDate recruitmentEndAt, String dDay, Boolean closed, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.position = position;
        this.techStacks = techStacks;
        this.recruitmentEndAt = recruitmentEndAt;
        this.dDay = dDay;
        this.closed = closed;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getCurrentRecruitment() { return currentRecruitment; }
    public Integer getTotalRecruitment() { return totalRecruitment; }
    public String getProjectType() { return projectType; }
    public String getProgressMethod() { return progressMethod; }
    public String getPosition() { return position; }
    public List<String> getTechStacks() { return techStacks; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public String getDDay() { return dDay; }
    public String getDeadlineLabel() { return dDay; }
    public Boolean getClosed() { return closed; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
