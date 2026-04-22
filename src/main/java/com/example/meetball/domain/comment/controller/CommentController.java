package com.example.meetball.domain.comment.controller;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.dto.CommentResponseDto;
import com.example.meetball.domain.comment.service.CommentService;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
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
    private final UserService userService;
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
            @SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        User user = userService.getUserById(userId);
        ProjectDetailRole role = authorizationService.getProjectDetailRole(user, project);

        CommentRequestDto commentData = new CommentRequestDto(
                projectId, 
                user.getNickname(),
                role.name(),
                requestDto.getContent(), 
                requestDto.getParentId()
        );
        return ResponseEntity.ok(commentService.saveComment(commentData));
    }

    // 댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long projectId,
            @PathVariable Long commentId,
            @RequestBody CommentRequestDto requestDto,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long commentId,
            @SessionAttribute(name = "userId", required = false) Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
