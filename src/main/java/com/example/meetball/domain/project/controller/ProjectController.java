package com.example.meetball.domain.project.controller;

import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public Page<ProjectListResponseDto> getProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "projectType", required = false) String projectType,
            @RequestParam(name = "progressMethod", required = false) String progressMethod
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.getProjects(keyword, projectType, progressMethod, pageable);
    }

    @GetMapping("/{projectId}")
    public ProjectDetailResponseDto getProjectById(@org.springframework.web.bind.annotation.PathVariable("projectId") Long projectId) {
        return projectService.getProjectById(projectId);
    }

    @PostMapping
    public ProjectDetailResponseDto createProject(@RequestBody ProjectCreateRequestDto request) {
        return projectService.createProject(request);
    }

    @PutMapping("/{projectId}")
    public ProjectDetailResponseDto updateProject(@org.springframework.web.bind.annotation.PathVariable("projectId") Long projectId,
                                                  @RequestBody ProjectUpdateRequestDto request) {
        return projectService.updateProject(projectId, request);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@org.springframework.web.bind.annotation.PathVariable("projectId") Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
