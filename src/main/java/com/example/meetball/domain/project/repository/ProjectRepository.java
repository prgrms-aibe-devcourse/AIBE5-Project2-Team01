package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByOrderByCreatedDateDescIdDesc();
}
