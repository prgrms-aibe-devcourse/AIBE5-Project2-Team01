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
    private String recruitStatus;
    private String progressStatus;
    private int bookmarkCount;
    private int readCount;
    private boolean bookmarked;
    private LocalDateTime createdAt;

    public ProjectListResponseDto(Long id, String title, String summary, String thumbnailUrl,
                                  Integer currentRecruitment, Integer totalRecruitment, String projectType,
                                  String progressMethod, String position, List<String> techStacks,
                                  LocalDate recruitmentEndAt, String dDay, String recruitStatus, String progressStatus,
                                  int bookmarkCount, int readCount, boolean bookmarked, LocalDateTime createdAt) {
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
        this.recruitStatus = recruitStatus;
        this.progressStatus = progressStatus;
        this.bookmarkCount = bookmarkCount;
        this.readCount = readCount;
        this.bookmarked = bookmarked;
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
    public String getRecruitStatus() { return recruitStatus; }
    public String getProgressStatus() { return progressStatus; }
    public int getBookmarkCount() { return bookmarkCount; }
    public int getReadCount() { return readCount; }
    public boolean isBookmarked() { return bookmarked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
