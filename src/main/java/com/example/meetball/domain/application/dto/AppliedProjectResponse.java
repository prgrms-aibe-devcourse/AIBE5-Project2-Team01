package com.example.meetball.domain.application.dto;

import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AppliedProjectResponse {
    private Long applicationId;
    private Long projectId;
    private String projectTitle;
    private String position;
    private ApplicationStatus status; // PENDING, ACCEPTED, REJECTED
    private LocalDateTime appliedAt;

    public static AppliedProjectResponse from(Application application) {
        return AppliedProjectResponse.builder()
                .applicationId(application.getId())
                .projectId(application.getProject().getId())
                .projectTitle(application.getProject().getTitle())
                .position(application.getPosition())
                .status(application.getStatus())
                .appliedAt(application.getCreatedAt())
                .build();
    }
}
