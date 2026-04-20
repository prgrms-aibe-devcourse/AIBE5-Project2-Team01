package com.example.meetball.domain.review.dto;

import com.example.meetball.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserReviewResponse {
    private Long reviewId;
    private String reviewerNickname;
    private String reviewerJobTitle;
    private String projectTitle;
    private String content;
    private double score;
    private LocalDateTime createdAt;

    public static UserReviewResponse from(Review review) {
        return UserReviewResponse.builder()
                .reviewId(review.getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .reviewerJobTitle(review.getReviewer().getJobTitle())
                .projectTitle(review.getProject().getTitle())
                .content(review.getContent())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
