package com.example.meetball.domain.project.entity;

import com.example.meetball.domain.profile.entity.Profile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "project_participant",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "profile_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruit_position_id")
    private ProjectRecruitPosition recruitPosition;

    @Column(name = "participant_role", nullable = false, length = 20)
    private String role;

    @Column(name = "participation_status", nullable = false, length = 20)
    private String participationStatus = "ACTIVE";

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Builder
    public ProjectParticipant(Profile profile, Project project, ProjectRecruitPosition recruitPosition, String role) {
        this.profile = profile;
        this.project = project;
        this.recruitPosition = recruitPosition;
        this.role = role;
        this.participationStatus = "ACTIVE";
        this.joinedAt = LocalDateTime.now();
    }

    public ProjectParticipant(Profile profile, Project project, String role) {
        this(profile, project, null, role);
    }
}
