package com.example.meetball.domain.project.dto;

public record ProjectMemberProfile(
        Long userId,
        String nickname,
        String role,
        String jobTitle
) {
}
