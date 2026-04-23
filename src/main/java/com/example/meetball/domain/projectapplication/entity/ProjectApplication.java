package com.example.meetball.domain.projectapplication.entity;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import com.example.meetball.domain.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "project_application")
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long id;

    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @Transient
    private String applicantName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_position_id", nullable = false)
    private ProjectRecruitPosition recruitPosition;

    @Transient
    private String position;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, columnDefinition = "varchar(20)")
    private ProjectApplicationStatus status;


    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Unified Constructor
    @Builder
    public ProjectApplication(Project project, Profile profile, String applicantName, String position, ProjectRecruitPosition recruitPosition, ProjectApplicationStatus status, String message) {
        this.project = project;
        if (project != null) {
            this.projectId = project.getId();
        }
        this.profile = profile;
        this.applicantName = applicantName;
        this.recruitPosition = recruitPosition;
        this.position = position;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ProjectApplicationStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }

    public void updatePosition(String position) {
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateRecruitPosition(ProjectRecruitPosition recruitPosition) {
        this.recruitPosition = recruitPosition;
        this.position = recruitPosition != null ? recruitPosition.getPositionName() : this.position;
        this.updatedAt = LocalDateTime.now();
    }

    public String getApplicantName() {
        if (applicantName != null && !applicantName.isBlank()) {
            return applicantName;
        }
        return profile != null ? profile.getNickname() : "";
    }

    public String getPosition() {
        if (recruitPosition != null) {
            return recruitPosition.getPositionName();
        }
        return position;
    }
}
