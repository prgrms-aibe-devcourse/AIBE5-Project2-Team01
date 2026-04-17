package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

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

    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));
        projectRepository.delete(project);
    }
}
