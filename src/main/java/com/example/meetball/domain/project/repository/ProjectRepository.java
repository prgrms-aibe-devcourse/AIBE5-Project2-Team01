package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:projectType IS NULL OR :projectType = '' OR p.projectType = :projectType) AND " +
           "(:progressMethod IS NULL OR :progressMethod = '' OR p.progressMethod = :progressMethod)")
    Page<Project> findProjectsWithFilters(@Param("keyword") String keyword,
                                          @Param("projectType") String projectType,
                                          @Param("progressMethod") String progressMethod,
                                          Pageable pageable);

    List<Project> findAllByOrderByCreatedDateDescIdDesc();
}
