package com.example.meetball.domain.projectresource.controller;

import com.example.meetball.domain.projectresource.dto.ProjectResourceResponseDto;
import com.example.meetball.domain.projectresource.service.ProjectResourceService;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
import com.example.meetball.global.auth.enums.ProjectDetailRole;
import com.example.meetball.global.auth.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/resources")
@RequiredArgsConstructor
public class ProjectResourceController {

    private final ProjectResourceService projectResourceService;
    private final ProjectRepository projectRepository;
    private final ProfileService profileService;
    private final AuthorizationService authorizationService;

    // 해당 프로젝트의 모든 자료 목록 조회
    @GetMapping
    public ResponseEntity<List<ProjectResourceResponseDto>> getResources(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectResourceService.getProjectResources(projectId));
    }

    // 파일 업로드 (팀장, 팀원만 가능)
    @PostMapping
    public ResponseEntity<ProjectResourceResponseDto> uploadResource(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        requireProjectParticipant(projectId, profileId, "Only project members can upload resources.");

        ProjectResourceResponseDto responseDto = projectResourceService.uploadFile(projectId, file);
        return ResponseEntity.status(201).body(responseDto);
    }

    @PostMapping("/links")
    public ResponseEntity<ProjectResourceResponseDto> uploadLink(
            @PathVariable Long projectId,
            @RequestBody LinkRequest request,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        requireProjectParticipant(projectId, profileId, "Only project members can add links.");

        ProjectResourceResponseDto responseDto = projectResourceService.uploadLink(projectId, request.title(), request.url());
        return ResponseEntity.status(201).body(responseDto);
    }

    // 파일 다운로드
    @GetMapping("/{resourceId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long projectId,
            @PathVariable Long resourceId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        requireProjectParticipant(projectId, profileId, "Only project members can download resources.");
        
        Resource resource = projectResourceService.loadFileAsResource(projectId, resourceId);
        String originalFileName = projectResourceService.getOriginalFileName(projectId, resourceId);
        
        // 한글 파일명 깨짐 방지
        String encodedUploadFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    private void requireProjectParticipant(Long projectId, Long profileId, String forbiddenMessage) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        Profile profile = profileService.getProfileById(profileId);
        ProjectDetailRole role = authorizationService.getProjectDetailRole(profile, project);

        if (role != ProjectDetailRole.LEADER && role != ProjectDetailRole.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
    }

    public record LinkRequest(String title, String url) {
    }
}
