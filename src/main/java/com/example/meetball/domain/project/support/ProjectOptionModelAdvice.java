package com.example.meetball.domain.project.support;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class ProjectOptionModelAdvice {

    @ModelAttribute
    public void addProjectOptions(Model model) {
        model.addAttribute("meetballProjectPurposeOptions", ProjectOptionCatalog.projectPurposeOptions());
        model.addAttribute("meetballWorkMethodOptions", ProjectOptionCatalog.workMethodOptions());
    }
}
