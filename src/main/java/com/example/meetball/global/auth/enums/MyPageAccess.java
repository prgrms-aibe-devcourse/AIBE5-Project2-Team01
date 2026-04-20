package com.example.meetball.global.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 마이페이지 / 프로필에서의 2단계 권한
 */
@Getter
@RequiredArgsConstructor
public enum MyPageAccess {
    OWNER("본인"),
    VISITOR("타인");

    private final String description;
}
