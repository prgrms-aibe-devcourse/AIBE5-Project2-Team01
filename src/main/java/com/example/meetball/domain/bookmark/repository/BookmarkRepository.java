package com.example.meetball.domain.bookmark.repository;

import com.example.meetball.domain.bookmark.entity.Bookmark;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    // 유저와 프로젝트에 기반하여 찾기 (토글 삭제용)
    Optional<Bookmark> findByProjectAndUser(Project project, User user);

    // 사용자별 찜 목록 조회용
    List<Bookmark> findByUser(User user);

    // 해당 프로젝트의 총 찜 개수 조회용
    int countByProject(Project project);
}
