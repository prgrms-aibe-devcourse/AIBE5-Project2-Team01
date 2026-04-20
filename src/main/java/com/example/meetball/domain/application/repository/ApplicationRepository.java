package com.example.meetball.domain.application.repository;

import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // 마이페이지용: 내가 지원한 목록 (우리 코드)
    List<Application> findByUser(User user);

    // 팀장용: 특정 프로젝트의 지원자 목록 (mergefile)
    List<Application> findByProject(Project project);

    // 중복 지원 방지 (User 기반)
    boolean existsByProjectAndUser(Project project, User user);
}
