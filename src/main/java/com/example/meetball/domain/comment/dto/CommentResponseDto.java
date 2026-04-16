package com.example.meetball.domain.comment.dto;

import com.example.meetball.domain.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentResponseDto {
    private Long id;
    private Long projectId;
    private String authorNickname;
    private String authorRole;
    private String content;
    private LocalDateTime createdAt;
    private List<CommentResponseDto> children; // 대댓글 리스트 포함

    public CommentResponseDto(Comment comment) {
        this.id = comment.getId();
        this.projectId = comment.getProjectId();
        this.authorNickname = comment.getAuthorNickname();
        this.authorRole = comment.getAuthorRole();
        this.content = comment.getContent();
        this.createdAt = comment.getCreatedAt();
        // 자식 댓글들을 DTO로 변환
        this.children = comment.getChildren() != null ? 
            comment.getChildren().stream()
                   .map(CommentResponseDto::new)
                   .collect(Collectors.toList()) : new ArrayList<>();
    }
}
