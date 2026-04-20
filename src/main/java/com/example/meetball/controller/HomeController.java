package com.example.meetball.controller;

import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.service.ProjectService;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProjectService projectService;

    public HomeController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<ProjectSummaryView> projects = projectService.getProjectSummaries();
        model.addAttribute("projects", projects);
        model.addAttribute("projectCount", projects.size());
        return "home/index";
    }
}