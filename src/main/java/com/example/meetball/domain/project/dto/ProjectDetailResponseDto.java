package com.example.meetball.domain.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ProjectDetailResponseDto {

    private Long id;
    private String title;
    private String summary;
    private String description;
    private String projectPurpose;
    private String workMethod;
    private String position;
    private Long leaderProfileId;
    private String leaderName;
    private String leaderRole;
    private String leaderAvatarUrl;
    private String thumbnailUrl;
    private Integer currentRecruitment;
    private Integer totalRecruitment;
    private Integer recruitmentCount;
    private LocalDate recruitmentStartAt;
    private LocalDate recruitmentEndAt;
    private LocalDate projectStartAt;
    private LocalDate projectEndAt;
    private String recruitStatus;
    private String progressStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> techStacks;
    private List<ProjectRecruitPositionStatus> positionStatuses;
    private int readCount;

    public ProjectDetailResponseDto(Long id, String title, String description, String projectPurpose,
                                    String workMethod, Integer recruitmentCount, LocalDate recruitmentStartAt,
                                    LocalDate recruitmentEndAt, LocalDate projectStartAt, LocalDate projectEndAt,
                                    String recruitStatus, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(id, title, null, description, projectPurpose, workMethod, null, null, null, null, null, null,
                null, null, recruitmentCount, recruitmentStartAt, recruitmentEndAt, projectStartAt,
                projectEndAt, recruitStatus, "READY", createdAt, updatedAt, List.of(), List.of(), 0);
    }

    public ProjectDetailResponseDto(Long id, String title, String summary, String description, String projectPurpose,
                                    String workMethod, String position, Long leaderProfileId, String leaderName,
                                    String leaderRole, String leaderAvatarUrl, String thumbnailUrl, Integer currentRecruitment,
                                    Integer totalRecruitment, Integer recruitmentCount, LocalDate recruitmentStartAt,
                                    LocalDate recruitmentEndAt, LocalDate projectStartAt, LocalDate projectEndAt,
                                    String recruitStatus, String progressStatus, LocalDateTime createdAt, LocalDateTime updatedAt,
                                    List<String> techStacks,
                                    List<ProjectRecruitPositionStatus> positionStatuses) {
        this(id, title, summary, description, projectPurpose, workMethod, position, leaderProfileId, leaderName,
                leaderRole, leaderAvatarUrl, thumbnailUrl, currentRecruitment, totalRecruitment, recruitmentCount,
                recruitmentStartAt, recruitmentEndAt, projectStartAt, projectEndAt, recruitStatus, progressStatus, createdAt,
                updatedAt, techStacks, positionStatuses, 0);
    }

    public ProjectDetailResponseDto(Long id, String title, String summary, String description, String projectPurpose,
                                    String workMethod, String position, Long leaderProfileId, String leaderName,
                                    String leaderRole, String leaderAvatarUrl, String thumbnailUrl, Integer currentRecruitment,
                                    Integer totalRecruitment, Integer recruitmentCount, LocalDate recruitmentStartAt,
                                    LocalDate recruitmentEndAt, LocalDate projectStartAt, LocalDate projectEndAt,
                                    String recruitStatus, String progressStatus, LocalDateTime createdAt, LocalDateTime updatedAt,
                                    List<String> techStacks, List<ProjectRecruitPositionStatus> positionStatuses, int readCount) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.projectPurpose = projectPurpose;
        this.workMethod = workMethod;
        this.position = position;
        this.leaderProfileId = leaderProfileId;
        this.leaderName = leaderName;
        this.leaderRole = leaderRole;
        this.leaderAvatarUrl = leaderAvatarUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.recruitmentCount = recruitmentCount;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
        this.projectStartAt = projectStartAt;
        this.projectEndAt = projectEndAt;
        this.recruitStatus = recruitStatus;
        this.progressStatus = progressStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.techStacks = techStacks == null ? List.of() : techStacks;
        this.positionStatuses = positionStatuses == null ? List.of() : positionStatuses;
        this.readCount = readCount;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getProjectPurpose() { return projectPurpose; }
    public String getWorkMethod() { return workMethod; }
    public String getPosition() { return position; }
    public Long getLeaderProfileId() { return leaderProfileId; }
    public String getLeaderName() { return leaderName; }
    public String getLeaderRole() { return leaderRole; }
    public String getLeaderAvatarUrl() { return leaderAvatarUrl; }
    public Integer getCurrentRecruitment() { return currentRecruitment; }
    public Integer getTotalRecruitment() { return totalRecruitment; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public LocalDate getRecruitmentStartAt() { return recruitmentStartAt; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public LocalDate getProjectStartAt() { return projectStartAt; }
    public LocalDate getProjectEndAt() { return projectEndAt; }
    public String getRecruitStatus() { return recruitStatus; }
    public String getProgressStatus() { return progressStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDeadlineLabel() {
        if ("COMPLETED".equals(progressStatus)) return "완료";
        if ("CLOSED".equals(recruitStatus)) return "마감";
        if (recruitmentEndAt == null) return "진행 중";
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), recruitmentEndAt);
        if (days < 0) return "마감";
        if (days == 0) return "D-DAY";
        return "D-" + days;
    }

    public String getCreatedDateLabel() {
        if (createdAt == null) return "-";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    public java.util.List<String> getTechStacks() {
        return techStacks;
    }

    public List<ProjectRecruitPositionStatus> getPositionStatuses() {
        return positionStatuses;
    }

    public int getReadCount() {
        return readCount;
    }
}
