package com.example.meetball.domain.viewhistory.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.viewhistory.entity.ViewHistory;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    // 최근에 읽은 순서대로 정렬해서 가져오기
    List<ViewHistory> findByProfileOrderByReadAtDesc(Profile profile);

    Optional<ViewHistory> findByProjectAndProfile(Project project, Profile profile);

    int countByProject(Project project);

    void deleteByProject(Project project);
}
