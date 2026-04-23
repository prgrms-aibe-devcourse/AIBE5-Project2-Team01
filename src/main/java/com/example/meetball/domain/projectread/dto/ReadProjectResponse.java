package com.example.meetball.domain.projectread.dto;

import com.example.meetball.domain.projectread.entity.ProjectRead;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectTechStack;
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
        Project project = history.getProject();
        return ReadProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .techStack(formatTechStacks(project))
                .readAt(history.getReadAt())
                .build();
    }

    private static String formatTechStacks(Project project) {
        if (project.getTechStackSelections() != null && !project.getTechStackSelections().isEmpty()) {
            return project.getTechStackSelections().stream()
                    .map(ProjectTechStack::getTechStackName)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return "";
    }
}
