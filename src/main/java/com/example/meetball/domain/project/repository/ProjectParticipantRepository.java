package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectParticipantRepository extends JpaRepository<ProjectParticipant, Long> {
    List<ProjectParticipant> findByProfile(Profile profile);

    boolean existsByProjectAndProfile(Project project, Profile profile);

    Optional<ProjectParticipant> findByProjectAndProfile(Project project, Profile profile);

    List<ProjectParticipant> findByProject(Project project);

    long countByProject(Project project);

    void deleteByProject(Project project);

    void deleteByProjectAndProfile(Project project, Profile profile);
}
