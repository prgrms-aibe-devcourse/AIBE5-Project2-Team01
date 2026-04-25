package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import java.util.List;

/**
 * 규칙 기반 후보군을 LLM이 해석하여 축/질문/카드 문구를 보강합니다.
 */
public interface LlmRecommendationInterpreter {

    RecommendationListResponseDto interpret(
            Profile profile,
            List<RecommendationResponseDto> candidatePool,
            List<String> recentAxes
    );
}
