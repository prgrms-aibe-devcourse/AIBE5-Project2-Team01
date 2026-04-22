package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.meetball.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ReviewRepository reviewRepository;
    private final UserService userService;

    // --- MVC (front2) ---
    public List<ProjectSummaryView> getProjectSummaries() {
        return projectRepository.findAllByOrderByCreatedDateDescIdDesc()
                .stream()
                .map(this::toSummaryView)
                .toList();
    }

    public ProjectDetailView getProjectDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        return toDetailView(project);
    }

    private ProjectSummaryView toSummaryView(Project project) {
        return new ProjectSummaryView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getProjectType(),
                project.getPosition(),
                project.getLeaderName(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                valueOrZero(project.getTotalRecruitment()),
                formatDeadline(project.getRecruitmentDeadline()),
                splitTechStacks(project.getTechStackCsv())
        );
    }

    private ProjectDetailView toDetailView(Project project) {
        return new ProjectDetailView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                project.getProjectType(),
                project.getProgressMethod(),
                project.getPosition(),
                project.getLeaderName(),
                project.getLeaderRole(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                valueOrZero(project.getTotalRecruitment()),
                calculateProgressPercent(project.getCurrentRecruitment(), project.getTotalRecruitment()),
                formatDeadline(project.getRecruitmentDeadline()),
                project.getCreatedDate() != null ? project.getCreatedDate().format(DATE_FORMATTER) : "",
                splitTechStacks(project.getTechStackCsv())
        );
    }

    private List<String> splitTechStacks(String techStackCsv) {
        if (techStackCsv == null || techStackCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(techStackCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private String formatDeadline(LocalDate deadline) {
        if (deadline == null) {
            return "-";
        }
        long days = ChronoUnit.DAYS.between(LocalDate.now(), deadline);
        if (days < 0) {
            return "마감";
        }
        return "D-" + days;
    }

    private int calculateProgressPercent(Integer currentRecruitment, Integer totalRecruitment) {
        int current = valueOrZero(currentRecruitment);
        int total = valueOrZero(totalRecruitment);
        if (total <= 0) {
            return 0;
        }
        return Math.min(100, (current * 100) / total);
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    public Page<ProjectListResponseDto> getProjects(String keyword, String projectType,
                                                    String progressMethod, String position,
                                                    String techStack, Pageable pageable) {
        Page<Project> projects = projectRepository.findProjectsWithFilters(keyword, projectType, progressMethod, position, techStack, pageable);

        return projects.map(project -> new ProjectListResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getThumbnailUrl(),
                project.getCurrentRecruitment(),
                project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount(),
                project.getProjectType(),
                project.getProgressMethod(),
                project.getPosition(),
                splitTechStacks(project.getTechStackCsv()),
                project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt(),
                formatDeadline(project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt()),
                project.getClosed(),
                project.getCreatedAt()
        ));
    }

    public ProjectDetailResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getProjectType(),
                project.getProgressMethod(),
                project.getRecruitmentCount(),
                project.getRecruitmentStartAt(),
                project.getRecruitmentEndAt(),
                project.getProjectStartAt(),
                project.getProjectEndAt(),
                project.getClosed(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }

    @Transactional
    public ProjectDetailResponseDto createProject(ProjectCreateRequestDto request, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userService.getUserById(userId);
        String leaderNickname = user.getNickname();

        LocalDateTime now = LocalDateTime.now();
        Project project = new Project(
                request.getTitle(),
                request.getDescription(),
                request.getProjectType(),
                request.getProgressMethod(),
                request.getRecruitmentCount(),
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                request.getClosed(),
                now,
                now
        );

        project.setLeaderName(leaderNickname);

        Project savedProject = projectRepository.save(project);
        projectMemberRepository.save(ProjectMember.builder()
                .project(savedProject)
                .user(user)
                .role("LEADER")
                .build());

        return new ProjectDetailResponseDto(
                savedProject.getId(), savedProject.getTitle(), savedProject.getDescription(),
                savedProject.getProjectType(), savedProject.getProgressMethod(), savedProject.getRecruitmentCount(),
                savedProject.getRecruitmentStartAt(), savedProject.getRecruitmentEndAt(), savedProject.getProjectStartAt(),
                savedProject.getProjectEndAt(), savedProject.getClosed(), savedProject.getCreatedAt(), savedProject.getUpdatedAt()
        );
    }

    @Transactional
    public ProjectDetailResponseDto updateProject(Long projectId, ProjectUpdateRequestDto request, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userService.getUserById(userId);

        if (!isProjectLeader(project, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can update the project.");
        }

        project.update(
                request.getTitle(),
                request.getDescription(),
                request.getProjectType(),
                request.getProgressMethod(),
                request.getRecruitmentCount(),
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                request.getClosed(),
                LocalDateTime.now()
        );

        Project updatedProject = projectRepository.save(project);

        return new ProjectDetailResponseDto(
                updatedProject.getId(), updatedProject.getTitle(), updatedProject.getDescription(),
                updatedProject.getProjectType(), updatedProject.getProgressMethod(), updatedProject.getRecruitmentCount(),
                updatedProject.getRecruitmentStartAt(), updatedProject.getRecruitmentEndAt(), updatedProject.getProjectStartAt(),
                updatedProject.getProjectEndAt(), updatedProject.getClosed(), updatedProject.getCreatedAt(), updatedProject.getUpdatedAt()
        );
    }

    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userService.getUserById(userId);

        if (!isProjectLeader(project, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can delete the project.");
        }

        projectRepository.delete(project);
    }

    private boolean isProjectLeader(Project project, User user) {
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(projectMember -> "LEADER".equals(projectMember.getRole()))
                .orElse(false);
    }

    public List<ParticipatedProjectResponse> getParticipatedProjects(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .map(projectMember -> {
                    Project project = projectMember.getProject();
                    boolean canReview = project.getRecruitmentDeadline() != null
                            && !reviewRepository.existsByProjectAndReviewer(project, user)
                            && LocalDate.now().isAfter(project.getRecruitmentDeadline());
                    Long dDay = project.getRecruitmentDeadline() != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), project.getRecruitmentDeadline())
                            : null;
                    String statusLabel = Boolean.TRUE.equals(project.getClosed()) ? "COMPLETED" : "PROCEEDING";
                    return ParticipatedProjectResponse.builder()
                            .projectId(project.getId())
                            .title(project.getTitle())
                            .userRole(projectMember.getRole())
                            .status(statusLabel)
                            .canReview(canReview)
                            .dDay(dDay)
                            .closed(Boolean.TRUE.equals(project.getClosed()))
                            .build();
                })
                .toList();
    }
}
