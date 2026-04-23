package com.example.meetball.domain.projectapplication.service;

import com.example.meetball.domain.projectapplication.dto.ProjectApplicationRequestDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationResponseDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.entity.ProjectApplicationStatus;
import com.example.meetball.domain.projectapplication.repository.ProjectApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
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
public class ProjectApplicationService {

    private final ProjectApplicationRepository projectApplicationRepository;
    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProfileRepository profileRepository;

    private record ResolvedPosition(String name, ProjectRecruitPosition entity) {
    }

    @Transactional
    public ProjectApplicationResponseDto createApplication(Long projectId, ProjectApplicationRequestDto request, Long profileId) {
        // 1. 비회원 또는 사용자 식별 정보 없는 경우 지원 불가 처리
        if (profileId == null) {
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

        Profile applicantProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        String applicantNickname = applicantProfile.getNickname();

        if (projectParticipantRepository.existsByProjectAndProfile(project, applicantProfile)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Project members cannot apply to their own project.");
        }
        if (project.isRecruitmentFull()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recruitment count is already full.");
        }

        ResolvedPosition selectedPosition = validatePositionCapacity(project, request.getPosition(), null);

        boolean hasActiveApplication = projectApplicationRepository.findAllByProjectAndProfile(project, applicantProfile).stream()
                .anyMatch(application -> application.getStatus() == null || !application.getStatus().isHiddenFromManagement());
        if (hasActiveApplication) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already applied to this project.");
        }

        ProjectApplication application = ProjectApplication.builder()
                .project(project)
                .profile(applicantProfile)
                .applicantName(applicantNickname)
                .position(selectedPosition.name())
                .recruitPosition(selectedPosition.entity())
                .message(request.getMessage())
                .status(ProjectApplicationStatus.PENDING)
                .build();

        ProjectApplication savedApplication = projectApplicationRepository.save(application);

        return new ProjectApplicationResponseDto(savedApplication);
    }

