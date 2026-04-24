package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectParticipantProfile;
import com.example.meetball.domain.project.dto.ProjectRecruitPositionStatus;
import com.example.meetball.domain.project.dto.ProjectSummaryView;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.repository.ProjectApplicationRepository;
import com.example.meetball.domain.projectresource.service.ProjectResourceService;
import com.example.meetball.domain.bookmarkedproject.repository.BookmarkedProjectRepository;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import com.example.meetball.domain.project.entity.ProjectTechStack;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.viewhistory.repository.ViewHistoryRepository;
import com.example.meetball.domain.review.repository.ProjectReviewRepository;
import com.example.meetball.domain.review.repository.PeerReviewRepository;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.meetball.domain.profile.service.ProfileService;
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
    private final ProjectParticipantRepository projectParticipantRepository;
    private final PeerReviewRepository peerReviewRepository;
    private final ProjectReviewRepository projectReviewRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final BookmarkedProjectRepository bookmarkedProjectRepository;
    private final ProjectResourceService projectResourceService;
    private final CommentRepository commentRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final ProfileService profileService;
    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;

    public List<ProjectSummaryView> getProjectSummaries() {
        return projectRepository.findAllByOrderByCreatedAtDescIdDesc()
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
                resolveLeaderProfileId(project),
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
                project.getViewCount()
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
        return List.of();
    }

    private String formatPositionText(Project project) {
        List<ProjectSelectionCatalog.PositionCapacity> positions = positionCapacities(project);
        return positions.isEmpty()
                ? ""
                : positions.stream()
                .map(position -> position.name() + ":" + position.capacity())
                .collect(Collectors.joining(", "));
    }

    private String applicationPositionName(ProjectApplication application) {
        ProjectRecruitPosition recruitPosition = application.getRecruitPosition();
        if (recruitPosition != null && recruitPosition.getPositionName() != null) {
            return recruitPosition.getPositionName();
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
        Profile viewer = viewerId != null ? profileService.getProfileById(viewerId) : null;

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
                project.getRecruitStatus(),
                project.getProgressStatus(),
                bookmarkedProjectRepository.countByProject(project),
                project.getViewCount(),
                viewer != null && bookmarkedProjectRepository.findByProjectAndProfile(project, viewer).isPresent(),
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
                Join<Project, ProjectRecruitPosition> keywordPositionJoin = root.join("positionSelections", JoinType.LEFT);
                Join<ProjectRecruitPosition, Position> keywordPositionValueJoin = keywordPositionJoin.join("position", JoinType.LEFT);
                Join<Project, ProjectTechStack> keywordTechJoin = root.join("techStackSelections", JoinType.LEFT);
                Join<ProjectTechStack, TechStack> keywordTechStackValueJoin = keywordTechJoin.join("techStack", JoinType.LEFT);
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(keywordPositionValueJoin.get("name"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(keywordTechStackValueJoin.get("name"), "")), pattern)
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
                Expression<String> storedProgress = cb.lower(cb.coalesce(root.get("workMethod"), ""));
                predicates.add(cb.or(
                        cb.equal(storedProgress, progress),
                        cb.and(cb.equal(cb.literal(progressMethod), "ONLINE"),
                                cb.or(cb.like(storedProgress, "%online%"), cb.like(cb.coalesce(root.get("workMethod"), ""), "%온라인%"))),
                        cb.and(cb.equal(cb.literal(progressMethod), "OFFLINE"),
                                cb.or(cb.like(storedProgress, "%offline%"), cb.like(cb.coalesce(root.get("workMethod"), ""), "%오프라인%"))),
                        cb.and(cb.equal(cb.literal(progressMethod), "HYBRID"),
                                cb.or(cb.like(storedProgress, "%hybrid%"),
                                        cb.like(cb.coalesce(root.get("workMethod"), ""), "%혼합%"),
                                        cb.like(cb.coalesce(root.get("workMethod"), ""), "%온/오프%")))
                ));
            }

            if (position != null && !position.isBlank()) {
                Join<Project, ProjectRecruitPosition> positionJoin = root.join("positionSelections", JoinType.LEFT);
                Join<ProjectRecruitPosition, Position> positionValueJoin = positionJoin.join("position", JoinType.LEFT);
                predicates.add(cb.equal(positionValueJoin.get("name"), position));
            }

            if (techStacks != null && !techStacks.isEmpty()) {
                Join<Project, ProjectTechStack> techJoin = root.join("techStackSelections", JoinType.LEFT);
                Join<ProjectTechStack, TechStack> techStackValueJoin = techJoin.join("techStack", JoinType.LEFT);
                List<Predicate> techPredicates = techStacks.stream()
                        .map(tech -> cb.equal(techStackValueJoin.get("name"), tech))
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
    public void recordProjectView(Long projectId, Long viewerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        project.incrementViewCount();
        if (viewerId == null) {
            return;
        }
        Profile viewer = profileService.getProfileById(viewerId);
        viewHistoryRepository.findByProjectAndProfile(project, viewer)
                .ifPresentOrElse(
                        viewHistoryRepository::save,
                        () -> viewHistoryRepository.save(com.example.meetball.domain.viewhistory.entity.ViewHistory.builder()
                                .project(project)
                                .profile(viewer)
                                .build())
                );
    }

    @Transactional
    public ProjectDetailResponseDto createProject(ProjectCreateRequestDto request, Long profileId) {
        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile ownerProfile = profileService.getProfileById(profileId);
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
                request.getRecruitStatus(),
                request.getProgressStatus(),
                now,
                now
        );

        project.assignOwner(ownerProfile);
        project.replacePositions(
                ProjectSelectionCatalog.parsePositionCapacities(normalizedPositions, totalRecruitment),
                this::resolvePosition
        );
        project.replaceTechStacks(resolveTechStacks(normalizedTechStacks));
        project.updateThumbnailUrl(cleanText(request.getThumbnailUrl()));
        Project savedProject = projectRepository.save(project);
        projectParticipantRepository.save(ProjectParticipant.builder()
                .project(savedProject)
                .profile(ownerProfile)
                .role("LEADER")
                .build());

        return toDetailResponse(savedProject);
    }

    @Transactional
    public ProjectDetailResponseDto updateProject(Long projectId, ProjectUpdateRequestDto request, Long profileId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile leaderProfile = profileService.getProfileById(profileId);

        if (!isProjectLeader(project, leaderProfile)) {
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
                request.getRecruitStatus(),
                request.getProgressStatus(),
                LocalDateTime.now()
        );
        project.replacePositions(
                ProjectSelectionCatalog.parsePositionCapacities(normalizedPositions, totalRecruitment),
                this::resolvePosition
        );
        project.replaceTechStacks(resolveTechStacks(normalizedTechStacks));
        project.updateThumbnailUrl(cleanText(request.getThumbnailUrl()));
        Project updatedProject = projectRepository.save(project);

        return toDetailResponse(updatedProject);
    }

    private void validatePositionUpdate(Project project, String normalizedPositions) {
        List<ProjectApplication> applications = projectApplicationRepository.findByProject(project);
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
    public void deleteProject(Long projectId, Long profileId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));

        if (profileId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required.");
        }

        Profile leaderProfile = profileService.getProfileById(profileId);

        if (!isProjectLeader(project, leaderProfile)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the project leader can delete the project.");
        }

        projectResourceService.deleteProjectResources(projectId);
        commentRepository.deleteByProjectId(projectId);
        peerReviewRepository.deleteByProject(project);
        projectReviewRepository.deleteByProject(project);
        bookmarkedProjectRepository.deleteByProject(project);
        projectApplicationRepository.deleteByProject(project);
        viewHistoryRepository.deleteByProject(project);
        projectParticipantRepository.deleteByProject(project);
        projectRepository.delete(project);
    }

    private boolean isProjectLeader(Project project, Profile profile) {
        return projectParticipantRepository.findByProjectAndProfile(project, profile)
                .map(participant -> "LEADER".equals(participant.getRole()))
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

    private Position resolvePosition(String positionName) {
        return positionRepository.findByName(positionName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported position: " + positionName));
    }

    private List<TechStack> resolveTechStacks(List<String> techStackNames) {
        return techStackNames.stream()
                .map(techStackName -> techStackRepository.findByName(techStackName)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported tech stack: " + techStackName)))
                .toList();
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

    private List<ProjectRecruitPositionStatus> buildPositionStatuses(Project project) {
        List<ProjectSelectionCatalog.PositionCapacity> capacities = positionCapacities(project);
        if (capacities.isEmpty()) {
            return List.of();
        }
        Map<String, Long> acceptedCounts = projectApplicationRepository.findByProject(project).stream()
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));

        return capacities.stream()
                .map(position -> {
                    int current = acceptedCounts.getOrDefault(position.name(), 0L).intValue();
                    int capacity = position.capacity();
                    return new ProjectRecruitPositionStatus(position.name(), current, capacity, current >= capacity);
                })
                .toList();
    }

    public List<ParticipatedProjectResponse> getParticipatedProjects(Profile profile) {
        return projectParticipantRepository.findByProfile(profile).stream()
                .map(participant -> {
                    Project project = participant.getProject();
                    boolean canReview = project.isCompleted()
                            && !projectReviewRepository.existsByProjectAndReviewer(project, profile)
                            && "MEMBER".equals(participant.getRole());
                    Long dDay = project.getRecruitmentDeadline() != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), project.getRecruitmentDeadline())
                            : null;
                    String statusLabel = project.isCompleted() ? "COMPLETED" : "PROCEEDING";
                    return ParticipatedProjectResponse.builder()
                            .projectId(project.getId())
                            .title(project.getTitle())
                            .participantRole(participant.getRole())
                            .status(statusLabel)
                            .canReview(canReview)
                            .dDay(dDay)
                            .recruitStatus(project.getRecruitStatus())
                            .progressStatus(project.getProgressStatus())
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
                resolveLeaderProfileId(project),
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
                project.getRecruitStatus(),
                project.getProgressStatus(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                techStacks,
                project.getViewCount()
        );
    }

    private Long resolveLeaderProfileId(Project project) {
        if (project.getOwnerProfile() != null && project.getOwnerProfile().getId() != null) {
            return project.getOwnerProfile().getId();
        }
        Long leaderProfileId = projectParticipantRepository.findByProject(project).stream()
                .filter(participant -> "LEADER".equals(participant.getRole()))
                .map(ProjectParticipant::getProfile)
                .filter(Objects::nonNull)
                .map(Profile::getId)
                .findFirst()
                .orElse(null);
        return leaderProfileId;
    }

    private List<ProjectParticipantProfile> buildTeamMembers(Project project) {
        List<ProjectParticipantProfile> members = projectParticipantRepository.findByProject(project).stream()
                .filter(participant -> participant.getProfile() != null)
                .sorted((left, right) -> roleOrder(left.getRole()) - roleOrder(right.getRole()))
                .map(participant -> new ProjectParticipantProfile(
                        participant.getProfile().getId(),
                        participant.getProfile().getNickname(),
                        participant.getRole(),
                        participant.getProfile().getPosition()
                ))
                .toList();
        if (!members.isEmpty()) {
            return members;
        }
        if (project.getOwnerProfile() == null) {
            return List.of();
        }
        return List.of(new ProjectParticipantProfile(
                project.getOwnerProfile().getId(),
                project.getOwnerProfile().getNickname(),
                "LEADER",
                project.getOwnerProfile().getPosition()
        ));
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
