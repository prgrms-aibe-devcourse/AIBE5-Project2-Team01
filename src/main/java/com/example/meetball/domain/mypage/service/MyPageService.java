package com.example.meetball.domain.mypage.service;

import com.example.meetball.domain.projectapplication.dto.ProjectApplicationResponseDto;
import com.example.meetball.domain.projectapplication.service.ProjectApplicationService;
import com.example.meetball.domain.bookmarkedproject.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.bookmarkedproject.service.BookmarkedProjectService;
import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.service.ProjectService;
import com.example.meetball.domain.viewhistory.dto.ViewHistoryProjectResponse;
import com.example.meetball.domain.viewhistory.service.ViewHistoryService;
import com.example.meetball.domain.review.dto.PeerReviewResponse;
import com.example.meetball.domain.review.service.ReviewService;
import com.example.meetball.domain.profile.dto.ProfileOnboardingRequest;
import com.example.meetball.domain.profile.dto.ProfileUpdateRequest;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
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

    private final ProfileService profileService;
    private final ProjectService projectService;
    private final ProjectApplicationService projectApplicationService;
    private final ViewHistoryService viewHistoryService;
    private final BookmarkedProjectService bookmarkedProjectService;
    private final ReviewService reviewService;
    private final AuthorizationService authorizationService;

    @Transactional(readOnly = true)
    public MyPageProfileResponse getMyProfile(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        Profile profile = profileService.getProfileById(profileId);

        double meetBallIndex = reviewService.calculateMeetBallIndex(profile);
        return MyPageProfileResponse.from(profile, meetBallIndex, true);
    }

    @Transactional
    public void updateProfile(Long profileId, ProfileUpdateRequest request) {
        profileService.updateProfile(profileId, request);
    }

    @Transactional
    public Profile completeOnboarding(Long profileId, ProfileOnboardingRequest request) {
        return profileService.completeOnboarding(profileId, request);
    }

    @Transactional(readOnly = true)
    public List<BookmarkedProjectResponse> getMyBookmarks(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        Profile profile = profileService.getProfileById(profileId);
        return bookmarkedProjectService.getBookmarkedProjects(profile);
    }

    @Transactional(readOnly = true)
    public List<ProjectApplicationResponseDto> getMyApplications(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        return projectApplicationService.getMyApplications(profileId);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getMyProjects(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        Profile profile = profileService.getProfileById(profileId);
        return projectService.getParticipatedProjects(profile);
    }

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getCompletedProjects(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        Profile profile = profileService.getProfileById(profileId);
        return projectService.getParticipatedProjects(profile).stream()
                .filter(ParticipatedProjectResponse::isCompleted)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ViewHistoryProjectResponse> getRecentReads(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);
        Profile profile = profileService.getProfileById(profileId);
        return viewHistoryService.getReadHistory(profile);
    }

    @Transactional(readOnly = true)
    public List<PeerReviewResponse> getMyReviews(Long profileId, Long viewerId) {
        requireOwnerAccess(profileId, viewerId);

        Profile profile = profileService.getProfileById(profileId);
        return reviewService.getReceivedPeerReviews(profile);
    }

    /**
     * 본인 접근 여부를 확인하는 헬퍼 메서드
     */
    private boolean isOwnerAccess(Long profileId, Long viewerId) {
        if (viewerId == null) return false;
        Profile targetProfile = profileService.getProfileById(profileId);
        Profile viewerProfile = profileService.getProfileById(viewerId);
        return authorizationService.getMyPageAccess(viewerProfile, targetProfile) == MyPageAccess.OWNER;
    }

    private void requireOwnerAccess(Long profileId, Long viewerId) {
        if (!isOwnerAccess(profileId, viewerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "My page is only available for the signed-in user.");
        }
    }
}
