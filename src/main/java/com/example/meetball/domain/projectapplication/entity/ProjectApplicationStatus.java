package com.example.meetball.domain.projectapplication.entity;

public enum ProjectApplicationStatus {
    PENDING,  // 검토 중
    APPROVED, // 과거 데이터 호환용. 새 응답/요청에서는 ACCEPTED로 정규화합니다.
    ACCEPTED, // 수락됨
    REJECTED,  // 거절됨
    WITHDRAWN, // 지원자가 철회함
    REMOVED; // 팀장이 지원서/팀원을 제거함

    public boolean isAccepted() {
        return this == ACCEPTED || this == APPROVED;
    }

    public boolean blocksPositionRemoval() {
        return this == PENDING || isAccepted();
    }

    public boolean isHiddenFromManagement() {
        return this == WITHDRAWN || this == REMOVED;
    }
}
