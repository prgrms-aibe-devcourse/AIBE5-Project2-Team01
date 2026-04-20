package com.example.meetball.domain.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationRequestDto {
    private Long userId;       // 마이페이지 연동용 (우리 코드 방식)
    private String position;
    private String message;

    public ApplicationRequestDto(Long userId, String position, String message) {
        this.userId = userId;
        this.position = position;
        this.message = message;
    }
}

