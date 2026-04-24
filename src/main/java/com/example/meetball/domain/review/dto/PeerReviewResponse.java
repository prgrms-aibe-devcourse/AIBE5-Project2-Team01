package com.example.meetball.domain.review.dto;

import com.example.meetball.domain.review.entity.PeerReview;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PeerReviewResponse {
    private Long reviewId;
    private String reviewerNickname;
    private String reviewerPosition;
    private String projectTitle;
    private String content;
    private double score;
    private LocalDateTime createdAt;

    public static PeerReviewResponse from(PeerReview review) {
        return PeerReviewResponse.builder()
                .reviewId(review.getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .reviewerPosition(review.getReviewer().getPosition())
                .projectTitle(review.getProject().getTitle())
                .content(review.getContent())
                .score(review.getScore())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
