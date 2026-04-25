package com.example.meetball.domain.project.dto;

public record ProjectPositionEditConstraint(
        String name,
        int appliedCount,
        int acceptedCount,
        int capacity,
        int minimumCapacity,
        boolean full,
        boolean nameLocked,
        boolean capacityLocked,
        boolean deleteLocked,
        String message
) {
}
