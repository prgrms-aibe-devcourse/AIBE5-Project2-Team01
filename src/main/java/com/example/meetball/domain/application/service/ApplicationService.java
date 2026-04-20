package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;

    public ApplicationService(ApplicationRepository applicationRepository, ProjectRepository projectRepository) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public ApplicationResponseDto createApplication(Long projectId, ApplicationRequestDto request, String requesterName) {
        // 1. 비회원 또는 사용자 식별 정보 없는 경우 지원 불가 처리
        if (requesterName == null || requesterName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required. Please provide X-User-Name header.");
        }
        
        // 2. 존재하지 않는 프로젝트에 지원 시 적절한 예외 처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        // 3. 마감된 프로젝트는 지원 불가 처리
        if (Boolean.TRUE.equals(project.getClosed()) || 
            (project.getRecruitmentDeadline() != null && project.getRecruitmentDeadline().isBefore(LocalDate.now())) ||
            (project.getRecruitmentEndAt() != null && project.getRecruitmentEndAt().isBefore(LocalDate.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to a closed or past deadline project.");
        }

        // 4. 동일 사용자의 동일 프로젝트 중복 지원 불가 처리
        if (applicationRepository.existsByProjectIdAndApplicantName(projectId, requesterName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied to this project by applicant: " + requesterName);
        }

        LocalDateTime now = LocalDateTime.now();
        Application application = new Application(
                projectId,
                requesterName, // requesterName을 식별된 지원자로 사용
                request.getPosition(),
                request.getMessage(),
                ApplicationStatus.PENDING,
                now,
                now
        );

        Application savedApplication = applicationRepository.save(application);

        return convertToResponseDto(savedApplication);
    }

    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId, String requesterName) {
        // 2. 존재하지 않는 프로젝트
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (requesterName == null || requesterName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        String utf8RequesterName = new String(requesterName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        boolean isLeader = requesterName.equals(project.getLeaderName()) || utf8RequesterName.equals(project.getLeaderName());

        // 5. 지원 목록 조회는 권한 없는 사용자가 못 보게 막음
        if (!isLeader) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can view the applications.");
        }

        List<Application> applications = applicationRepository.findByProjectId(projectId);
        return applications.stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request, String requesterName) {
        // 7. 존재하지 않는 지원(application) 상태 변경 시 적절한 예외 처리
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId));

        Project project = projectRepository.findById(application.getProjectId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found."));

        if (requesterName == null || requesterName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        String utf8RequesterName = new String(requesterName.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        boolean isLeader = requesterName.equals(project.getLeaderName()) || utf8RequesterName.equals(project.getLeaderName());

        // 6. 지원 상태 변경은 리더만 가능하도록 점검
        if (!isLeader) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can update application status.");
        }

        ApplicationStatus nextStatus;
        try {
            nextStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
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
