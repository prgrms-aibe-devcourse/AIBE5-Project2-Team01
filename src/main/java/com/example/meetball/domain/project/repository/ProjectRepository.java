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
            WHERE p.recruitStatus = :recruitStatus
              AND p.progressStatus <> :completedStatus
            ORDER BY p.createdAt DESC, p.id DESC
            """)
    List<Project> findRecommendationCandidates(@Param("recruitStatus") String recruitStatus,
                                               @Param("completedStatus") String completedStatus);

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
              AND (:projectPurpose IS NULL OR :projectPurpose = ''
                   OR COALESCE(p.projectPurpose, '') = :projectPurpose)
              AND (:workMethod IS NULL OR :workMethod = ''
                   OR COALESCE(p.workMethod, '') = :workMethod)
              AND (:position IS NULL OR :position = ''
                   OR positionValue.name LIKE CONCAT('%', :position, '%'))
              AND (:techStack IS NULL OR :techStack = ''
                   OR techStackValue.name LIKE CONCAT('%', :techStack, '%'))
            """)
    Page<Project> findProjectsWithFilters(@Param("keyword") String keyword,
                                          @Param("projectPurpose") String projectPurpose,
                                          @Param("workMethod") String workMethod,
                                          @Param("position") String position,
                                          @Param("techStack") String techStack,
                                          Pageable pageable);

    List<Project> findAllByOrderByCreatedAtDescIdDesc();
}
