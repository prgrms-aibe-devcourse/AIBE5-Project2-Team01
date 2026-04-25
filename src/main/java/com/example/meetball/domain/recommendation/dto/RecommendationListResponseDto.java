package com.example.meetball.domain.recommendation.dto;

import java.util.List;

/**
 * 추천 카드 목록과 비교 축/버블/질문을 함께 전달하는 응답 DTO.
 */
public class RecommendationListResponseDto {

    private final List<RecommendationResponseDto> recommendations;
    private final String axis;
    private final List<String> bubbles;
    private final String question;

    public RecommendationListResponseDto(List<RecommendationResponseDto> recommendations, String axis, List<String> bubbles) {
        this(recommendations, axis, bubbles, null);
    }

    public RecommendationListResponseDto(List<RecommendationResponseDto> recommendations, String axis, List<String> bubbles, String question) {
        this.recommendations = recommendations;
        this.axis = axis;
        this.bubbles = bubbles;
        this.question = question;
    }

    public List<RecommendationResponseDto> getRecommendations() {
        return recommendations;
    }

    public String getAxis() {
        return axis;
    }

    public List<String> getBubbles() {
        return bubbles;
    }

    public String getQuestion() {
        return question;
    }
}
