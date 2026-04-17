package com.example.meetball.domain.project.dto;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectStatus;
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

    public static ParticipatedProjectResponse of(Project project, String role) {
        return ParticipatedProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .status(project.getStatus())
                .userRole(role)
                .canReview(project.getStatus() == ProjectStatus.COMPLETED)
                .build();
    }
}
