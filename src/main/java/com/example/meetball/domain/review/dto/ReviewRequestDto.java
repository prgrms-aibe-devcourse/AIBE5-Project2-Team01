package com.example.meetball.domain.review.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {
    private double score;

    // 타겟 닉네임 (특정 멤버를 지목하지 않는 팀 전체 평가면 생략 가능)
    private String targetProfileNickname;

    @Size(max = 100, message = "리뷰 내용은 100자 이내로 입력해주세요.")
    private String content;
}
