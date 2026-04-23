package com.example.meetball.domain.review.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.review.entity.ProjectReview;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectReviewRepository extends JpaRepository<ProjectReview, Long> {

    List<ProjectReview> findByProject(Project project);

    boolean existsByProjectAndReviewer(Project project, Profile reviewer);

    void deleteByProject(Project project);
}
