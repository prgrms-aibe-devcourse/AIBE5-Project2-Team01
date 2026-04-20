package com.example.meetball.global.auth.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.global.auth.enums.MyPageAccess;
import com.example.meetball.global.auth.enums.ProjectDetailRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthorizationService {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 프로젝트 상세 페이지에서의 권한(4단계)을 판별합니다.
     */
    public ProjectDetailRole getProjectDetailRole(User currentUser, Project project) {
        // 1. 게스트 확인 (로그인 하지 않은 경우)
        if (currentUser == null) {
            return ProjectDetailRole.GUEST;
        }

        // 2. 프로젝트 멤버 여부 확인
        Optional<ProjectMember> projectMember = projectMemberRepository.findByProjectAndUser(project, currentUser);

        if (projectMember.isEmpty()) {
            return ProjectDetailRole.REGULAR_USER; // 로그인 했으나 멤버 아님
        }

        // 3. 팀장 vs 팀원 구분
        String role = projectMember.get().getRole();
        if ("LEADER".equals(role)) {
            return ProjectDetailRole.LEADER;
        }

        return ProjectDetailRole.MEMBER;
    }

    /**
     * 마이페이지 / 프로필의 접근 권한(2단계)을 판별합니다.
     */
    public MyPageAccess getMyPageAccess(User currentUser, User profileUser) {
        // 로그인하지 않았거나, 로그인한 ID와 프로필 ID가 다르면 타인(VISITOR)
        if (currentUser == null || !currentUser.getId().equals(profileUser.getId())) {
            return MyPageAccess.VISITOR;
        }

        return MyPageAccess.OWNER;
    }
}
