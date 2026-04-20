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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    // TODO: 현재는 RequestParam으로 userId를 받지만 추후 @AuthenticationPrincipal 로 토큰/세션 기반 인증 유저를 가져와야 함.
    
    @GetMapping("/profile")
    public ResponseEntity<MyPageProfileResponse> getMyProfile(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getMyProfile(userId, viewerId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestParam Long userId,
            @RequestBody UserProfileUpdateRequest request) {
        myPageService.updateUserProfile(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<List<BookmarkedProjectResponse>> getMyBookmarks(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getMyBookmarks(userId, viewerId));
    }

    @GetMapping("/applications")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getMyApplications(userId, viewerId));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<ParticipatedProjectResponse>> getMyProjects(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getMyProjects(userId, viewerId));
    }

    @GetMapping("/projects/completed")
    public ResponseEntity<List<ParticipatedProjectResponse>> getCompletedProjects(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getCompletedProjects(userId, viewerId));
    }

    @GetMapping("/recent-reads")
    public ResponseEntity<List<ReadProjectResponse>> getRecentReads(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getRecentReads(userId, viewerId));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<UserReviewResponse>> getMyReviews(
            @RequestParam Long userId,
            @RequestParam(required = false) Long viewerId) {
        return ResponseEntity.ok(myPageService.getMyReviews(userId, viewerId));
    }
}
