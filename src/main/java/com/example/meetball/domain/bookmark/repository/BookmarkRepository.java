package com.example.meetball.domain.bookmark.repository;

import com.example.meetball.domain.bookmark.entity.Bookmark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 유저와 프로젝트에 기반하여 찾기 (토글 삭제용)
    Optional<Bookmark> findByProjectIdAndUserNickname(Long projectId, String userNickname);

    // 해당 프로젝트의 총 찜 개수 조회용
    int countByProjectId(Long projectId);
}
