package com.example.meetball.domain.mypage.service;

import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.service.ApplicationService;
import com.example.meetball.domain.bookmark.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.bookmark.service.BookmarkService;
import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
        requireOwnerAccess(userId, viewerId);
        User user = userService.getUserById(userId);

        double meetBallIndex = reviewService.calculateMeetBallIndex(user);
        return MyPageProfileResponse.from(user, meetBallIndex, true);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        userService.updateUserProfile(userId, request);
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getMyBookmarks(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        User user = userService.getUserById(userId);
        return bookmarkService.getBookmarkedProjects(user);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyApplications(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        return applicationService.getMyApplications(userId);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getMyProjects(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        User user = userService.getUserById(userId);
        return projectService.getParticipatedProjects(user);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getCompletedProjects(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        User user = userService.getUserById(userId);
        return projectService.getParticipatedProjects(user).stream()
                .filter(ParticipatedProjectResponse::isCompleted)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReadProjectResponse> getRecentReads(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        User user = userService.getUserById(userId);
        return projectReadService.getReadHistory(user);
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponse> getMyReviews(Long userId, Long viewerId) {
        requireOwnerAccess(userId, viewerId);
        
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

    private void requireOwnerAccess(Long userId, Long viewerId) {
        if (!isOwnerAccess(userId, viewerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "My page is only available for the signed-in user.");
        }
    }
}
