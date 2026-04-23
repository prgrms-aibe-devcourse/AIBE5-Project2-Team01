package com.example.meetball.domain.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

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

    @Column(length = 1000)
    private String techStack; // 지정 기술 스택 CSV (예: Java, Spring)

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<UserTechStack> techStackSelections = new ArrayList<>();

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

    public void replaceTechStacks(List<String> techStacks) {
        List<String> requestedTechStacks = normalizeTechStackNames(techStacks);
        this.techStackSelections.removeIf(existing -> !requestedTechStacks.contains(existing.getTechStackName()));

        for (int i = 0; i < requestedTechStacks.size(); i++) {
            String techStackName = requestedTechStacks.get(i);
            UserTechStack existing = this.techStackSelections.stream()
                    .filter(current -> techStackName.equals(current.getTechStackName()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                this.techStackSelections.add(new UserTechStack(this, techStackName, i));
            } else {
                existing.updateSortOrder(i);
            }
        }
    }

    private List<String> normalizeTechStackNames(List<String> techStacks) {
        if (techStacks == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(techStacks.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList()));
    }

    // 권한 승급 로직 등
    public void changeRole(String role) {
        this.role = role;
    }
}
