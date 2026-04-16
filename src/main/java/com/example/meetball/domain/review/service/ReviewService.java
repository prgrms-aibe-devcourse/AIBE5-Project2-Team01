package com.example.meetball.domain.review.service;

import com.example.meetball.domain.review.dto.ReviewRequestDto;
import com.example.meetball.domain.review.dto.ReviewSummaryDto;
import com.example.meetball.domain.review.entity.Review;
import com.example.meetball.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public ReviewSummaryDto getProjectReviewSummary(Long projectId) {
        List<Review> reviews = reviewRepository.findByProjectId(projectId);
        
        if (reviews.isEmpty()) {
            return new ReviewSummaryDto(0.0, 0, new HashMap<>());
        }

        double totalScore = 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) counts.put(i, 0);

        for (Review r : reviews) {
            totalScore += r.getScore();
            // 통계 막대그래프용 분포는 정수(반올림)로 그룹화
            int roundedScore = (int) Math.round(r.getScore());
            if (roundedScore < 1) roundedScore = 1;
            counts.put(roundedScore, counts.get(roundedScore) + 1);
        }

        double average = Math.round((totalScore / reviews.size()) * 10.0) / 10.0;
        
        Map<Integer, Integer> percentages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = counts.get(i);
            int percentage = (int) Math.round((double) count / reviews.size() * 100);
            percentages.put(i, percentage);
        }

        return new ReviewSummaryDto(average, reviews.size(), percentages);
    }

    // 신규 리뷰(별점) 등록 로직
    @Transactional
    public void addReview(Long projectId, ReviewRequestDto requestDto) {
        if (requestDto.getScore() < 0.5 || requestDto.getScore() > 5.0) {
            throw new IllegalArgumentException("별점은 0.5점에서 5.0점 사이여야 합니다.");
        }

        Review review = Review.builder()
                .projectId(projectId)
                .reviewerNickname(requestDto.getReviewerNickname())
                .targetUserNickname(requestDto.getTargetUserNickname() != null ? requestDto.getTargetUserNickname() : "팀전체")
                .score(requestDto.getScore())
                .build();
        
        reviewRepository.save(review);
    }
}
