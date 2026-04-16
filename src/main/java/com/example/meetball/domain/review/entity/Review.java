package com.example.meetball.domain.review.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    // 평가자 닉네임
    @Column(name = "reviewer_nickname", nullable = false)
    private String reviewerNickname;

    // 피평가자(평가를 받는 사람) 닉네임 - 프로젝트 전체일 경우 식별자 생략 가능
    @Column(name = "target_user_nickname")
    private String targetUserNickname;

    // 별점 (0.5 ~ 5.0)
    @Column(nullable = false)
    private double score;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Review(Long projectId, String reviewerNickname, String targetUserNickname, double score) {
        this.projectId = projectId;
        this.reviewerNickname = reviewerNickname;
        this.targetUserNickname = targetUserNickname;
        this.score = score;
    }
}
