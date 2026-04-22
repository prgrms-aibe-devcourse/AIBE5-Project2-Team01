package com.example.meetball.domain.comment.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private String authorNickname;

    @Column(name = "author_user_id")
    private Long authorUserId;

    @Column(nullable = false, length = 1000)
    private String content;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 작성자의 당시 직책 (LEADER, MEMBER, GUEST 등)
    @Column(nullable = false)
    private String authorRole;

    // 대댓글을 위한 자기 참조 관계 설정 (부모 댓글)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    // 대댓글 목록 (자식 댓글들)
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> children = new ArrayList<>();

    @Builder
    public Comment(Long projectId, String authorNickname, Long authorUserId, String content, Comment parent, String authorRole) {
        this.projectId = projectId;
        this.authorNickname = authorNickname;
        this.authorUserId = authorUserId;
        this.content = content;
        this.parent = parent;
        this.authorRole = authorRole;
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
