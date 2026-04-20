package com.example.meetball.domain.comment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommentRequestDto {
    private Long projectId;
    private String authorNickname;
    private String authorRole; // 권한 추가
    private String content;
    private Long parentId; 

    public CommentRequestDto(Long projectId, String authorNickname, String authorRole, String content, Long parentId) {
        this.projectId = projectId;
        this.authorNickname = authorNickname;
        this.authorRole = authorRole;
        this.content = content;
        this.parentId = parentId;
    }
}
