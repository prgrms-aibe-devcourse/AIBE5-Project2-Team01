package com.example.meetball.domain.project.dto;

public record ProjectPositionStatus(
        String name,
        int current,
        int capacity,
        boolean full
) {
}
