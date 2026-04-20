package com.example.meetball.domain.projectread.dto;

import com.example.meetball.domain.projectread.entity.ProjectRead;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReadProjectResponse {
    private Long projectId;
    private String title;
    private String techStack;
    private LocalDateTime readAt;

    public static ReadProjectResponse from(ProjectRead history) {
        return ReadProjectResponse.builder()
                .projectId(history.getProject().getId())
                .title(history.getProject().getTitle())
                .techStack(history.getProject().getTechStack())
                .readAt(history.getReadAt())
                .build();
    }
}
