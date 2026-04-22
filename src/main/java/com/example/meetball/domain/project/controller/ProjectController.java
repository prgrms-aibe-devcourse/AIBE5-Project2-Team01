package com.example.meetball.domain.project.controller;

import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectPageResponseDto;
import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.service.ProjectService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProjectController {

    private final ProjectService projectService;
    private final com.example.meetball.domain.project.repository.ProjectRepository projectRepository;
    private final com.example.meetball.domain.user.service.UserService userService;
    private final com.example.meetball.global.auth.service.AuthorizationService authorizationService;

    public ProjectController(ProjectService projectService,
                             com.example.meetball.domain.project.repository.ProjectRepository projectRepository,
                             com.example.meetball.domain.user.service.UserService userService,
                             com.example.meetball.global.auth.service.AuthorizationService authorizationService) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    // --- MVC (front2) ---
    @GetMapping("/projects")
    public String projects(Model model) {
        List<ProjectSummaryView> projects = projectService.getProjectSummaries();
        model.addAttribute("projects", projects);
        model.addAttribute("projectCount", projects.size());
        return "home/index";
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable("id") Long id,
                         @RequestParam(name = "userId", required = false) Long userId,
                         Model model) {
        try {
            // 1. DTO 추가
            model.addAttribute("project", projectService.getProjectDetail(id));
            
            // 2. 권한(Role) 확인 로직
            com.example.meetball.domain.project.entity.Project projectEntity = 
                    projectRepository.findById(id).orElse(null);
            
            com.example.meetball.domain.user.entity.User currentUser = null;
            if (userId != null) {
                // 임시로 DB에서 유저를 조회 (실제 로그인 기능 제외에 따른 Mocking)
                currentUser = userService.getUserById(userId);
            }
            
            if (projectEntity != null) {
                com.example.meetball.global.auth.enums.ProjectDetailRole role = 
                        authorizationService.getProjectDetailRole(currentUser, projectEntity);
                model.addAttribute("role", role.name()); // GUEST, REGULAR_USER, MEMBER, LEADER
            }
            
            return "project/detail";
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found", exception);
        }
    }

    @GetMapping("/register")
    public String register() {
        return "project/register";
    }

    @GetMapping("/projects/{id}/manage")
    public String manage(@PathVariable("id") Long id, Model model) {
        model.addAttribute("projectId", id);
        return "project/manage";
    }

    // --- REST API ---
    @ResponseBody
    @GetMapping("/api/projects")
    public ProjectPageResponseDto<ProjectListResponseDto> getProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "projectType", required = false) String projectType,
            @RequestParam(name = "progressMethod", required = false) String progressMethod,
            @RequestParam(name = "position", required = false) String position,
            @RequestParam(name = "techStack", required = false) String techStack
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectListResponseDto> projects = projectService.getProjects(keyword, projectType, progressMethod, position, techStack, pageable);
        return ProjectPageResponseDto.from(projects);
    }

    @ResponseBody
    @GetMapping("/api/projects/{projectId}")
    public ProjectDetailResponseDto getProjectById(@PathVariable("projectId") Long projectId) {
        return projectService.getProjectById(projectId);
    }

    @ResponseBody
    @PostMapping("/api/projects")
    public ProjectDetailResponseDto createProject(@RequestBody ProjectCreateRequestDto request,
                                                  @SessionAttribute(name = "userId", required = false) Long userId) {
        return projectService.createProject(request, userId);
    }

    @ResponseBody
    @PutMapping("/api/projects/{projectId}")
    public ProjectDetailResponseDto updateProject(@PathVariable("projectId") Long projectId,
                                                  @RequestBody ProjectUpdateRequestDto request,
                                                  @SessionAttribute(name = "userId", required = false) Long userId) {
        return projectService.updateProject(projectId, request, userId);
    }

    @ResponseBody
    @DeleteMapping("/api/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId,
                                              @SessionAttribute(name = "userId", required = false) Long userId) {
        projectService.deleteProject(projectId, userId);
        return ResponseEntity.noContent().build();
    }
}
