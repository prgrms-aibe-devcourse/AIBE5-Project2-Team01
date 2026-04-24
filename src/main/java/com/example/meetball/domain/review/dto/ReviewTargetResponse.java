package com.example.meetball.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewTargetResponse {
    private Long profileId;
    private String nickname;
    private String position;
}
