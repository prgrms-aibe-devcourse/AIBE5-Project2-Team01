package com.example.meetball.domain.viewhistory.entity;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "view_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "project_id"})
})
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "view_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @LastModifiedDate
    @Column(name = "viewed_at")
    private LocalDateTime readAt;

    @Builder
    public ViewHistory(Profile profile, Project project) {
        this.profile = profile;
        this.project = project;
    }
}
