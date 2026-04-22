package com.example.meetball.domain.recommendation.controller;

import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * AI 추천 프로젝트 컨트롤러
 *
 * 세션 기반으로 현재 사용자에게 맞는 추천 프로젝트를 제공합니다.
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * 특정 사용자 기준 추천 프로젝트 목록 조회
     *
     * @param session HTTP 세션 (세션 기반 인증 확장 시 사용)
     * @return 점수 내림차순 추천 프로젝트 목록
     */
    @GetMapping
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendations(
            HttpSession session) {

        Long resolvedUserId = (Long) session.getAttribute("userId");

        if (resolvedUserId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<RecommendationResponseDto> recommendations = recommendationService.recommend(resolvedUserId);
            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException e) {
            // 사용자를 찾을 수 없는 경우 404 반환
            return ResponseEntity.notFound().build();
        }
    }
}
