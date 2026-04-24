package com.example.meetball.domain.project.dto;

public record ProjectRecruitPositionStatus(
        String name,
        int current,
        int capacity,
        boolean full
) {
}
