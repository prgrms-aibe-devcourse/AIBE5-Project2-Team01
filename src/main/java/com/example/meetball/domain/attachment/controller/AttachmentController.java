package com.example.meetball.domain.attachment.controller;

import com.example.meetball.domain.attachment.dto.AttachmentResponseDto;
import com.example.meetball.domain.attachment.service.AttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;

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
            @RequestParam(defaultValue = "GUEST") String userRole) {
        
        if ("GUEST".equals(userRole) || "NONE".equals(userRole)) {
            return ResponseEntity.status(403).build(); // 권한 없음
        }

        AttachmentResponseDto responseDto = attachmentService.uploadFile(projectId, file);
        return ResponseEntity.status(201).body(responseDto);
    }

    // 링크(URL) 등록 (팀장, 팀원만 가능)
    @PostMapping("/links")
    public ResponseEntity<AttachmentResponseDto> uploadLink(
            @PathVariable Long projectId,
            @RequestParam String title,
            @RequestParam String url,
            @RequestParam(defaultValue = "GUEST") String userRole) {
        
        if ("GUEST".equals(userRole) || "NONE".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        AttachmentResponseDto responseDto = attachmentService.uploadLink(projectId, title, url);
        return ResponseEntity.status(201).body(responseDto);
    }

    // 파일 다운로드
    @GetMapping("/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable Long projectId,
            @PathVariable Long attachmentId) {
        
        Resource resource = attachmentService.loadFileAsResource(attachmentId);
        String originalFileName = attachmentService.getOriginalFileName(attachmentId);
        
        // 한글 파일명 깨짐 방지
        String encodedUploadFileName = UriUtils.encode(originalFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                .body(resource);
    }
}
