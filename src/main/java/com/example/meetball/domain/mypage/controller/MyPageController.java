package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.bookmark.dto.BookmarkedProjectResponse;
import com.example.meetball.domain.mypage.dto.MyPageProfileResponse;
import com.example.meetball.domain.mypage.service.MyPageService;
import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.projectread.dto.ReadProjectResponse;
import com.example.meetball.domain.review.dto.UserReviewResponse;
import com.example.meetball.domain.user.dto.UserProfileUpdateRequest;
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
            @RequestParam(required = false) Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long targetUserId = userId != null ? userId : requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getMyProfile(targetUserId, sessionUserId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestParam(required = false) Long userId,
            @RequestBody UserProfileUpdateRequest request,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long currentUserId = requireSessionUser(sessionUserId);
        if (userId != null && !userId.equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update another user's profile.");
        }
        myPageService.updateUserProfile(currentUserId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<BookmarkedProjectResponse>> getMyBookmarks(
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long currentUserId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getMyBookmarks(currentUserId, currentUserId));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long currentUserId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getMyApplications(currentUserId, currentUserId));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ParticipatedProjectResponse>> getMyProjects(
            @RequestParam(required = false) Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long targetUserId = userId != null ? userId : requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getMyProjects(targetUserId, sessionUserId));
    }

    @GetMapping("/projects/completed")
    public ResponseEntity<List<ParticipatedProjectResponse>> getCompletedProjects(
            @RequestParam(required = false) Long userId,
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long targetUserId = userId != null ? userId : requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getCompletedProjects(targetUserId, sessionUserId));
    }

    @GetMapping("/recent-reads")
    public ResponseEntity<List<ReadProjectResponse>> getRecentReads(
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long currentUserId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getRecentReads(currentUserId, currentUserId));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<UserReviewResponse>> getMyReviews(
            @SessionAttribute(name = "userId", required = false) Long sessionUserId) {
        Long currentUserId = requireSessionUser(sessionUserId);
        return ResponseEntity.ok(myPageService.getMyReviews(currentUserId, currentUserId));
    }

    private Long requireSessionUser(Long sessionUserId) {
        if (sessionUserId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return sessionUserId;
    }
}
