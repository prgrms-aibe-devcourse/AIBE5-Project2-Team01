package com.example.meetball.domain.bookmark.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(name = "bookmarks", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"project_id", "user_nickname"})
})
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: 원래는 Project 엔티티와 연관관계를 맺어야 하나 MVP(임시 DB)이므로 ID값만 저장
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    // TODO: User 엔티티와 연관관계 매핑 (MVP 단계에서는 닉네임 문자열로 임시 관리)
    @Column(name = "user_nickname", nullable = false)
    private String userNickname;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Bookmark(Long projectId, String userNickname) {
        this.projectId = projectId;
        this.userNickname = userNickname;
    }
}
