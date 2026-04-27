package com.example.meetball.domain.recommendation.strategy;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import java.util.List;

/**
 * 현재 프로필과 추천 후보 프로젝트 목록을 바탕으로 규칙 기반 후보군을 생성합니다.
 */
public interface RecommendationStrategy {

    List<RecommendationResponseDto> generateCandidatePool(Profile profile, List<Project> availableProjects);
}
