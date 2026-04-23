package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.entity.ProjectPosition;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    private record ResolvedPosition(String name, ProjectPosition entity) {
    }

    @Transactional
    public ApplicationResponseDto createApplication(Long projectId, ApplicationRequestDto request, Long userId) {
        // 1. 비회원 또는 사용자 식별 정보 없는 경우 지원 불가 처리
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        // 3. 모집 마감 또는 완료된 프로젝트는 지원 불가 처리
        if (Boolean.TRUE.equals(project.getClosed()) ||
            Boolean.TRUE.equals(project.getCompleted()) ||
            (project.getRecruitmentDeadline() != null && project.getRecruitmentDeadline().isBefore(LocalDate.now())) ||
            (project.getRecruitmentEndAt() != null && project.getRecruitmentEndAt().isBefore(LocalDate.now()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to a closed or past deadline project.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        String applicantNickname = user.getNickname();

        if (projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project members cannot apply to their own project.");
        }
        if (project.isRecruitmentFull()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recruitment count is already full.");
        }

        ResolvedPosition selectedPosition = validatePositionCapacity(project, request.getPosition(), null);

        boolean hasActiveApplication = applicationRepository.findAllByProjectAndUser(project, user).stream()
                .anyMatch(application -> application.getStatus() == null || !application.getStatus().isHiddenFromManagement());
        if (hasActiveApplication) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied to this project by user: " + user.getId());
        }

        Application application = Application.builder()
                .project(project)
                .user(user)
                .applicantName(applicantNickname)
                .position(selectedPosition.name())
                .projectPosition(selectedPosition.entity())
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

    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId, Long userId) {
        // 2. 존재하지 않는 프로젝트
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        // 5. 지원 목록 조회는 권한 없는 사용자가 못 보게 막음
        if (!isProjectLeader(project, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can view the applications.");
        }

        List<Application> applications = applicationRepository.findByProject(project);
        return applications.stream()
                .filter(application -> application.getStatus() == null || !application.getStatus().isHiddenFromManagement())
                .map(ApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request, Long userId) {
        // 7. 존재하지 않는 지원(application) 상태 변경 시 적절한 예외 처리
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId));

        Project project = application.getProject();
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found.");
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        // 6. 지원 상태 변경은 리더만 가능하도록 점검
        if (!isProjectLeader(project, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can update application status.");
        }

        ApplicationStatus previousStatus = application.getStatus();
        ApplicationStatus nextStatus = parseStatus(request);

        application.updateStatus(nextStatus, LocalDateTime.now());
        User applicant = application.getUser();
        if (applicant != null) {
            boolean wasAccepted = isAccepted(previousStatus);
            boolean willBeAccepted = isAccepted(nextStatus);
            boolean alreadyMember = projectMemberRepository.existsByProjectAndUser(project, applicant);

            if (willBeAccepted && !alreadyMember) {
                ResolvedPosition acceptedPosition = validatePositionCapacity(project, applicationPositionName(application), application.getId());
                if (acceptedPosition.entity() != null) {
                    application.updateProjectPosition(acceptedPosition.entity());
                }
                try {
                    project.incrementCurrentRecruitment();
                } catch (IllegalStateException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
                }
                projectMemberRepository.save(ProjectMember.builder()
                        .project(project)
                        .user(applicant)
                        .role("MEMBER")
                        .build());
            } else if (!willBeAccepted && wasAccepted && alreadyMember) {
                projectMemberRepository.deleteByProjectAndUser(project, applicant);
                project.decrementCurrentRecruitment();
            }
            if (shouldDetachPosition(nextStatus)) {
                application.updateProjectPosition(null);
            }
        }

        Application updatedApplication = applicationRepository.save(application);

        return new ApplicationResponseDto(updatedApplication);
    }

    @Transactional
    public ApplicationResponseDto withdrawApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId));

        if (userId == null || application.getUser() == null || !userId.equals(application.getUser().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the applicant can withdraw this application.");
        }

        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending applications can be withdrawn.");
        }

        application.updateProjectPosition(null);
        application.updateStatus(ApplicationStatus.WITHDRAWN, LocalDateTime.now());
        return new ApplicationResponseDto(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationResponseDto removeApplication(Long applicationId, Long userId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found with id: " + applicationId));
        Project project = application.getProject();
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found.");
        }

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User leader = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));
        if (!isProjectLeader(project, leader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can remove applications.");
        }

        User applicant = application.getUser();
        boolean wasAccepted = isAccepted(application.getStatus());
        boolean alreadyMember = applicant != null && projectMemberRepository.existsByProjectAndUser(project, applicant);
        if (wasAccepted && alreadyMember) {
            projectMemberRepository.deleteByProjectAndUser(project, applicant);
            project.decrementCurrentRecruitment();
        }

        application.updateProjectPosition(null);
        application.updateStatus(ApplicationStatus.REMOVED, LocalDateTime.now());
        return new ApplicationResponseDto(applicationRepository.save(application));
    }

    private boolean isProjectLeader(Project project, User user) {
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(projectMember -> "LEADER".equals(projectMember.getRole()))
                .orElse(false);
    }

    private ResolvedPosition validatePositionCapacity(Project project, String requestedPosition, Long excludedApplicationId) {
        String selectedPosition;
        try {
            selectedPosition = ProjectSelectionCatalog.positionName(requestedPosition);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application position is required.", exception);
        }

        List<ProjectSelectionCatalog.PositionCapacity> capacities = projectPositionCapacities(project);
        ProjectSelectionCatalog.PositionCapacity target = capacities.stream()
                .filter(position -> position.name().equals(selectedPosition))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to an unavailable position."));
        ProjectPosition targetEntity = project.getPositionSelections().stream()
                .filter(position -> target.name().equals(position.getPositionName()))
                .findFirst()
                .orElse(null);

        long acceptedCount = applicationRepository.findByProject(project).stream()
                .filter(application -> excludedApplicationId == null || !excludedApplicationId.equals(application.getId()))
                .filter(application -> target.name().equals(applicationPositionName(application)))
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .count();

        if (acceptedCount >= target.capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected position is already full.");
        }
        return new ResolvedPosition(selectedPosition, targetEntity);
    }

    private List<ProjectSelectionCatalog.PositionCapacity> projectPositionCapacities(Project project) {
        if (project.getPositionSelections() != null && !project.getPositionSelections().isEmpty()) {
            return project.getPositionSelections().stream()
                    .map(position -> new ProjectSelectionCatalog.PositionCapacity(position.getPositionName(), position.getCapacity()))
                    .toList();
        }
        return ProjectSelectionCatalog.parsePositionCapacities(project.getPosition(), project.getTotalRecruitment());
    }

    private String applicationPositionName(Application application) {
        ProjectPosition position = application.getProjectPosition();
        if (position != null && position.getPositionName() != null) {
            return position.getPositionName();
        }
        return ProjectSelectionCatalog.positionName(application.getPosition());
    }

    private ApplicationStatus parseStatus(ApplicationStatusUpdateRequestDto request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Application status is required.");
        }
        String rawStatus = request.getStatus().trim().toUpperCase();
        if ("APPROVED".equals(rawStatus)) {
            return ApplicationStatus.ACCEPTED;
        }
        try {
            return ApplicationStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
        }
    }

    private boolean isAccepted(ApplicationStatus status) {
        return status != null && status.isAccepted();
    }

    private boolean shouldDetachPosition(ApplicationStatus status) {
        return status == ApplicationStatus.REJECTED
                || status == ApplicationStatus.WITHDRAWN
                || status == ApplicationStatus.REMOVED;
    }
}
