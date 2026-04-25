package com.example.meetball.domain.project.controller;

import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectPageResponseDto;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.service.ProjectService;
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
    private final com.example.meetball.domain.profile.service.ProfileService profileService;
    private final com.example.meetball.global.auth.service.AuthorizationService authorizationService;

    public ProjectController(ProjectService projectService,
                             com.example.meetball.domain.project.repository.ProjectRepository projectRepository,
                             com.example.meetball.domain.profile.service.ProfileService profileService,
                             com.example.meetball.global.auth.service.AuthorizationService authorizationService) {
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.profileService = profileService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/projects")
    public String projects() {
        return "redirect:/";
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable("id") Long id,
                         @SessionAttribute(name = "profileId", required = false) Long profileId,
                         Model model) {
        try {
            projectService.recordProjectView(id, profileId);
            // 1. DTO 추가
            model.addAttribute("project", projectService.getProjectDetail(id));
            model.addAttribute("currentProjectPath", "/projects/" + id);
            
            // 2. 권한(Role) 확인 로직
            com.example.meetball.domain.project.entity.Project projectEntity = 
                    projectRepository.findById(id).orElse(null);
            
            com.example.meetball.domain.profile.entity.Profile currentProfile = null;
            if (profileId != null) {
                currentProfile = profileService.getProfileById(profileId);
            }
            
            if (projectEntity != null) {
                com.example.meetball.global.auth.enums.ProjectDetailRole role = 
                        authorizationService.getProjectDetailRole(currentProfile, projectEntity);
                model.addAttribute("role", role.name()); // GUEST, REGULAR_USER, MEMBER, LEADER
            }
            
            return "project/detail";
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found", exception);
        }
    }

    @GetMapping("/register")
    public String register(@SessionAttribute(name = "profileId", required = false) Long profileId,
                           Model model) {
        if (profileId == null) {
            return "redirect:/?login=1&redirect=/register";
        }
        model.addAttribute("pageTitle", "프로젝트 등록");
        model.addAttribute("editMode", false);
        model.addAttribute("projectId", null);
        model.addAttribute("editProject", null);
        model.addAttribute("positionEditConstraints", java.util.List.of());
        return "project/register";
    }

    @GetMapping("/projects/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @SessionAttribute(name = "profileId", required = false) Long profileId,
                       Model model) {
        if (profileId == null) {
            return "redirect:/?login=1&redirect=/projects/" + id + "/edit";
        }

        com.example.meetball.domain.project.entity.Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id));
        com.example.meetball.domain.profile.entity.Profile currentProfile = profileService.getProfileById(profileId);
        com.example.meetball.global.auth.enums.ProjectDetailRole role =
                authorizationService.getProjectDetailRole(currentProfile, project);
        if (role != com.example.meetball.global.auth.enums.ProjectDetailRole.LEADER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can edit the project.");
        }

        model.addAttribute("pageTitle", "프로젝트 수정");
        model.addAttribute("editMode", true);
        model.addAttribute("projectId", id);
        model.addAttribute("editProject", projectService.getProjectById(id));
        model.addAttribute("positionEditConstraints", projectService.getProjectPositionEditConstraints(id));
        return "project/register";
    }

    @GetMapping("/projects/{id}/manage")
    public String manage(@PathVariable("id") Long id,
                         @SessionAttribute(name = "profileId", required = false) Long profileId,
        Model model) {
        if (profileId == null) {
            return "redirect:/?login=1&redirect=/projects/" + id + "/manage";
        }

        com.example.meetball.domain.project.entity.Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + id));
        com.example.meetball.domain.profile.entity.Profile currentProfile = profileService.getProfileById(profileId);
        com.example.meetball.global.auth.enums.ProjectDetailRole role =
                authorizationService.getProjectDetailRole(currentProfile, project);
        if (role != com.example.meetball.global.auth.enums.ProjectDetailRole.LEADER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can manage the project.");
        }

        model.addAttribute("projectId", id);
        model.addAttribute("project", projectService.getProjectById(id));
        return "project/manage";
    }

    // --- REST API ---
    @ResponseBody
    @GetMapping("/api/projects")
    public ProjectPageResponseDto<ProjectListResponseDto> getProjects(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "6") int size,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "projectPurpose", required = false) String projectPurpose,
            @RequestParam(name = "position", required = false) String position,
            @RequestParam(name = "techStack", required = false) String techStack,
            @RequestParam(name = "bookmarkedOnly", defaultValue = "false") boolean bookmarkedOnly,
            @SessionAttribute(name = "profileId", required = false) Long profileId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectListResponseDto> projects = projectService.getProjects(keyword, projectPurpose, position, techStack, bookmarkedOnly, pageable, profileId);
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
                                                  @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectService.createProject(request, profileId);
    }

    @ResponseBody
    @PutMapping("/api/projects/{projectId}")
    public ProjectDetailResponseDto updateProject(@PathVariable("projectId") Long projectId,
                                                  @RequestBody ProjectUpdateRequestDto request,
                                                  @SessionAttribute(name = "profileId", required = false) Long profileId) {
        return projectService.updateProject(projectId, request, profileId);
    }

    @ResponseBody
    @DeleteMapping("/api/projects/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable("projectId") Long projectId,
                                              @SessionAttribute(name = "profileId", required = false) Long profileId) {
        projectService.deleteProject(projectId, profileId);
        return ResponseEntity.noContent().build();
    }
}
