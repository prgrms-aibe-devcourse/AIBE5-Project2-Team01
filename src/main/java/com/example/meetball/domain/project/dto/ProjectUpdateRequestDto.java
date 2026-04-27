package com.example.meetball.domain.project.dto;

import com.example.meetball.domain.project.entity.Project;

import java.time.LocalDate;
import java.util.List;

public class ProjectUpdateRequestDto {

    private String title;
    private String description;
    private String requiredQualifications;
    private String preferredQualifications;
    private String projectPurpose;
    private String workMethod;
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
    public String getRequiredQualifications() { return requiredQualifications; }
    public String getPreferredQualifications() { return preferredQualifications; }
    public String getProjectPurpose() { return projectPurpose; }
    public String getWorkMethod() { return workMethod; }
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
