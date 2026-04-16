package com.example.meetball.domain.review.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReviewRequestDto {
    private double score;
    private String reviewerNickname;
    
    // 타겟 닉네임 (특정 멤버를 지목하지 않는 팀 전체 평가면 생략 가능)
    private String targetUserNickname;
}
