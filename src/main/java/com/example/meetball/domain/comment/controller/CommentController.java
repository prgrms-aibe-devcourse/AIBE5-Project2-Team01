package com.example.meetball.domain.comment.controller;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.dto.CommentResponseDto;
import com.example.meetball.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/comments")
public class CommentController {

    private final CommentService commentService;

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
            @RequestBody CommentRequestDto requestDto) {
        CommentRequestDto commentData = new CommentRequestDto(
                projectId, 
                requestDto.getAuthorNickname(), 
                requestDto.getAuthorRole(),
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
            @RequestBody CommentRequestDto requestDto) {
        return ResponseEntity.ok(commentService.updateComment(commentId, requestDto));
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long projectId,
            @PathVariable Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
