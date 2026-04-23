package com.example.meetball.domain.projectread.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.projectread.entity.ProjectRead;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectReadRepository extends JpaRepository<ProjectRead, Long> {
    // 최근에 읽은 순서대로 정렬해서 가져오기
    List<ProjectRead> findByUserOrderByReadAtDesc(User user);

    Optional<ProjectRead> findByProjectAndUser(Project project, User user);

    int countByProject(Project project);

    void deleteByProject(Project project);
}
