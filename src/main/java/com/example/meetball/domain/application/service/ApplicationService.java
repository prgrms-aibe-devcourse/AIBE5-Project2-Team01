package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;

    public ApplicationService(ApplicationRepository applicationRepository, ProjectRepository projectRepository) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
    }

    public ApplicationResponseDto createApplication(Long projectId, ApplicationRequestDto request) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }

        if (applicationRepository.existsByProjectIdAndApplicantName(projectId, request.getApplicantName())) {
            throw new RuntimeException("Already applied to this project by applicant: " + request.getApplicantName());
        }

        LocalDateTime now = LocalDateTime.now();
        Application application = new Application(
                projectId,
                request.getApplicantName(),
                request.getPosition(),
                request.getMessage(),
                ApplicationStatus.PENDING,
                now,
                now
        );

        Application savedApplication = applicationRepository.save(application);

        return convertToResponseDto(savedApplication);
    }

    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new RuntimeException("Project not found with id: " + projectId);
        }

        List<Application> applications = applicationRepository.findByProjectId(projectId);
        return applications.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + applicationId));

        ApplicationStatus nextStatus;
        try {
            nextStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + request.getStatus());
        }

        application.updateStatus(nextStatus, LocalDateTime.now());
        Application updatedApplication = applicationRepository.save(application);

        return convertToResponseDto(updatedApplication);
    }

    private ApplicationResponseDto convertToResponseDto(Application application) {
        return new ApplicationResponseDto(
                application.getId(),
                application.getProjectId(),
                application.getApplicantName(),
                application.getPosition(),
                application.getMessage(),
                application.getStatus().name(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
