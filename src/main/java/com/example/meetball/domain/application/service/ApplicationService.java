package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ApplicationResponseDto createApplication(Long projectId, ApplicationRequestDto request, String requesterName) {
        // 1. 비회원 또는 사용자 식별 정보 없는 경우 지원 불가 처리
        if (requesterName == null || requesterName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required. Please provide X-User-Name header.");
        }
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        // 3. 마감된 프로젝트는 지원 불가 처리
        if (Boolean.TRUE.equals(project.getClosed()) || 
            (project.getRecruitmentDeadline() != null && project.getRecruitmentDeadline().isBefore(LocalDate.now())) ||
            (project.getRecruitmentEndAt() != null && project.getRecruitmentEndAt().isBefore(LocalDate.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to a closed or past deadline project.");
        }

        // yunseok1: user 조회
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId()).orElse(null);
        }

        // 4. 동일 사용자의 동일 프로젝트 중복 지원 불가 처리
        if (applicationRepository.existsByProjectIdAndApplicantName(projectId, requesterName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied to this project by applicant: " + requesterName);
        }
        if (user != null && applicationRepository.existsByProjectAndUser(project, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied to this project by user: " + user.getId());
        }

        Application application = Application.builder()
                .project(project)
                .user(user)
                .applicantName(requesterName)
                .position(request.getPosition())
                .message(request.getMessage())
                .status(ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);

        return new ApplicationResponseDto(savedApplication);
    }

    public List<ApplicationResponseDto> getMyApplications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId));
        return applicationRepository.findByUser(user).stream()
                .map(ApplicationResponseDto::new)
                .collect(Collectors.toList());
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

        List<Application> applications = applicationRepository.findByProject(project);
        return applications.stream()
                .map(ApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request, String requesterName) {
        // 7. 존재하지 않는 지원(application) 상태 변경 시 적절한 예외 처리
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId));

        Project project = application.getProject();
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found.");
        }

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

        return new ApplicationResponseDto(updatedApplication);
    }
}
