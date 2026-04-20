package com.example.meetball.domain.review.entity;

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
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // 리뷰 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    // 리뷰 대상자 (피평가자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id")
    private User reviewee;

    // 리뷰 내용
    @Column(columnDefinition = "TEXT")
    private String content;

    // 별점 (0.5 ~ 5.0)
    @Column(nullable = false)
    private double score;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Review(Project project, User reviewer, User reviewee, String content, double score) {
        this.project = project;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.content = content;
        this.score = score;
    }
}
