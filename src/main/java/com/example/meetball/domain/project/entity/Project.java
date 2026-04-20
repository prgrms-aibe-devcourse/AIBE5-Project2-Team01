package com.example.meetball.domain.project.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status; // RECRUITING, PROCEEDING, COMPLETED

    private String techStack; // 예: "Java, Spring, React"

    private java.time.LocalDate recruitmentDeadline; // 모집 마감일

    @Builder
    public Project(String title, String content, ProjectStatus status, String techStack, java.time.LocalDate recruitmentDeadline) {
        this.title = title;
        this.content = content;
        this.status = status;
        this.techStack = techStack;
        this.recruitmentDeadline = recruitmentDeadline;
    }
}
