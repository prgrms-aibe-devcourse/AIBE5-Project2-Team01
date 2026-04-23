package com.example.meetball.domain.bookmark.dto;

import com.example.meetball.domain.bookmark.entity.Bookmark;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectTechStack;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookmarkedProjectResponse {
    private Long projectId;
    private String title;
    private String techStack;

    public static BookmarkedProjectResponse from(Bookmark bookmark) {
        Project project = bookmark.getProject();
        return BookmarkedProjectResponse.builder()
                .projectId(project.getId())
                .title(project.getTitle())
                .techStack(formatTechStacks(project))
                .build();
    }

    private static String formatTechStacks(Project project) {
        if (project.getTechStackSelections() != null && !project.getTechStackSelections().isEmpty()) {
            return project.getTechStackSelections().stream()
                    .map(ProjectTechStack::getTechStackName)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
        }
        return ProjectSelectionCatalog.normalizeTechStackCsvOrDefault(project.getTechStackCsv());
    }
}
