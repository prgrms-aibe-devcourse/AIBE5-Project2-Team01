package com.example.meetball.domain.comment.controller;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.dto.CommentResponseDto;
import com.example.meetball.domain.comment.service.CommentService;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
import com.example.meetball.global.auth.enums.ProjectDetailRole;
import com.example.meetball.global.auth.service.AuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final ProjectRepository projectRepository;
    private final ProfileService profileService;
    private final AuthorizationService authorizationService;

    // 댓글 조회 (페이징 적용: size=10 등으로 한 번에 10개씩 가져옴)
    @GetMapping
    public ResponseEntity<Page<CommentResponseDto>> getComments(
            @PathVariable Long projectId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CommentResponseDto> comments = commentService.getCommentsByProjectId(projectId, pageable);
        return ResponseEntity.ok(comments);
    }

    // 댓글/대댓글 등록
    @PostMapping
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long projectId,
            @RequestBody CommentRequestDto requestDto,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        Profile profile = profileService.getProfileById(profileId);
        ProjectDetailRole role = authorizationService.getProjectDetailRole(profile, project);

        CommentRequestDto commentData = new CommentRequestDto(
                projectId, 
                role.name(),
                requestDto.getContent(), 
                requestDto.getParentId()
        );
        return ResponseEntity.ok(commentService.saveComment(commentData, profile));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        Profile profile = profileService.getProfileById(profileId);
        return ResponseEntity.ok(commentService.updateComment(projectId, commentId, requestDto, profile));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long commentId,
            @SessionAttribute(name = "profileId", required = false) Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        Profile profile = profileService.getProfileById(profileId);
        commentService.deleteComment(projectId, commentId, profile);
        return ResponseEntity.noContent().build();
    }
}
