package com.example.meetball.domain.project.dto;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipatedProjectResponse {
    private Long projectId;
    private String title;
    private ProjectStatus status;
    private String userRole; // LEADER, MEMBER
    private boolean canReview;
    
    @JsonProperty("dDay")
    private Long dDay; // 마감일까지 남은 일수 (모집 중일 때만)

    public static ParticipatedProjectResponse of(Project project, String role, boolean canReview, Long dDay) {
        return ParticipatedProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .status(project.getStatus())
                .userRole(role)
                .canReview(canReview)
                .dDay(dDay)
                .build();
    }
}
