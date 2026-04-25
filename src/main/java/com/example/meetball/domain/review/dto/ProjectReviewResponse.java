package com.example.meetball.domain.review.dto;

import com.example.meetball.domain.review.entity.ProjectReview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectReviewResponse {
    private Long id;
    private String reviewerNickname;
    private String reviewerPosition;
    private String content;
    private double score;
    private LocalDateTime createdAt;

    public static ProjectReviewResponse from(ProjectReview review) {
        return ProjectReviewResponse.builder()
                .id(review.getId())
                .reviewerNickname(review.getReviewer() != null ? review.getReviewer().getNickname() : "익명")
                .reviewerPosition(review.getReviewer() != null ? review.getReviewer().getPosition() : null)
                .content(review.getContent())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
