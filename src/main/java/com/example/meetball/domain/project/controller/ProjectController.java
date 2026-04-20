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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // --- MVC (front2) ---
    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.getProjectSummaries());
        return "home/index";
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        try {
            model.addAttribute("project", projectService.getProjectDetail(id));
            return "project/detail";
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found", exception);
        }
    }

    @GetMapping("/register")
    public String register() {
        return "project/register";
    }

    @GetMapping("/manage")
    public String manage() {
        return "project/manage";
    }

    // --- REST API (HEAD) ---
    @ResponseBody
    @GetMapping("/api/projects")
    public Page<ProjectListResponseDto> getProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "projectType", required = false) String projectType,
            @RequestParam(name = "progressMethod", required = false) String progressMethod,
            @RequestParam(name = "position", required = false) String position,
            @RequestParam(name = "techStack", required = false) String techStack
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return projectService.getProjects(keyword, projectType, progressMethod, position, techStack, pageable);
    }

    @ResponseBody
    @GetMapping("/api/projects/{projectId}")
    public ProjectDetailResponseDto getProjectById(@PathVariable("projectId") Long projectId) {
        return projectService.getProjectById(projectId);
    }

    @ResponseBody
    @PostMapping("/api/projects")
    public ProjectDetailResponseDto createProject(@RequestBody ProjectCreateRequestDto request,
                                                  @RequestHeader(value = "X-User-Name", required = false) String requesterName) {
        return projectService.createProject(request, requesterName);
    }

    @ResponseBody
    @PutMapping("/api/projects/{projectId}")
    public ProjectDetailResponseDto updateProject(@PathVariable("projectId") Long projectId,
                                                  @RequestBody ProjectUpdateRequestDto request,
                                                  @RequestHeader(value = "X-User-Name", required = false) String requesterName) {
        return projectService.updateProject(projectId, request, requesterName);
    }

    @ResponseBody
    @DeleteMapping("/api/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId,
                                              @RequestHeader(value = "X-User-Name", required = false) String requesterName) {
        projectService.deleteProject(projectId, requesterName);
        return ResponseEntity.noContent().build();
    }
}