    public List<ProjectApplicationResponseDto> getMyApplications(Long profileId) {
        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found with id: " + profileId));
        return projectApplicationRepository.findByProfile(profile).stream()
                .map(ProjectApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ProjectApplicationResponseDto> getApplicationsByProjectId(Long projectId, Long profileId) {
        // 2. 존재하지 않는 프로젝트
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile leaderProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        // 5. 지원 목록 조회는 권한 없는 사용자가 못 보게 막음
        if (!isProjectLeader(project, leaderProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can view the applications.");
        }

        List<ProjectApplication> applications = projectApplicationRepository.findByProject(project);
        return applications.stream()
                .filter(application -> application.getStatus() == null || !application.getStatus().isHiddenFromManagement())
                .map(ProjectApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProjectApplicationResponseDto updateApplicationStatus(Long applicationId, ProjectApplicationStatusUpdateRequestDto request, Long profileId) {
        // 7. 존재하지 않는 지원(application) 상태 변경 시 적절한 예외 처리
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectApplication not found with id: " + applicationId));

        Project project = application.getProject();
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found.");
        }

        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile leaderProfile = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));

        // 6. 지원 상태 변경은 리더만 가능하도록 점검
        if (!isProjectLeader(project, leaderProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can update application status.");
        }

        ProjectApplicationStatus previousStatus = application.getStatus();
        ProjectApplicationStatus nextStatus = parseStatus(request);

        application.updateStatus(nextStatus, LocalDateTime.now());
        Profile applicant = application.getProfile();
        if (applicant != null) {
            boolean wasAccepted = isAccepted(previousStatus);
            boolean willBeAccepted = isAccepted(nextStatus);
            boolean alreadyMember = projectParticipantRepository.existsByProjectAndProfile(project, applicant);

            if (willBeAccepted && !alreadyMember) {
                ResolvedPosition acceptedPosition = validatePositionCapacity(project, applicationPositionName(application), application.getId());
                if (acceptedPosition.entity() != null) {
                    application.updateRecruitPosition(acceptedPosition.entity());
                    acceptedPosition.entity().incrementApprovedUser();
                }
                projectParticipantRepository.save(ProjectParticipant.builder()
                        .project(project)
                        .profile(applicant)
                        .recruitPosition(acceptedPosition.entity())
                        .role("MEMBER")
                        .build());
            } else if (!willBeAccepted && wasAccepted && alreadyMember) {
                projectParticipantRepository.deleteByProjectAndProfile(project, applicant);
                if (application.getRecruitPosition() != null) {
                    application.getRecruitPosition().decrementApprovedUser();
                }
                project.decrementCurrentRecruitment();
            }
            if (shouldDetachPosition(nextStatus)) {
                application.updateRecruitPosition(null);
            }
        }

        ProjectApplication updatedApplication = projectApplicationRepository.save(application);

        return new ProjectApplicationResponseDto(updatedApplication);
    }

    @Transactional
    public ProjectApplicationResponseDto withdrawApplication(Long applicationId, Long profileId) {
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectApplication not found with id: " + applicationId));

        if (profileId == null || application.getProfile() == null || !profileId.equals(application.getProfile().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the applicant can withdraw this application.");
        }

        if (application.getStatus() != ProjectApplicationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending applications can be withdrawn.");
        }

        application.updateStatus(ProjectApplicationStatus.WITHDRAWN, LocalDateTime.now());
        return new ProjectApplicationResponseDto(projectApplicationRepository.save(application));
    }

    @Transactional
    public ProjectApplicationResponseDto removeApplication(Long applicationId, Long profileId) {
        ProjectApplication application = projectApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ProjectApplication not found with id: " + applicationId));
        Project project = application.getProject();
        if (project == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project related to this application not found.");
        }

        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile leader = profileRepository.findById(profileId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required."));
        if (!isProjectLeader(project, leader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can remove applications.");
        }

        Profile applicant = application.getProfile();
        boolean wasAccepted = isAccepted(application.getStatus());
        boolean alreadyMember = applicant != null && projectParticipantRepository.existsByProjectAndProfile(project, applicant);
        if (wasAccepted && alreadyMember) {
            projectParticipantRepository.deleteByProjectAndProfile(project, applicant);
            if (application.getRecruitPosition() != null) {
                application.getRecruitPosition().decrementApprovedUser();
            }
            project.decrementCurrentRecruitment();
        }

        application.updateStatus(ProjectApplicationStatus.REMOVED, LocalDateTime.now());
        return new ProjectApplicationResponseDto(projectApplicationRepository.save(application));
    }

    private boolean isProjectLeader(Project project, Profile profile) {
        return projectParticipantRepository.findByProjectAndProfile(project, profile)
                .map(participant -> "LEADER".equals(participant.getRole()))
                .orElse(false);
    }

    private ResolvedPosition validatePositionCapacity(Project project, String requestedPosition, Long excludedApplicationId) {
        String selectedPosition;
        try {
            selectedPosition = ProjectSelectionCatalog.positionName(requestedPosition);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ProjectApplication position is required.", exception);
        }

        List<ProjectSelectionCatalog.PositionCapacity> capacities = recruitPositionCapacities(project);
        ProjectSelectionCatalog.PositionCapacity target = capacities.stream()
                .filter(position -> position.name().equals(selectedPosition))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot apply to an unavailable position."));
        ProjectRecruitPosition targetEntity = project.getPositionSelections().stream()
                .filter(position -> target.name().equals(position.getPositionName()))
                .findFirst()
                .orElse(null);

        long acceptedCount = projectApplicationRepository.findByProject(project).stream()
                .filter(application -> excludedApplicationId == null || !excludedApplicationId.equals(application.getId()))
                .filter(application -> target.name().equals(applicationPositionName(application)))
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .count();

        if (acceptedCount >= target.capacity()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selected position is already full.");
        }
        return new ResolvedPosition(selectedPosition, targetEntity);
    }

    private List<ProjectSelectionCatalog.PositionCapacity> recruitPositionCapacities(Project project) {
        if (project.getPositionSelections() != null && !project.getPositionSelections().isEmpty()) {
            return project.getPositionSelections().stream()
                    .map(position -> new ProjectSelectionCatalog.PositionCapacity(position.getPositionName(), position.getCapacity()))
                    .toList();
        }
        return ProjectSelectionCatalog.parsePositionCapacities(project.getPosition(), project.getTotalRecruitment());
    }

    private String applicationPositionName(ProjectApplication application) {
        ProjectRecruitPosition position = application.getRecruitPosition();
        if (position != null && position.getPositionName() != null) {
            return position.getPositionName();
        }
        return ProjectSelectionCatalog.positionName(application.getPosition());
    }

    private ProjectApplicationStatus parseStatus(ProjectApplicationStatusUpdateRequestDto request) {
        if (request == null || request.getStatus() == null || request.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ProjectApplication status is required.");
        }
        String rawStatus = request.getStatus().trim().toUpperCase();
        if ("APPROVED".equals(rawStatus)) {
            return ProjectApplicationStatus.ACCEPTED;
        }
        try {
            return ProjectApplicationStatus.valueOf(rawStatus);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + request.getStatus());
        }
    }

    private boolean isAccepted(ProjectApplicationStatus status) {
        return status != null && status.isAccepted();
    }

    private boolean shouldDetachPosition(ProjectApplicationStatus status) {
        return false;
    }
}
