package com.example.meetball.domain.mypage.service;

import com.example.meetball.domain.application.dto.AppliedProjectResponse;
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

    @Transactional(readOnly = true)
    public MyPageProfileResponse getMyProfile(Long userId, Long viewerId) {
        User user = userService.getUserById(userId);
        double meetBallIndex = reviewService.calculateMeetBallIndex(user);
        boolean isOwner = userId.equals(viewerId);
        
        return MyPageProfileResponse.from(user, meetBallIndex, isOwner);
    }

    @Transactional
    public void updateUserProfile(Long userId, UserProfileUpdateRequest request) {
        userService.updateUserProfile(userId, request);
    }

    // 아래 메서드들은 Mock (임시) 데이터 반환용 뼈대입니다. 추후 타 도메인 연결 시 실제 로직으로 변경됩니다.

    public List<BookmarkedProjectResponse> getMyBookmarks(Long userId) {
        User user = userService.getUserById(userId);
        return bookmarkService.getBookmarkedProjects(user);
    }

    public List<AppliedProjectResponse> getMyApplications(Long userId) {
        User user = userService.getUserById(userId);
        return applicationService.getAppliedProjects(user);
    }

    public List<ParticipatedProjectResponse> getMyProjects(Long userId) {
        User user = userService.getUserById(userId);
        return projectService.getParticipatedProjects(user);
    }

    public List<ReadProjectResponse> getRecentReads(Long userId) {
        User user = userService.getUserById(userId);
        return projectReadService.getReadHistory(user);
    }

    @Transactional(readOnly = true)
    public List<UserReviewResponse> getMyReviews(Long userId, Long viewerId) {
        // 본인이 아닌 경우 빈 목록 반환 (프라이버시 보호)
        if (viewerId == null || !userId.equals(viewerId)) {
            return Collections.emptyList();
        }
        
        User user = userService.getUserById(userId);
        return reviewService.getUserReviews(user);
    }
}
