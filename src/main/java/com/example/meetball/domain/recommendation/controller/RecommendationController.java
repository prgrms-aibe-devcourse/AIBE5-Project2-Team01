package com.example.meetball.domain.recommendation.controller;

import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.service.RecommendationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 * AI 추천 프로젝트 컨트롤러
 *
 * MVP 단계: userId 쿼리 파라미터로 직접 지정
 * 세션 확장: 세션에 userId가 있으면 우선 사용하고, 없으면 파라미터 fallback
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
     * @param userId  쿼리 파라미터로 전달되는 사용자 ID (로그인 전 테스트용)
     * @param session HTTP 세션 (세션 기반 인증 확장 시 사용)
     * @return 점수 내림차순 추천 프로젝트 목록
     *
     * 사용 예:
     *   GET /api/recommendations?userId=1          (로컬 테스트용)
     *   GET /api/recommendations                   (로그인 세션 기반)
     */
    @GetMapping
    public ResponseEntity<List<RecommendationResponseDto>> getRecommendations(
            @RequestParam(name = "userId", required = false) Long userId,
            HttpSession session) {

        // 세션에서 userId 우선 조회 (세션 기반 인증과 파라미터 방식 모두 지원)
        Long resolvedUserId = (Long) session.getAttribute("userId");
        if (resolvedUserId == null) {
            resolvedUserId = userId;
        }

        // userId를 확인할 수 없는 경우 401 반환
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
