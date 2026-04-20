package com.example.meetball.domain.project.dto;

import com.example.meetball.domain.project.entity.Project;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ParticipatedProjectResponse {
    private Long projectId;
    private String title;
    private String userRole; // LEADER, MEMBER
    private boolean canReview;
    private boolean closed; // 프로젝트 마감 여부
    
    @JsonProperty("dDay")
    private Long dDay; // 마감일까지 남은 일수 (모집 중일 때만)

    public static ParticipatedProjectResponse of(Project project, String role, boolean canReview, Long dDay) {
        return ParticipatedProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .userRole(role)
                .canReview(canReview)
                .dDay(dDay)
                .build();
    }
}
