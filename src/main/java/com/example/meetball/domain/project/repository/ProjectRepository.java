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
            LEFT JOIN positionSelection.position positionValue
            LEFT JOIN p.techStackSelections techStackSelection
            LEFT JOIN techStackSelection.techStack techStackValue
            WHERE (:keyword IS NULL OR :keyword = ''
                   OR p.title LIKE CONCAT('%', :keyword, '%')
                   OR positionValue.name LIKE CONCAT('%', :keyword, '%')
                   OR techStackValue.name LIKE CONCAT('%', :keyword, '%'))
              AND (:projectType IS NULL OR :projectType = ''
                   OR LOWER(COALESCE(p.projectType, '')) LIKE LOWER(CONCAT('%', :projectType, '%')))
              AND (:progressMethod IS NULL OR :progressMethod = ''
                   OR LOWER(COALESCE(p.workMethod, '')) = LOWER(:progressMethod)
                   OR (:progressMethod = 'ONLINE'
                       AND (LOWER(COALESCE(p.workMethod, '')) LIKE '%online%'
                            OR COALESCE(p.workMethod, '') LIKE '%온라인%'))
                   OR (:progressMethod = 'OFFLINE'
                       AND (LOWER(COALESCE(p.workMethod, '')) LIKE '%offline%'
                            OR COALESCE(p.workMethod, '') LIKE '%오프라인%'))
                   OR (:progressMethod = 'HYBRID'
                       AND (LOWER(COALESCE(p.workMethod, '')) LIKE '%hybrid%'
                            OR COALESCE(p.workMethod, '') LIKE '%혼합%'
                            OR COALESCE(p.workMethod, '') LIKE '%온/오프%')))
              AND (:position IS NULL OR :position = ''
                   OR positionValue.name LIKE CONCAT('%', :position, '%'))
              AND (:techStack IS NULL OR :techStack = ''
                   OR techStackValue.name LIKE CONCAT('%', :techStack, '%'))
            """)
    Page<Project> findProjectsWithFilters(@Param("keyword") String keyword,
                                          @Param("projectType") String projectType,
                                          @Param("progressMethod") String progressMethod,
                                          @Param("position") String position,
                                          @Param("techStack") String techStack,
                                          Pageable pageable);

    List<Project> findAllByOrderByCreatedAtDescIdDesc();
}
