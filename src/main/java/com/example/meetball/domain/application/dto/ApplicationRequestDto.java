package com.example.meetball.domain.application.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApplicationRequestDto {
    private Long userId;
    private String applicantName;
    private String position;
    private String message;

    public ApplicationRequestDto(Long userId, String applicantName, String position, String message) {
        this.userId = userId;
        this.applicantName = applicantName;
        this.position = position;
        this.message = message;
    }
}
