package com.example.meetball.domain.bookmarkedproject.repository;

import com.example.meetball.domain.bookmarkedproject.entity.BookmarkedProject;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkedProjectRepository extends JpaRepository<BookmarkedProject, Long> {

    // 유저와 프로젝트에 기반하여 찾기 (토글 삭제용)
    Optional<BookmarkedProject> findByProjectAndProfile(Project project, Profile profile);

    // 사용자별 찜 목록 조회용
    List<BookmarkedProject> findByProfile(Profile profile);

    // 해당 프로젝트의 총 찜 개수 조회용
    int countByProject(Project project);

    void deleteByProject(Project project);
}
