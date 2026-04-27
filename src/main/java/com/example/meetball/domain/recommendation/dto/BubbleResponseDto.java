package com.example.meetball.domain.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 추천 질문의 선택지(버블) 정보를 담는 DTO.
 * label: 화면에 표시될 문구 (예: "스타트업에서 실전 경험")
 * value: 내부 매칭용 키워드 (예: "스타트업")
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BubbleResponseDto {
    private String label;
    private String value;
}
