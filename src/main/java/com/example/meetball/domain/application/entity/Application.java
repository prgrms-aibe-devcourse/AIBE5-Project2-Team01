package com.example.meetball.domain.application.entity;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.user.entity.User;
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
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // HEAD
    @Column(name = "project_id", insertable = false, updatable = false)
    private Long projectId;

    @Column(name = "applicant_name")
    private String applicantName;
    // 마이페이지 연동: User 엔티티 직접 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    // 프로젝트 엔티티 직접 참조
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    private String position;
    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;


    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Unified Constructor
    @Builder
    public Application(Project project, User user, String applicantName, String position, ApplicationStatus status, String message) {
        this.project = project;
        if (project != null) {
            this.projectId = project.getId();
        }
        this.user = user;
        this.applicantName = applicantName;
        this.position = position;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(ApplicationStatus status, LocalDateTime updatedAt) {
        this.status = status;
        this.updatedAt = updatedAt;
    }
}
