package com.example.meetball.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 1000)
    private String summary;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 40)
    private String projectType;

    @Column(nullable = false, length = 40)
    private String position;

    @Column(nullable = false, length = 60)
    private String leaderName;

    @Column(nullable = false, length = 60)
    private String leaderRole;

    @Column(nullable = false)
    private String leaderAvatarUrl;

    @Column(nullable = false)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Integer currentRecruitment;

    @Column(nullable = false)
    private Integer totalRecruitment;

    @Column(nullable = false)
    private LocalDate recruitmentDeadline;

    @Column(nullable = false)
    private LocalDate createdDate;

    @Column(nullable = false, length = 500)
    private String techStackCsv;

    protected Project() {
    }

    public Project(
            String title,
            String summary,
            String description,
            String projectType,
            String position,
            String leaderName,
            String leaderRole,
            String leaderAvatarUrl,
            String thumbnailUrl,
            Integer currentRecruitment,
            Integer totalRecruitment,
            LocalDate recruitmentDeadline,
            LocalDate createdDate,
            String techStackCsv
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.projectType = projectType;
        this.position = position;
        this.leaderName = leaderName;
        this.leaderRole = leaderRole;
        this.leaderAvatarUrl = leaderAvatarUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.recruitmentDeadline = recruitmentDeadline;
        this.createdDate = createdDate;
        this.techStackCsv = techStackCsv;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getDescription() {
        return description;
    }

    public String getProjectType() {
        return projectType;
    }

    public String getPosition() {
        return position;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public String getLeaderRole() {
        return leaderRole;
    }

    public String getLeaderAvatarUrl() {
        return leaderAvatarUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public Integer getCurrentRecruitment() {
        return currentRecruitment;
    }

    public Integer getTotalRecruitment() {
        return totalRecruitment;
    }

    public LocalDate getRecruitmentDeadline() {
        return recruitmentDeadline;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public String getTechStackCsv() {
        return techStackCsv;
    }
}
