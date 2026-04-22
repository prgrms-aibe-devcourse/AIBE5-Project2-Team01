package com.example.meetball.domain.review.controller;

import com.example.meetball.domain.review.dto.ReviewRequestDto;
import com.example.meetball.domain.review.dto.ReviewSummaryDto;
import com.example.meetball.domain.review.dto.ReviewTargetResponse;
import com.example.meetball.domain.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/summary")
    public ResponseEntity<ReviewSummaryDto> getReviewSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(reviewService.getProjectReviewSummary(projectId));
    }

    // 리뷰 대상 팀원 목록 조회
    @GetMapping("/teammates")
    public ResponseEntity<List<ReviewTargetResponse>> getTeammates(
            @PathVariable Long projectId,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        return ResponseEntity.ok(reviewService.getTeammatesForReview(projectId, requireSessionUser(userId)));
    }

    // 신규 별점 등록
    @PostMapping
    public ResponseEntity<Void> addReview(
            @PathVariable Long projectId,
            @Valid @RequestBody ReviewRequestDto requestDto,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        reviewService.addReview(projectId, requireSessionUser(userId), requestDto);
        return ResponseEntity.status(201).build();
    }

    private Long requireSessionUser(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return userId;
    }
}
