package com.example.meetball.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewTargetResponse {
    private Long userId;
    private String nickname;
    private String jobTitle;
}
