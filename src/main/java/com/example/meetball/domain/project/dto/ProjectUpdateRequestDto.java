package com.example.meetball.domain.project.dto;

import java.time.LocalDate;
import java.util.List;

public class ProjectUpdateRequestDto {

    private String title;
    private String description;
    private String projectType;
    private String progressMethod;
    private String position;
    private List<String> techStacks = List.of();
    private String thumbnailUrl;
    private Integer recruitmentCount;
    private LocalDate recruitmentStartAt;
    private LocalDate recruitmentEndAt;
    private LocalDate projectStartAt;
    private LocalDate projectEndAt;
    private Boolean closed;
    private Boolean completed;

    public ProjectUpdateRequestDto() {
    }

    public ProjectUpdateRequestDto(String title, String description, String projectType, String progressMethod,
                                   Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                                   LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed) {
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
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getProgressMethod() { return progressMethod; }
    public String getPosition() { return position; }
    public List<String> getTechStacks() { return techStacks == null ? List.of() : techStacks; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public LocalDate getRecruitmentStartAt() { return recruitmentStartAt; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public LocalDate getProjectStartAt() { return projectStartAt; }
    public LocalDate getProjectEndAt() { return projectEndAt; }
    public Boolean getClosed() { return closed; }
    public Boolean getCompleted() { return completed; }
}
