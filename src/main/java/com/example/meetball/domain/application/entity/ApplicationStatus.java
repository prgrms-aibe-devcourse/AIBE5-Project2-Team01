package com.example.meetball.domain.application.entity;

public enum ApplicationStatus {
    PENDING,  // 검토 중
    APPROVED, // 승인됨 (추가됨)
    ACCEPTED, // 수락됨
    REJECTED  // 거절됨
}
