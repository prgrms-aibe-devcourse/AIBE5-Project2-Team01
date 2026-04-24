package com.example.meetball.domain.review.entity;

import com.example.meetball.domain.project.entity.Project;
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
@Table(name = "peer_review")
public class PeerReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "peer_review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // 리뷰 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_profile_id", nullable = false)
    private Profile reviewer;

    // 리뷰 대상자 (피평가자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_profile_id", nullable = false)
    private Profile reviewee;

    // 리뷰 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 별점 (0.5 ~ 5.0)
    @Column(name = "rating", nullable = false)
    private int rating;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PeerReview(Project project, Profile reviewer, Profile reviewee, String content, double score) {
        this.project = project;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.content = content;
        this.rating = Math.max(1, Math.min(5, Math.round((float) score)));
    }

    public double getScore() {
        return rating;
    }
}
