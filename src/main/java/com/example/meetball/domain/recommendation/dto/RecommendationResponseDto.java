package com.example.meetball.domain.recommendation.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 추천 프로젝트 단일 카드 응답 DTO.
 *
 * 현재 브랜치의 프로젝트 구조를 유지하면서도
 * feat/ai-login-update-clone 프런트가 기대하는 필드를 최대한 맞춥니다.
 */
@Getter
@Builder
public class RecommendationResponseDto {

    private final Long projectId;
    private final String title;
    private final String summary;
    private final int score;
    private final List<String> reasons;
    private final String recommendationReason;
    private final String axisValue;
    private final String progressMethod;
    private final String projectType;
    private final String positionSummary;
}
