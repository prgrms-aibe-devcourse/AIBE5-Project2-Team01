package com.example.meetball.domain.projectresource.repository;

import com.example.meetball.domain.projectresource.entity.ProjectResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectResourceRepository extends JpaRepository<ProjectResource, Long> {
    List<ProjectResource> findByProjectId(Long projectId);

    void deleteByProjectId(Long projectId);
}
