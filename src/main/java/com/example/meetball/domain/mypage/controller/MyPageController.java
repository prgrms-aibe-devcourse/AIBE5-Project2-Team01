package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.projectapplication.dto.ProjectApplicationResponseDto;
import com.example.meetball.domain.bookmarkedproject.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.profile.dto.ProfileOnboardingRequest;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.viewhistory.dto.ViewHistoryProjectResponse;
import com.example.meetball.domain.review.dto.PeerReviewResponse;
import com.example.meetball.domain.profile.dto.ProfileUpdateRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping("/profile")
    public ResponseEntity<MyPageProfileResponse> getMyProfile(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getMyProfile(currentProfileId, currentProfileId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestBody ProfileUpdateRequest request,
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId,
            HttpSession session) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        try {
            myPageService.updateProfile(currentProfileId, request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            session.setAttribute("profileNickname", request.getNickname());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/onboarding")
    public ResponseEntity<Void> completeOnboarding(
            @RequestBody ProfileOnboardingRequest request,
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId,
            HttpSession session) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        Profile profile;
        try {
            profile = myPageService.completeOnboarding(currentProfileId, request);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage());
        }
        session.setAttribute("profileNickname", profile.getNickname());
        session.removeAttribute("needsProfile");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<BookmarkedProjectResponse>> getMyBookmarks(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getMyBookmarks(currentProfileId, currentProfileId));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ProjectApplicationResponseDto>> getMyApplications(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getMyApplications(currentProfileId, currentProfileId));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ParticipatedProjectResponse>> getMyProjects(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getMyProjects(currentProfileId, currentProfileId));
    }

    @GetMapping("/projects/completed")
    public ResponseEntity<List<ParticipatedProjectResponse>> getCompletedProjects(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getCompletedProjects(currentProfileId, currentProfileId));
    }

    @GetMapping("/recent-reads")
    public ResponseEntity<List<ViewHistoryProjectResponse>> getRecentReads(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getRecentReads(currentProfileId, currentProfileId));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<PeerReviewResponse>> getMyReviews(
            @SessionAttribute(name = "profileId", required = false) Long sessionProfileId) {
        Long currentProfileId = requireSessionProfile(sessionProfileId);
        return ResponseEntity.ok(myPageService.getMyReviews(currentProfileId, currentProfileId));
    }

    private Long requireSessionProfile(Long sessionProfileId) {
        if (sessionProfileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return sessionProfileId;
    }
}
