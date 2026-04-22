package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT p FROM Project p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR p.title LIKE CONCAT('%', :keyword, '%') OR p.position LIKE CONCAT('%', :keyword, '%') OR p.techStackCsv LIKE CONCAT('%', :keyword, '%')) AND " +
           "(:projectType IS NULL OR :projectType = '' OR p.projectType = :projectType) AND " +
           "(:progressMethod IS NULL OR :progressMethod = '' OR p.progressMethod = :progressMethod) AND " +
           "(:position IS NULL OR :position = '' OR p.position LIKE CONCAT('%', :position, '%')) AND " +
           "(:techStack IS NULL OR :techStack = '' OR p.techStackCsv LIKE CONCAT('%', :techStack, '%'))")
    Page<Project> findProjectsWithFilters(@Param("keyword") String keyword,
                                          @Param("projectType") String projectType,
                                          @Param("progressMethod") String progressMethod,
                                          @Param("position") String position,
                                          @Param("techStack") String techStack,
                                          Pageable pageable);

    List<Project> findAllByOrderByCreatedDateDescIdDesc();
}
