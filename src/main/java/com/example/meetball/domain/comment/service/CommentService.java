package com.example.meetball.domain.comment.service;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.dto.CommentResponseDto;
import com.example.meetball.domain.comment.entity.Comment;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public Page<CommentResponseDto> getCommentsByProjectId(Long projectId, Pageable pageable) {
        return commentRepository.findByProjectIdAndParentIsNullOrderByCreatedAtDesc(projectId, pageable)
                .map(CommentResponseDto::new);
    }

    @Transactional
    public CommentResponseDto saveComment(CommentRequestDto requestDto, User author) {
        // 1. 비회원(NONE)은 모든 작성 불가
        if (author == null || "NONE".equals(requestDto.getAuthorRole()) || requestDto.getAuthorRole() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        // 2. 답글(대댓글) 권한 체크: 팀장(LEADER) 또는 팀원(MEMBER)만 가능
        if (requestDto.getParentId() != null) {
            if (!"LEADER".equals(requestDto.getAuthorRole()) && !"MEMBER".equals(requestDto.getAuthorRole())) {
                throw new IllegalArgumentException("답글은 팀 멤버만 작성할 수 있습니다.");
            }
        }
        
        Comment parentComment = null;
        if (requestDto.getParentId() != null) {
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
            verifyProject(parentComment, requestDto.getProjectId());
        }

        Comment comment = Comment.builder()
                .projectId(requestDto.getProjectId())
                .authorNickname(requestDto.getAuthorNickname())
                .authorUserId(author.getId())
                .authorRole(requestDto.getAuthorRole())
                .content(requestDto.getContent())
                .parent(parentComment)
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    // 2. 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long projectId, Long commentId, CommentRequestDto requestDto, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        verifyProject(comment, projectId);
        verifyAuthor(comment, currentUser);
        comment.updateContent(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    // 2. 댓글 삭제
    @Transactional
    public void deleteComment(Long projectId, Long commentId, User currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        verifyProject(comment, projectId);
        verifyAuthor(comment, currentUser);
        commentRepository.delete(comment);
    }

    private void verifyProject(Comment comment, Long projectId) {
        if (!comment.getProjectId().equals(projectId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found in this project.");
        }
    }

    private void verifyAuthor(Comment comment, User currentUser) {
        if (currentUser == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }
        Long authorUserId = comment.getAuthorUserId();
        if (authorUserId != null) {
            if (!authorUserId.equals(currentUser.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another user's comment.");
            }
            return;
        }
        // Existing imported comments may not have authorUserId yet.
        if (!comment.getAuthorNickname().equals(currentUser.getNickname())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot modify another user's comment.");
        }
    }
}
