package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectMemberProfile;
import com.example.meetball.domain.project.dto.ProjectPositionStatus;
import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.attachment.service.AttachmentService;
import com.example.meetball.domain.bookmark.repository.BookmarkRepository;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.entity.ProjectPosition;
import com.example.meetball.domain.project.entity.ProjectTechStack;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.projectread.repository.ProjectReadRepository;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.meetball.domain.user.service.UserService;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProjectService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ReviewRepository reviewRepository;
    private final ApplicationRepository applicationRepository;
    private final BookmarkRepository bookmarkRepository;
    private final AttachmentService attachmentService;
    private final CommentRepository commentRepository;
    private final ProjectReadRepository projectReadRepository;
    private final UserService userService;
    private final UserRepository userRepository;

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
                displayProjectType(project.getProjectType()),
                formatPositionText(project),
                project.getLeaderName(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                valueOrZero(project.getTotalRecruitment()),
                formatDeadline(project.getRecruitmentDeadline()),
                splitTechStacks(project)
        );
    }

    private ProjectDetailView toDetailView(Project project) {
        return new ProjectDetailView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                displayProjectType(project.getProjectType()),
                displayProgressMethod(project.getProgressMethod()),
                formatPositionText(project),
                resolveLeaderUserId(project),
                project.getLeaderName(),
                project.getLeaderRole(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                valueOrZero(project.getTotalRecruitment()),
                calculateProgressPercent(project.getCurrentRecruitment(), project.getTotalRecruitment()),
                formatDeadline(project.getRecruitmentDeadline()),
                project.getCreatedDate() != null ? project.getCreatedDate().format(DATE_FORMATTER) : "",
                formatPeriod(project.getRecruitmentStartAt(), project.getRecruitmentEndAt() != null ? project.getRecruitmentEndAt() : project.getRecruitmentDeadline()),
                formatPeriod(project.getProjectStartAt(), project.getProjectEndAt()),
                splitTechStacks(project),
                buildPositionStatuses(project),
                buildTeamMembers(project),
                projectReadRepository.countByProject(project)
        );
    }

    private String formatPeriod(LocalDate start, LocalDate end) {
        if (start == null && end == null) {
            return "-";
        }
        String startText = start != null ? start.format(DATE_FORMATTER) : "-";
        String endText = end != null ? end.format(DATE_FORMATTER) : "-";
        return startText + " ~ " + endText;
    }

    private List<String> splitTechStacks(Project project) {
        if (project.getTechStackSelections() != null && !project.getTechStackSelections().isEmpty()) {
            return project.getTechStackSelections().stream()
                    .map(ProjectTechStack::getTechStackName)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

    private List<ProjectSelectionCatalog.PositionCapacity> positionCapacities(Project project) {
        if (project.getPositionSelections() != null && !project.getPositionSelections().isEmpty()) {
            return project.getPositionSelections().stream()
                    .map(position -> new ProjectSelectionCatalog.PositionCapacity(
                            position.getPositionName(),
                            position.getCapacity()
                    ))
                    .toList();
        }
        return ProjectSelectionCatalog.parsePositionCapacities(
                project.getPosition(),
                project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount()
        );
    }

    private String formatPositionText(Project project) {
        List<ProjectSelectionCatalog.PositionCapacity> positions = positionCapacities(project);
        return positions.isEmpty()
                ? ""
                : positions.stream()
                .map(position -> position.name() + ":" + position.capacity())
                .collect(Collectors.joining(", "));
    }

    private String applicationPositionName(Application application) {
        ProjectPosition projectPosition = application.getProjectPosition();
        if (projectPosition != null && projectPosition.getPositionName() != null) {
            return projectPosition.getPositionName();
        }
        return ProjectSelectionCatalog.positionName(application.getPosition());
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
                                                    String techStack, Pageable pageable,
                                                    Long viewerId) {
        String normalizedPosition = normalizePositionFilter(position);
        List<String> normalizedTechStacks = normalizeTechStackFilterList(techStack);
        Page<Project> projects = projectRepository.findAll(
                buildProjectFilterSpec(
                        cleanText(keyword),
                        normalizeProjectTypeFilter(projectType),
                        normalizeProgressMethod(progressMethod),
                        normalizedPosition,
                        normalizedTechStacks
                ),
                pageable
        );
        User viewer = viewerId != null ? userService.getUserById(viewerId) : null;

        return projects.map(project -> new ProjectListResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount(),
                displayProjectType(project.getProjectType()),
                displayProgressMethod(project.getProgressMethod()),
                formatPositionText(project),
                splitTechStacks(project),
                project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt(),
                formatDeadline(project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt()),
                project.getClosed(),
                project.getCompleted(),
                bookmarkRepository.countByProject(project),
                projectReadRepository.countByProject(project),
                viewer != null && bookmarkRepository.findByProjectAndUser(project, viewer).isPresent(),
                project.getCreatedAt()
        ));
    }

    private Specification<Project> buildProjectFilterSpec(String keyword, String projectType,
                                                          String progressMethod, String position,
                                                          List<String> techStacks) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                Join<Project, ProjectPosition> keywordPositionJoin = root.join("positionSelections", JoinType.LEFT);
                Join<Project, ProjectTechStack> keywordTechJoin = root.join("techStackSelections", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(keywordPositionJoin.get("positionName"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(keywordTechJoin.get("techStackName"), "")), pattern)
                ));
            }

            if (projectType != null && !projectType.isBlank()) {
                predicates.add(cb.like(
                        cb.lower(cb.coalesce(root.get("projectType"), "")),
                        "%" + projectType.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            if (progressMethod != null && !progressMethod.isBlank()) {
                String progress = progressMethod.toLowerCase(Locale.ROOT);
                Expression<String> storedProgress = cb.lower(cb.coalesce(root.get("progressMethod"), ""));
                predicates.add(cb.or(
                        cb.equal(storedProgress, progress),
                        cb.and(cb.equal(cb.literal(progressMethod), "ONLINE"),
                                cb.or(cb.like(storedProgress, "%online%"), cb.like(cb.coalesce(root.get("progressMethod"), ""), "%온라인%"))),
                        cb.and(cb.equal(cb.literal(progressMethod), "OFFLINE"),
                                cb.or(cb.like(storedProgress, "%offline%"), cb.like(cb.coalesce(root.get("progressMethod"), ""), "%오프라인%"))),
                        cb.and(cb.equal(cb.literal(progressMethod), "HYBRID"),
                                cb.or(cb.like(storedProgress, "%hybrid%"),
                                        cb.like(cb.coalesce(root.get("progressMethod"), ""), "%혼합%"),
                                        cb.like(cb.coalesce(root.get("progressMethod"), ""), "%온/오프%")))
                ));
            }

            if (position != null && !position.isBlank()) {
                Join<Project, ProjectPosition> positionJoin = root.join("positionSelections", JoinType.LEFT);
                predicates.add(cb.equal(positionJoin.get("positionName"), position));
            }

            if (techStacks != null && !techStacks.isEmpty()) {
                Join<Project, ProjectTechStack> techJoin = root.join("techStackSelections", JoinType.LEFT);
                List<Predicate> techPredicates = techStacks.stream()
                        .map(tech -> cb.equal(techJoin.get("techStackName"), tech))
                        .toList();
                predicates.add(cb.or(techPredicates.toArray(Predicate[]::new)));
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public ProjectDetailResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        return toDetailResponse(project);
    }

    @Transactional
    public ProjectDetailResponseDto createProject(ProjectCreateRequestDto request, Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        User user = userService.getUserById(userId);
        String leaderNickname = user.getNickname();

        String normalizedPositions = normalizePositionText(request.getPosition());
        List<String> normalizedTechStacks = normalizeTechStacks(request.getTechStacks());
        int totalRecruitment = ProjectSelectionCatalog.totalCapacity(normalizedPositions);
        LocalDateTime now = LocalDateTime.now();
        Project project = new Project(
                request.getTitle(),
                request.getDescription(),
                normalizeProjectTypeForStorage(request.getProjectType()),
                normalizeProgressMethod(request.getProgressMethod()),
                totalRecruitment,
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                Boolean.TRUE.equals(request.getClosed()),
                Boolean.TRUE.equals(request.getCompleted()),
                now,
                now
        );

        project.setLeaderName(leaderNickname);
        project.updateDiscoveryFields(
                normalizedPositions,
                normalizedTechStacks,
                cleanText(request.getThumbnailUrl())
        );
        Project savedProject = projectRepository.save(project);
        projectMemberRepository.save(ProjectMember.builder()
                .project(savedProject)
                .user(user)
                .role("LEADER")
                .build());

        return toDetailResponse(savedProject);
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

        String normalizedPositions = normalizePositionText(request.getPosition());
        List<String> normalizedTechStacks = normalizeTechStacks(request.getTechStacks());
        int totalRecruitment = ProjectSelectionCatalog.totalCapacity(normalizedPositions);
        validatePositionUpdate(project, normalizedPositions);

        project.update(
                request.getTitle(),
                request.getDescription(),
                normalizeProjectTypeForStorage(request.getProjectType()),
                normalizeProgressMethod(request.getProgressMethod()),
                totalRecruitment,
                request.getRecruitmentStartAt(),
                request.getRecruitmentEndAt(),
                request.getProjectStartAt(),
                request.getProjectEndAt(),
                request.getClosed(),
                request.getCompleted(),
                LocalDateTime.now()
        );
        project.updateDiscoveryFields(
                normalizedPositions,
                normalizedTechStacks,
                cleanText(request.getThumbnailUrl())
        );
        Project updatedProject = projectRepository.save(project);

        return toDetailResponse(updatedProject);
    }

    private void validatePositionUpdate(Project project, String normalizedPositions) {
        List<Application> applications = applicationRepository.findByProject(project);
        if (applications.isEmpty()) {
            return;
        }

        Map<String, Integer> nextCapacities = ProjectSelectionCatalog.parsePositionCapacities(normalizedPositions, null)
                .stream()
                .collect(Collectors.toMap(
                        ProjectSelectionCatalog.PositionCapacity::name,
                        ProjectSelectionCatalog.PositionCapacity::capacity,
                        Integer::sum
                ));

        Map<String, Long> applicationCounts = applications.stream()
                .filter(application -> application.getStatus() != null && application.getStatus().blocksPositionRemoval())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));
        for (String position : applicationCounts.keySet()) {
            if (!nextCapacities.containsKey(position)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot remove a position with existing applications: " + position
                );
            }
        }

        Map<String, Long> acceptedCounts = applications.stream()
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));
        for (Map.Entry<String, Long> entry : acceptedCounts.entrySet()) {
            int nextCapacity = nextCapacities.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > nextCapacity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Cannot reduce position capacity below accepted applicants: " + entry.getKey()
                );
            }
        }
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

        attachmentService.deleteProjectAttachments(projectId);
        commentRepository.deleteByProjectId(projectId);
        reviewRepository.deleteByProject(project);
        bookmarkRepository.deleteByProject(project);
        applicationRepository.deleteByProject(project);
        projectReadRepository.deleteByProject(project);
        projectMemberRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

    private boolean isProjectLeader(Project project, User user) {
        return projectMemberRepository.findByProjectAndUser(project, user)
                .map(projectMember -> "LEADER".equals(projectMember.getRole()))
                .orElse(false);
    }

    private String cleanText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed;
    }

    private String normalizePositionText(String value) {
        try {
            return ProjectSelectionCatalog.normalizePositionText(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private String normalizePositionFilter(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        try {
            return ProjectSelectionCatalog.positionName(cleaned);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private List<String> normalizeTechStacks(List<String> values) {
        try {
            return ProjectSelectionCatalog.normalizeTechStackNames(values);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private List<String> normalizeTechStackFilterList(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return List.of();
        }
        try {
            return ProjectSelectionCatalog.normalizeTechStackFilters(cleaned);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private List<ProjectPositionStatus> buildPositionStatuses(Project project) {
        List<ProjectSelectionCatalog.PositionCapacity> capacities = positionCapacities(project);
        if (capacities.isEmpty()) {
            return List.of();
        }
        Map<String, Long> acceptedCounts = applicationRepository.findByProject(project).stream()
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));

        return capacities.stream()
                .map(position -> {
                    int current = acceptedCounts.getOrDefault(position.name(), 0L).intValue();
                    int capacity = position.capacity();
                    return new ProjectPositionStatus(position.name(), current, capacity, current >= capacity);
                })
                .toList();
    }

    public List<ParticipatedProjectResponse> getParticipatedProjects(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .map(projectMember -> {
                    Project project = projectMember.getProject();
                    boolean canReview = project.isCompleted()
                            && !reviewRepository.existsByProjectAndReviewer(project, user)
                            && "MEMBER".equals(projectMember.getRole());
                    Long dDay = project.getRecruitmentDeadline() != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), project.getRecruitmentDeadline())
                            : null;
                    String statusLabel = project.isCompleted() ? "COMPLETED" : "PROCEEDING";
                    return ParticipatedProjectResponse.builder()
                            .projectId(project.getId())
                            .title(project.getTitle())
                            .userRole(projectMember.getRole())
                            .status(statusLabel)
                            .canReview(canReview)
                            .dDay(dDay)
                            .closed(Boolean.TRUE.equals(project.getClosed()))
                            .completed(project.isCompleted())
                            .build();
                })
                .toList();
    }

    private ProjectDetailResponseDto toDetailResponse(Project project) {
        List<String> techStacks = splitTechStacks(project);
        Integer totalRecruitment = project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount();
        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                displayProjectType(project.getProjectType()),
                displayProgressMethod(project.getProgressMethod()),
                formatPositionText(project),
                resolveLeaderUserId(project),
                project.getLeaderName(),
                project.getLeaderRole(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                valueOrZero(project.getCurrentRecruitment()),
                totalRecruitment,
                project.getRecruitmentCount(),
                project.getRecruitmentStartAt(),
                project.getRecruitmentEndAt(),
                project.getProjectStartAt(),
                project.getProjectEndAt(),
                Boolean.TRUE.equals(project.getClosed()),
                project.getCompleted(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                techStacks,
                projectReadRepository.countByProject(project)
        );
    }

    private Long resolveLeaderUserId(Project project) {
        Long leaderUserId = projectMemberRepository.findByProject(project).stream()
                .filter(projectMember -> "LEADER".equals(projectMember.getRole()))
                .map(ProjectMember::getUser)
                .filter(Objects::nonNull)
                .map(User::getId)
                .findFirst()
                .orElse(null);
        if (leaderUserId != null) {
            return leaderUserId;
        }
        String leaderName = cleanText(project.getLeaderName());
        if (leaderName == null || leaderName.isEmpty()) {
            return null;
        }
        return userRepository.findByNickname(leaderName)
                .map(User::getId)
                .orElse(null);
    }

    private List<ProjectMemberProfile> buildTeamMembers(Project project) {
        List<ProjectMemberProfile> members = projectMemberRepository.findByProject(project).stream()
                .filter(projectMember -> projectMember.getUser() != null)
                .sorted((left, right) -> roleOrder(left.getRole()) - roleOrder(right.getRole()))
                .map(projectMember -> new ProjectMemberProfile(
                        projectMember.getUser().getId(),
                        projectMember.getUser().getNickname(),
                        projectMember.getRole(),
                        projectMember.getUser().getJobTitle()
                ))
                .toList();
        if (!members.isEmpty()) {
            return members;
        }

        String leaderName = cleanText(project.getLeaderName());
        if (leaderName == null || leaderName.isEmpty()) {
            return List.of();
        }
        return userRepository.findByNickname(leaderName)
                .map(user -> List.of(new ProjectMemberProfile(
                        user.getId(),
                        user.getNickname(),
                        "LEADER",
                        user.getJobTitle()
                )))
                .orElse(List.of());
    }

    private int roleOrder(String role) {
        if ("LEADER".equals(role)) {
            return 0;
        }
        if ("MEMBER".equals(role)) {
            return 1;
        }
        return 2;
    }

    private String normalizeProjectTypeFilter(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        if (cleaned.contains("사이드")) {
            return "사이드";
        }
        if (cleaned.contains("스타트업")) {
            return "스타트업";
        }
        if (cleaned.contains("공모전") || cleaned.equalsIgnoreCase("COMPETITION")) {
            return "공모전";
        }
        if (cleaned.contains("스터디") || cleaned.equalsIgnoreCase("STUDY")) {
            return cleaned.equalsIgnoreCase("STUDY") ? "STUDY" : "스터디";
        }
        if (cleaned.contains("기업")) {
            return "기업";
        }
        return cleaned;
    }

    private String normalizeProjectTypeForStorage(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        if (cleaned.contains("사이드")) {
            return "사이드 프로젝트";
        }
        if (cleaned.contains("스타트업")) {
            return "스타트업";
        }
        if (cleaned.contains("공모전") || cleaned.equalsIgnoreCase("COMPETITION")) {
            return "공모전";
        }
        if (cleaned.contains("스터디") || cleaned.equalsIgnoreCase("STUDY")) {
            return "스터디";
        }
        if (cleaned.contains("기업")) {
            return "기업 연계";
        }
        return cleaned;
    }

    private String displayProjectType(String value) {
        String normalized = normalizeProjectTypeForStorage(value);
        return normalized == null ? "" : normalized;
    }

    private String normalizeProgressMethod(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        String lower = cleaned.toLowerCase();
        if (cleaned.contains("혼합") || cleaned.contains("온/오프") || lower.contains("hybrid")) {
            return "HYBRID";
        }
        if (cleaned.contains("오프라인") || lower.contains("offline")) {
            return "OFFLINE";
        }
        if (cleaned.contains("온라인") || lower.contains("online")) {
            return "ONLINE";
        }
        return cleaned;
    }

    private String displayProgressMethod(String value) {
        String normalized = normalizeProgressMethod(value);
        if ("ONLINE".equals(normalized)) {
            return "온라인";
        }
        if ("OFFLINE".equals(normalized)) {
            return "오프라인";
        }
        if ("HYBRID".equals(normalized)) {
            return "온/오프 혼합";
        }
        return normalized == null ? "" : normalized;
    }
}
