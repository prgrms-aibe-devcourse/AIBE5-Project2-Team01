package com.example.meetball.domain.attachment.controller;

import com.example.meetball.domain.attachment.dto.AttachmentResponseDto;
import com.example.meetball.domain.attachment.service.AttachmentService;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
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
@RequestMapping("/api/projects/{projectId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    // 해당 프로젝트의 모든 첨부파일 목록 조회
    @GetMapping
    public ResponseEntity<List<AttachmentResponseDto>> getAttachments(@PathVariable Long projectId) {
        return ResponseEntity.ok(attachmentService.getProjectAttachments(projectId));
    }

    // 파일 업로드 (팀장, 팀원만 가능)
    @PostMapping
    public ResponseEntity<AttachmentResponseDto> uploadAttachment(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        requireProjectMember(projectId, userId, "Only project members can upload attachments.");

        AttachmentResponseDto responseDto = attachmentService.uploadFile(projectId, file);
        return ResponseEntity.status(201).body(responseDto);
    }

    // 파일 다운로드
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long projectId,
            @PathVariable Long attachmentId,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        requireProjectMember(projectId, userId, "Only project members can download attachments.");
        
        Resource resource = attachmentService.loadFileAsResource(projectId, attachmentId);
        String originalFileName = attachmentService.getOriginalFileName(projectId, attachmentId);
        
        // 한글 파일명 깨짐 방지
        String encodedUploadFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }

    private void requireProjectMember(Long projectId, Long userId, String forbiddenMessage) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        User user = userService.getUserById(userId);
        ProjectDetailRole role = authorizationService.getProjectDetailRole(user, project);

        if (role != ProjectDetailRole.LEADER && role != ProjectDetailRole.MEMBER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
    }
}
