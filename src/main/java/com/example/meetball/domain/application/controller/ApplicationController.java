package com.example.meetball.domain.application.controller;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.List;

import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;
    private final UserService userService;

    private Long resolveUserId(Long sessionUserId) {
        if (sessionUserId != null) return sessionUserId;
        return userService.findDefaultUser().map(User::getId).orElse(null);
    }

    // =====================================================
    // 지원자용 API
    // =====================================================

    @PostMapping("/api/projects/{projectId}/applications")
    public ResponseEntity<ApplicationResponseDto> applyToProject(
            @PathVariable("projectId") Long projectId,
            @RequestBody ApplicationRequestDto request,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        return ResponseEntity.status(201).body(applicationService.createApplication(projectId, request, resolveUserId(userId)));
    }

    /** 내가 지원한 목록 조회 (마이페이지용) */
    @GetMapping("/api/users/{userId}/applications")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(applicationService.getMyApplications(userId));
    }

    // =====================================================
    // 팀장용 API
    // =====================================================

    /** 특정 프로젝트의 지원자 목록 조회 */
    @GetMapping("/api/projects/{projectId}/applications")
    public List<ApplicationResponseDto> getApplications(
            @PathVariable("projectId") Long projectId,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        return applicationService.getApplicationsByProjectId(projectId, resolveUserId(userId));
    }

    /** 지원 상태 변경 (승인/거절) */
    @PatchMapping("/api/applications/{applicationId}/status")
    public ApplicationResponseDto updateApplicationStatus(
            @PathVariable("applicationId") Long applicationId,
            @RequestBody ApplicationStatusUpdateRequestDto request,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        return applicationService.updateApplicationStatus(applicationId, request, resolveUserId(userId));
    }
}
