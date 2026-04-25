package com.example.meetball.domain.projectapplication.repository;

import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectApplicationRepository extends JpaRepository<ProjectApplication, Long> {
    List<ProjectApplication> findByProfile(Profile profile);

    List<ProjectApplication> findByProfileOrderByCreatedAtDesc(Profile profile);

    List<ProjectApplication> findByProject(Project project);

    List<ProjectApplication> findAllByProjectAndProfile(Project project, Profile profile);

    boolean existsByProjectAndProfile(Project project, Profile profile);

    void deleteByProject(Project project);
}
