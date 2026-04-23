package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    @Query("""
            SELECT DISTINCT p
            FROM Project p
            LEFT JOIN p.positionSelections positionSelection
            LEFT JOIN p.techStackSelections techStackSelection
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR p.title LIKE CONCAT('%', :keyword, '%')
                   OR positionSelection.positionName LIKE CONCAT('%', :keyword, '%')
                   OR techStackSelection.techStackName LIKE CONCAT('%', :keyword, '%'))
              AND (:projectType IS NULL OR :projectType = ''
                   OR LOWER(COALESCE(p.projectType, '')) LIKE LOWER(CONCAT('%', :projectType, '%')))
              AND (:progressMethod IS NULL OR :progressMethod = ''
                   OR LOWER(COALESCE(p.progressMethod, '')) = LOWER(:progressMethod)
                   OR (:progressMethod = 'ONLINE'
                       AND (LOWER(COALESCE(p.progressMethod, '')) LIKE '%online%'
                            OR COALESCE(p.progressMethod, '') LIKE '%온라인%'))
                   OR (:progressMethod = 'OFFLINE'
                       AND (LOWER(COALESCE(p.progressMethod, '')) LIKE '%offline%'
                            OR COALESCE(p.progressMethod, '') LIKE '%오프라인%'))
                   OR (:progressMethod = 'HYBRID'
                       AND (LOWER(COALESCE(p.progressMethod, '')) LIKE '%hybrid%'
                            OR COALESCE(p.progressMethod, '') LIKE '%혼합%'
                            OR COALESCE(p.progressMethod, '') LIKE '%온/오프%')))
              AND (:position IS NULL OR :position = ''
                   OR positionSelection.positionName LIKE CONCAT('%', :position, '%'))
              AND (:techStack IS NULL OR :techStack = ''
                   OR techStackSelection.techStackName LIKE CONCAT('%', :techStack, '%'))
            """)
    Page<Project> findProjectsWithFilters(@Param("keyword") String keyword,
                                          @Param("projectType") String projectType,
                                          @Param("progressMethod") String progressMethod,
                                          @Param("position") String position,
                                          @Param("techStack") String techStack,
                                          Pageable pageable);

    List<Project> findAllByOrderByCreatedDateDescIdDesc();
}
