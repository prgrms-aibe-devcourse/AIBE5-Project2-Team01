package com.example.meetball.domain.project.dto;

import java.util.List;

public record ProjectDetailView(
        Long id,
        String title,
        String summary,
        String description,
        String projectPurpose,
        String workMethod,
        String position,
        Long leaderProfileId,
        String leaderName,
        String leaderRole,
        String leaderPosition,
        String leaderAvatarUrl,
        String thumbnailUrl,
        int currentRecruitment,
        int totalRecruitment,
        int progressPercent,
        String deadlineLabel,
        String recruitStatus,
        String progressStatus,
        String createdDateLabel,
        String recruitmentPeriodLabel,
        String projectPeriodLabel,
        List<String> techStacks,
        List<ProjectRecruitPositionStatus> positionStatuses,
        List<ProjectParticipantProfile> teamMembers,
        int readCount
) {
}
