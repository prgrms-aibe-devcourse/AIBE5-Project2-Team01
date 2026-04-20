package com.example.meetball.domain.application.controller;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    // =====================================================
    // 지원자용 API
    // =====================================================

    /** 프로젝트에 지원하기 */
    @PostMapping("/api/projects/{projectId}/applications")
    public ResponseEntity<ApplicationResponseDto> applyToProject(
            @PathVariable Long projectId,
            @RequestBody ApplicationRequestDto request) {
        return ResponseEntity.status(201).body(applicationService.createApplication(projectId, request));
    }

    // =====================================================
    // 팀장용 API
    // =====================================================

    /** 특정 프로젝트의 지원자 목록 조회 */
    @GetMapping("/api/projects/{projectId}/applications")
    public List<ApplicationResponseDto> getApplications(@PathVariable Long projectId) {
        return applicationService.getApplicationsByProjectId(projectId);
    }

    /** 지원 상태 변경 (승인/거절) */
    @PatchMapping("/api/applications/{applicationId}/status")
    public ApplicationResponseDto updateApplicationStatus(
            @PathVariable Long applicationId,
            @RequestBody ApplicationStatusUpdateRequestDto request) {
        return applicationService.updateApplicationStatus(applicationId, request);
    }
}
