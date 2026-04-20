package com.example.meetball.domain.projectread.entity;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.user.entity.User;
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
@Table(name = "project_reads", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "project_id"})
})
public class ProjectRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @LastModifiedDate
    private LocalDateTime readAt;

    @Builder
    public ProjectRead(User user, Project project) {
        this.user = user;
        this.project = project;
    }
}
