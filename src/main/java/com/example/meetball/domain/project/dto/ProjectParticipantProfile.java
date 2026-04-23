package com.example.meetball.domain.project.dto;

public record ProjectParticipantProfile(
        Long profileId,
        String nickname,
        String role,
        String jobTitle
) {
}
