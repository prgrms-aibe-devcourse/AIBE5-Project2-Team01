package com.example.meetball.domain.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ProjectReviewListResponse {
    private List<ProjectReviewResponse> reviews;
}
