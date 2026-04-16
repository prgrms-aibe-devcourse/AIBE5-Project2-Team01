package com.example.meetball.domain.review.controller;

import com.example.meetball.domain.review.dto.ReviewRequestDto;
import com.example.meetball.domain.review.dto.ReviewSummaryDto;
import com.example.meetball.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/summary")
    public ResponseEntity<ReviewSummaryDto> getReviewSummary(@PathVariable Long projectId) {
        return ResponseEntity.ok(reviewService.getProjectReviewSummary(projectId));
    }

    // 신규 별점 등록
    @PostMapping
    public ResponseEntity<Void> addReview(
            @PathVariable Long projectId,
            @RequestBody ReviewRequestDto requestDto) {
        reviewService.addReview(projectId, requestDto);
        return ResponseEntity.status(201).build();
    }
}
