package com.example.meetball.domain.project.dto;

import java.util.List;

public record ProjectDetailView(
        Long id,
        String title,
        String summary,
        String description,
        String projectType,
        String progressMethod,
        String position,
        Long leaderUserId,
        String leaderName,
        String leaderRole,
        String leaderAvatarUrl,
        String thumbnailUrl,
        int currentRecruitment,
        int totalRecruitment,
        int progressPercent,
        String deadlineLabel,
        String createdDateLabel,
        String recruitmentPeriodLabel,
        String projectPeriodLabel,
        List<String> techStacks,
        List<ProjectPositionStatus> positionStatuses,
        List<ProjectMemberProfile> teamMembers,
        int readCount
) {
}
