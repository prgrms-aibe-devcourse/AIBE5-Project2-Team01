package com.example.meetball.domain.projectapplication.controller;

import com.example.meetball.domain.projectapplication.dto.ProjectApplicationRequestDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationResponseDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.projectapplication.service.ProjectApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProjectApplicationController {

    private final ProjectApplicationService projectApplicationService;

    // =====================================================
    // 지원자용 API
    // =====================================================

    @PostMapping("/api/projects/{projectId}/applications")
    public ResponseEntity<ProjectApplicationResponseDto> applyToProject(
            @PathVariable("projectId") Long projectId,
            @RequestBody ProjectApplicationRequestDto request,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return ResponseEntity.status(201).body(projectApplicationService.createApplication(projectId, request, profileId));
    }

    /** 내가 낸 지원 철회 */
    @PatchMapping("/api/applications/{applicationId}/withdraw")
    public ProjectApplicationResponseDto withdrawApplication(
            @PathVariable("applicationId") Long applicationId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectApplicationService.withdrawApplication(applicationId, profileId);
    }

    @GetMapping("/api/projects/{projectId}/applications/me")
    public ResponseEntity<ProjectApplicationResponseDto> getMyApplicationForProject(
            @PathVariable("projectId") Long projectId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        ProjectApplicationResponseDto response = projectApplicationService.getMyApplicationForProject(projectId, profileId);
        return response == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(response);
    }

    @PutMapping("/api/applications/{applicationId}")
    public ProjectApplicationResponseDto updateMyApplication(
            @PathVariable("applicationId") Long applicationId,
            @RequestBody ProjectApplicationRequestDto request,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectApplicationService.updateMyApplication(applicationId, request, profileId);
    }

    // =====================================================
    // 팀장용 API
    // =====================================================

    /** 특정 프로젝트의 지원자 목록 조회 */
    @GetMapping("/api/projects/{projectId}/applications")
    public List<ProjectApplicationResponseDto> getApplications(
            @PathVariable("projectId") Long projectId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectApplicationService.getApplicationsByProjectId(projectId, profileId);
    }

    /** 지원 상태 변경 (승인/거절) */
    @PatchMapping("/api/applications/{applicationId}/status")
    public ProjectApplicationResponseDto updateApplicationStatus(
            @PathVariable("applicationId") Long applicationId,
            @RequestBody ProjectApplicationStatusUpdateRequestDto request,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectApplicationService.updateApplicationStatus(applicationId, request, profileId);
    }

    /** 지원서 삭제 또는 승인된 팀원 추방 */
    @DeleteMapping("/api/applications/{applicationId}")
    public ProjectApplicationResponseDto removeApplication(
            @PathVariable("applicationId") Long applicationId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectApplicationService.removeApplication(applicationId, profileId);
    }
}
