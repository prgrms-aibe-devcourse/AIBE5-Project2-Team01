package com.example.meetball.domain.comment.service;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.dto.CommentResponseDto;
import com.example.meetball.domain.comment.entity.Comment;
import com.example.meetball.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CommentResponseDto saveComment(CommentRequestDto requestDto) {
        Comment parentComment = null;

        if (requestDto.getParentId() != null) {
            // 대댓글 작성 권한 체크 (팀장/팀원만 가능)
            if (!"LEADER".equals(requestDto.getAuthorRole()) && !"MEMBER".equals(requestDto.getAuthorRole())) {
                throw new IllegalArgumentException("답글은 팀 인원만 작성할 수 있습니다.");
            }
            parentComment = commentRepository.findById(requestDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
        }

        Comment comment = Comment.builder()
                .projectId(requestDto.getProjectId())
                .authorNickname(requestDto.getAuthorNickname())
                .authorRole(requestDto.getAuthorRole())
                .content(requestDto.getContent())
                .parent(parentComment)
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        return new CommentResponseDto(savedComment);
    }

    // 2. 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        
        // 작성자 검증 로직이 향후 필요함 (로그인 유저 == 작성자)
        comment.updateContent(requestDto.getContent());
        return new CommentResponseDto(comment);
    }

    // 2. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        // 대댓글이 있는 부모 댓글 삭제 시 설정된 cascade에 따라 대댓글도 지워지거나, 
        // 혹은 '삭제된 댓글입니다' 처리를 할 수도 있습니다 (현재는 하위 데이터까지 완벽 삭제)
        commentRepository.delete(comment);
    }
}
