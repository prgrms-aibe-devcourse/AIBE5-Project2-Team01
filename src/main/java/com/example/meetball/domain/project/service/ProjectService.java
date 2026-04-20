package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProjectService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    // --- MVC (front2) ---
    public List<ProjectSummaryView> getProjectSummaries() {
        return projectRepository.findAllByOrderByCreatedDateDescIdDesc()
                .stream()
                .map(this::toSummaryView)
                .toList();
    }

    public ProjectDetailView getProjectDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        return toDetailView(project);
    }

    private ProjectSummaryView toSummaryView(Project project) {
        return new ProjectSummaryView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getProjectType(),
                project.getPosition(),
                project.getLeaderName(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                project.getCurrentRecruitment(),
                project.getTotalRecruitment(),
                formatDeadline(project.getRecruitmentDeadline()),
                splitTechStacks(project.getTechStackCsv())
        );
    }

    private ProjectDetailView toDetailView(Project project) {
        return new ProjectDetailView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getProjectType(),
                project.getPosition(),
                project.getLeaderName(),
                project.getLeaderRole(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                project.getCurrentRecruitment(),
                project.getTotalRecruitment(),
                calculateProgressPercent(project.getCurrentRecruitment(), project.getTotalRecruitment()),
                formatDeadline(project.getRecruitmentDeadline()),
                project.getCreatedDate() != null ? project.getCreatedDate().format(DATE_FORMATTER) : "",
                splitTechStacks(project.getTechStackCsv())
        );
    }

    private List<String> splitTechStacks(String techStackCsv) {
        if (techStackCsv == null) return List.of();
        return Arrays.stream(techStackCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private String formatDeadline(LocalDate deadline) {
        if (deadline == null) return "-";
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days < 0) {
            return "마감";
        }
        return "D-" + days;
    }

    private int calculateProgressPercent(Integer currentRecruitment, Integer totalRecruitment) {
        if (currentRecruitment == null) currentRecruitment = 0;
        if (totalRecruitment == null || totalRecruitment <= 0) {
            return 0;
        }
        return Math.min(100, (currentRecruitment * 100) / totalRecruitment);
    }

    // --- REST API (HEAD) ---
    public Page<ProjectListResponseDto> getProjects(String keyword, String projectType, 
                                                    String progressMethod, Pageable pageable) {
        Page<Project> projects = projectRepository.findProjectsWithFilters(keyword, projectType, progressMethod, pageable);
        
        return projects.map(project -> new ProjectListResponseDto(
                project.getId(),
                project.getTitle(),
                project.getRecruitmentCount(),
                project.getProjectType(),
                project.getProgressMethod(),
                project.getRecruitmentEndAt(),
                project.getClosed(),
                project.getCreatedAt()
        ));
    }

    public ProjectDetailResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        
        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getProjectType(),
                project.getProgressMethod(),
                project.getRecruitmentCount(),
                project.getRecruitmentStartAt(),
                project.getRecruitmentEndAt(),
                project.getProjectStartAt(),
                project.getProjectEndAt(),
                project.getClosed(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional
    public ProjectDetailResponseDto createProject(ProjectCreateRequestDto request) {
        LocalDateTime now = LocalDateTime.now();
        Project project = new Project(
                request.getTitle(),
                request.getDescription(),
                request.getProjectType(),
                request.getProgressMethod(),
                request.getRecruitmentCount(),
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                request.getClosed(),
                now,
                now
        );
        Project savedProject = projectRepository.save(project);
        
        return new ProjectDetailResponseDto(
                savedProject.getId(), savedProject.getTitle(), savedProject.getDescription(),
                savedProject.getProjectType(), savedProject.getProgressMethod(), savedProject.getRecruitmentCount(),
                savedProject.getRecruitmentStartAt(), savedProject.getRecruitmentEndAt(), savedProject.getProjectStartAt(),
                savedProject.getProjectEndAt(), savedProject.getClosed(), savedProject.getCreatedAt(), savedProject.getUpdatedAt()
        );
    }

    @Transactional
    public ProjectDetailResponseDto updateProject(Long projectId, ProjectUpdateRequestDto request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        
        project.update(
                request.getTitle(),
                request.getDescription(),
                request.getProjectType(),
                request.getProgressMethod(),
                request.getRecruitmentCount(),
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                request.getClosed(),
                LocalDateTime.now()
        );
        
        Project updatedProject = projectRepository.save(project);
        
        return new ProjectDetailResponseDto(
                updatedProject.getId(), updatedProject.getTitle(), updatedProject.getDescription(),
                updatedProject.getProjectType(), updatedProject.getProgressMethod(), updatedProject.getRecruitmentCount(),
                updatedProject.getRecruitmentStartAt(), updatedProject.getRecruitmentEndAt(), updatedProject.getProjectStartAt(),
                updatedProject.getProjectEndAt(), updatedProject.getClosed(), updatedProject.getCreatedAt(), updatedProject.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        projectRepository.delete(project);
    }
}
