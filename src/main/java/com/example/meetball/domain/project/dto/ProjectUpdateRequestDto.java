package com.example.meetball.domain.project.dto;

import com.example.meetball.domain.project.entity.Project;

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
    private String recruitStatus = Project.RECRUIT_STATUS_OPEN;
    private String progressStatus = Project.PROGRESS_STATUS_READY;

    public ProjectUpdateRequestDto() {
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
    public String getRecruitStatus() { return recruitStatus; }
    public String getProgressStatus() { return progressStatus; }
}
