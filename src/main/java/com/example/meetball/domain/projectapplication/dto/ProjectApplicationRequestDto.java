package com.example.meetball.domain.projectapplication.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProjectApplicationRequestDto {
    private Long profileId;
    private String applicantName;
    private String position;
    private String message;

    public ProjectApplicationRequestDto(Long profileId, String applicantName, String position, String message) {
        this.profileId = profileId;
        this.applicantName = applicantName;
        this.position = position;
        this.message = message;
    }
}
