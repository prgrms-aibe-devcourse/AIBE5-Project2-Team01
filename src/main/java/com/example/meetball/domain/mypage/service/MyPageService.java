package com.example.meetball.domain.mypage.service;

import com.example.meetball.domain.application.dto.AppliedProjectResponse;
import com.example.meetball.domain.application.service.ApplicationService;
import com.example.meetball.domain.bookmark.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.bookmark.service.BookmarkService;
import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.entity.ProjectStatus;
import com.example.meetball.domain.project.service.ProjectService;
import com.example.meetball.domain.projectread.dto.ReadProjectResponse;
import com.example.meetball.domain.projectread.service.ProjectReadService;
import com.example.meetball.domain.review.dto.UserReviewResponse;
import com.example.meetball.domain.review.service.ReviewService;
import com.example.meetball.domain.user.dto.UserProfileUpdateRequest;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
import com.example.meetball.global.auth.enums.MyPageAccess;
import com.example.meetball.global.auth.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserService userService;
    private final ProjectService projectService;
    private final ApplicationService applicationService;
    private final ProjectReadService projectReadService;
    private final BookmarkService bookmarkService;
    private final ReviewService reviewService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public MyPageProfileResponse getMyProfile(Long userId, Long viewerId) {
        User user = userService.getUserById(userId);
        User viewer = viewerId != null ? userService.getUserById(viewerId) : null;
        
        // 권한 체크
        MyPageAccess access = authorizationService.getMyPageAccess(viewer, user);
        boolean isOwner = (access == MyPageAccess.OWNER);

        // 비공개 프로필 처리: 본인이 아니고 비공개 설정인 경우
        if (!isOwner && !user.isPublic()) {
            throw new RuntimeException("비공개 프로필입니다."); // 추후 커스텀 예외로 변경 가능
        }

        double meetBallIndex = reviewService.calculateMeetBallIndex(user);
        return MyPageProfileResponse.from(user, meetBallIndex, isOwner);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        userService.updateUserProfile(userId, request);
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getMyBookmarks(Long userId, Long viewerId) {
        if (!isOwnerAccess(userId, viewerId)) {
            return Collections.emptyList();
        }
        User user = userService.getUserById(userId);
        return bookmarkService.getBookmarkedProjects(user);
    }

    @Transactional(readOnly = true)
    public List<AppliedProjectResponse> getMyApplications(Long userId, Long viewerId) {
        if (!isOwnerAccess(userId, viewerId)) {
            return Collections.emptyList();
        }
        User user = userService.getUserById(userId);
        return applicationService.getAppliedProjects(user);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getMyProjects(Long userId, Long viewerId) {
        // 참여한 프로젝트 목록은 공개 정보이므로 타인도 조회 가능 (단, 유저가 공개 상태여야 함)
        User user = userService.getUserById(userId);
        User viewer = viewerId != null ? userService.getUserById(viewerId) : null;
        
        if (authorizationService.getMyPageAccess(viewer, user) == MyPageAccess.VISITOR && !user.isPublic()) {
            return Collections.emptyList();
        }
        
        return projectService.getParticipatedProjects(user);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getCompletedProjects(Long userId, Long viewerId) {
        // 마감된 프로젝트는 성과 지표이므로 공개 정보입니다.
        User user = userService.getUserById(userId);
        User viewer = viewerId != null ? userService.getUserById(viewerId) : null;
        
        if (authorizationService.getMyPageAccess(viewer, user) == MyPageAccess.VISITOR && !user.isPublic()) {
            return Collections.emptyList();
        }
        
        // 상태가 COMPLETED인 프로젝트만 필터링
        return projectService.getParticipatedProjects(user).stream()
                .filter(p -> p.getStatus() == ProjectStatus.COMPLETED)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReadProjectResponse> getRecentReads(Long userId, Long viewerId) {
        if (!isOwnerAccess(userId, viewerId)) {
            return Collections.emptyList();
        }
        User user = userService.getUserById(userId);
        return projectReadService.getReadHistory(user);
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponse> getMyReviews(Long userId, Long viewerId) {
        // 상세 텍스트 리뷰는 본인만 확인 가능
        if (!isOwnerAccess(userId, viewerId)) {
            return Collections.emptyList();
        }
        
        User user = userService.getUserById(userId);
        return reviewService.getUserReviews(user);
    }

    /**
     * 본인 접근 여부를 확인하는 헬퍼 메서드
     */
    private boolean isOwnerAccess(Long userId, Long viewerId) {
        if (viewerId == null) return false;
        User user = userService.getUserById(userId);
        User viewer = userService.getUserById(viewerId);
        return authorizationService.getMyPageAccess(viewer, user) == MyPageAccess.OWNER;
    }
}
