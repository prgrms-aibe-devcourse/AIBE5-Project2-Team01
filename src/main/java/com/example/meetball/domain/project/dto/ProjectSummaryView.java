package com.example.meetball.domain.project.dto;

import java.util.List;

public record ProjectSummaryView(
        Long id,
        String title,
        String summary,
        String projectType,
        String position,
        String leaderName,
        String leaderAvatarUrl,
        String thumbnailUrl,
        int currentRecruitment,
        int totalRecruitment,
        String deadlineLabel,
        List<String> techStacks
) {
}
