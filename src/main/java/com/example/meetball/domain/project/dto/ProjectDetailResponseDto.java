package com.example.meetball.domain.project.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDetailResponseDto {

    private Long id;
    private String title;
    private String description;
    private String projectType;
    private String progressMethod;
    private Integer recruitmentCount;
    private LocalDate recruitmentStartAt;
    private LocalDate recruitmentEndAt;
    private LocalDate projectStartAt;
    private LocalDate projectEndAt;
    private Boolean closed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProjectDetailResponseDto(Long id, String title, String description, String projectType,
                                    String progressMethod, Integer recruitmentCount, LocalDate recruitmentStartAt,
                                    LocalDate recruitmentEndAt, LocalDate projectStartAt, LocalDate projectEndAt,
                                    Boolean closed, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
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

    // --- Helper methods for Thymeleaf ---
    public String getThumbnailUrl() {
        return "https://picsum.photos/seed/" + (id != null ? id : "default") + "/1200/600";
    }

    public String getDeadlineLabel() {
        if (closed != null && closed) return "마감됨";
        if (recruitmentEndAt == null) return "진행 중";
        long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), recruitmentEndAt);
        if (days < 0) return "마감됨";
        if (days == 0) return "D-DAY";
        return "D-" + days;
    }

    public String getCreatedDateLabel() {
        if (createdAt == null) return "-";
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    }

    public java.util.List<String> getTechStacks() {
        // 임시 더미 혹은 기본값 반환. 원래는 Project 엔티티에서 가져와야 함.
        return java.util.Arrays.asList("Java", "Spring", "MySQL");
    }
}
