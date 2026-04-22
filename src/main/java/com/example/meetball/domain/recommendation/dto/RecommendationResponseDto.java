package com.example.meetball.domain.recommendation.dto;

import java.util.List;

/**
 * 추천 프로젝트 응답 DTO
 * projectId, 제목, 점수, 추천 이유 목록을 포함합니다.
 */
public class RecommendationResponseDto {

    private Long projectId;
    private String title;
    private int score;
    private List<String> reasons;

    public RecommendationResponseDto(Long projectId, String title, int score, List<String> reasons) {
        this.projectId = projectId;
        this.title = title;
        this.score = score;
        this.reasons = reasons;
    }

    public Long getProjectId() { return projectId; }
    public String getTitle() { return title; }
    public int getScore() { return score; }
    public List<String> getReasons() { return reasons; }
}
