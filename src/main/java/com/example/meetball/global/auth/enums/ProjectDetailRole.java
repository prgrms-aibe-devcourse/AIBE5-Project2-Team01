package com.example.meetball.global.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 프로젝트 상세 페이지에서의 4단계 권한
 */
@Getter
@RequiredArgsConstructor
public enum ProjectDetailRole {
    LEADER("팀장"),
    MEMBER("팀원"),
    REGULAR_USER("일반회원"),
    GUEST("게스트");

    private final String description;
}
