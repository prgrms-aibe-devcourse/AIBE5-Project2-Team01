package com.example.meetball.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class ReviewSummaryDto {
    private double averageScore;
    private int totalReviews;
    // 5점, 4점 등에 대한 분포 (예: 5점 -> 80%) 매핑
    private Map<Integer, Integer> scorePercentages;
}
