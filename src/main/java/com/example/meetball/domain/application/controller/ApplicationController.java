package com.example.meetball.domain.application.controller;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.service.ApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/api/projects/{projectId}/applications")
    public ApplicationResponseDto applyToProject(@PathVariable("projectId") Long projectId,
                                                 @RequestBody ApplicationRequestDto request) {
        return applicationService.createApplication(projectId, request);
    }

    @GetMapping("/api/projects/{projectId}/applications")
    public List<ApplicationResponseDto> getApplications(@PathVariable("projectId") Long projectId) {
        return applicationService.getApplicationsByProjectId(projectId);
    }

    @PatchMapping("/api/applications/{applicationId}/status")
    public ApplicationResponseDto updateApplicationStatus(@PathVariable("applicationId") Long applicationId,
                                                          @RequestBody ApplicationStatusUpdateRequestDto request) {
        return applicationService.updateApplicationStatus(applicationId, request);
    }
}
