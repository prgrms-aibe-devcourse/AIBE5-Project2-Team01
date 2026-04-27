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
    private String projectPurpose;
    private String workMethod;
    private String position;
    private List<String> techStacks;
    private LocalDate recruitmentEndAt;
    private String dDay;
    private String recruitStatus;
    private String progressStatus;
    private int bookmarkCount;
    private int readCount;
    private int commentCount;
    private boolean bookmarked;
    private LocalDateTime createdAt;

    public ProjectListResponseDto(Long id, String title, String summary, String thumbnailUrl,
                                  Integer currentRecruitment, Integer totalRecruitment, String projectPurpose,
                                  String workMethod, String position, List<String> techStacks,
                                  LocalDate recruitmentEndAt, String dDay, String recruitStatus, String progressStatus,
                                  int bookmarkCount, int readCount, int commentCount, boolean bookmarked, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.projectPurpose = projectPurpose;
        this.workMethod = workMethod;
        this.position = position;
        this.techStacks = techStacks;
        this.recruitmentEndAt = recruitmentEndAt;
        this.dDay = dDay;
        this.recruitStatus = recruitStatus;
        this.progressStatus = progressStatus;
        this.bookmarkCount = bookmarkCount;
        this.readCount = readCount;
        this.commentCount = commentCount;
        this.bookmarked = bookmarked;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getCurrentRecruitment() { return currentRecruitment; }
    public Integer getTotalRecruitment() { return totalRecruitment; }
    public String getProjectPurpose() { return projectPurpose; }
    public String getWorkMethod() { return workMethod; }
    public String getPosition() { return position; }
    public List<String> getTechStacks() { return techStacks; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public String getDDay() { return dDay; }
    public String getDeadlineLabel() { return dDay; }
    public String getRecruitStatus() { return recruitStatus; }
    public String getProgressStatus() { return progressStatus; }
    public int getBookmarkCount() { return bookmarkCount; }
    public int getReadCount() { return readCount; }
    public int getCommentCount() { return commentCount; }
    public boolean isBookmarked() { return bookmarked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
