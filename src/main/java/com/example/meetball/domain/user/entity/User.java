package com.example.meetball.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email; // 구글 로그인 시 받아오는 이메일

    @Column(unique = true)
    private String nickname; // 사용자가 직접 설정하는 닉네임

    private String jobTitle; // 직무 (예: 백엔드 개발자)

    private String techStack; // 기술 스택 (예: Java, Spring Boot)

    @Column(nullable = false)
    private boolean isPublic = true; // 프로필 공개 여부 기본값 true

    @Column(nullable = false)
    private String role; // GUEST, MEMBER, LEADER

    @Builder
    public User(String email, String nickname, String jobTitle, String techStack, boolean isPublic, String role) {
        this.email = email;
        this.nickname = nickname;
        this.jobTitle = jobTitle;
        this.techStack = techStack;
        this.isPublic = isPublic;
        this.role = role;
    }

    // 프로필 업데이트 비즈니스 로직
    public void updateProfile(String nickname, String jobTitle, String techStack, boolean isPublic) {
        this.nickname = nickname;
        this.jobTitle = jobTitle;
        this.techStack = techStack;
        this.isPublic = isPublic;
    }

    // 권한 승급 로직 등
    public void changeRole(String role) {
        this.role = role;
    }
}
