package com.example.meetball.domain.recommendation.controller;

import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.service.RecommendationService;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 세션 기반 현재 프로필 맞춤 추천 API.
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    public ResponseEntity<RecommendationListResponseDto> getRecommendations(
            HttpSession session,
            @RequestParam(required = false) List<Long> excludeIds,
            @RequestParam(required = false) List<String> recentAxes) {

        Long resolvedProfileId = (Long) session.getAttribute("profileId");
        if (resolvedProfileId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            RecommendationListResponseDto recommendations =
                    recommendationService.recommend(resolvedProfileId, excludeIds, recentAxes);
            return ResponseEntity.ok(recommendations);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}
