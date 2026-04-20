package com.example.meetball.domain.project.controller;

import com.example.meetball.domain.project.service.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/projects")
    public String projects(Model model) {
        model.addAttribute("projects", projectService.getProjectSummaries());
        return "home/index";
    }

    @GetMapping("/projects/{id}")
    public String detail(@PathVariable Long id, Model model) {
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
}
