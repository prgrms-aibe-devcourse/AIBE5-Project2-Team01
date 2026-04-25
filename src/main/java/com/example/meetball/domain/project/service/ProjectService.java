package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.dto.ProjectCreateRequestDto;
import com.example.meetball.domain.project.dto.ProjectDetailResponseDto;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.dto.ProjectListResponseDto;
import com.example.meetball.domain.project.dto.ProjectParticipantProfile;
import com.example.meetball.domain.project.dto.ProjectPositionEditConstraint;
import com.example.meetball.domain.project.dto.ProjectRecruitPositionStatus;
import com.example.meetball.domain.project.dto.ProjectUpdateRequestDto;
import com.example.meetball.domain.bookmarkedproject.entity.BookmarkedProject;
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
import com.example.meetball.domain.project.support.ProjectOptionCatalog;
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
import java.util.Comparator;
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

    public ProjectDetailView getProjectDetail(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id));
        return toDetailView(project);
    }

    private ProjectDetailView toDetailView(Project project) {
        List<ProjectParticipant> participants = projectParticipantRepository.findByProject(project);
        int currentRecruitment = calculateCurrentRecruitment(project, participants);
        return new ProjectDetailView(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                displayProjectPurpose(project.getProjectPurpose()),
                displayWorkMethod(project.getWorkMethod()),
                formatPositionText(project),
                resolveLeaderProfileId(project, participants),
                project.getLeaderName(),
                project.getLeaderRole(),
                resolveLeaderPosition(project, participants),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                currentRecruitment,
                valueOrZero(project.getTotalRecruitment()),
                calculateProgressPercent(currentRecruitment, project.getTotalRecruitment()),
                formatRecruitmentDeadline(project, project.getRecruitmentDeadline()),
                project.getRecruitStatus(),
                project.getProgressStatus(),
                project.getCreatedDate() != null ? project.getCreatedDate().format(DATE_FORMATTER) : "",
                formatPeriod(project.getRecruitmentStartAt(), project.getRecruitmentEndAt() != null ? project.getRecruitmentEndAt() : project.getRecruitmentDeadline()),
                formatPeriod(project.getProjectStartAt(), project.getProjectEndAt()),
                splitTechStacks(project),
                buildPositionStatuses(project, participants),
                buildTeamMembers(project, participants),
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

    private String formatRecruitmentDeadline(Project project, LocalDate deadline) {
        if (project != null && Project.PROGRESS_STATUS_COMPLETED.equals(project.getProgressStatus())) {
            return "완료";
        }
        if (project != null && Project.RECRUIT_STATUS_CLOSED.equals(project.getRecruitStatus())) {
            return "마감";
        }
        return formatDeadline(deadline);
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

    public Page<ProjectListResponseDto> getProjects(String keyword, String projectPurpose,
                                                    String position, String techStack,
                                                    boolean bookmarkedOnly, Pageable pageable,
                                                    Long viewerId) {
        Profile viewer = viewerId != null ? profileService.getProfileById(viewerId) : null;
        String normalizedPosition = normalizePositionFilter(position);
        List<String> normalizedTechStacks = normalizeTechStackFilterList(techStack);
        Page<Project> projects = projectRepository.findAll(
                buildProjectFilterSpec(
                        cleanText(keyword),
                        normalizeProjectPurposeFilter(projectPurpose),
                        normalizedPosition,
                        normalizedTechStacks,
                        bookmarkedOnly,
                        viewer
                ),
                pageable
        );

        return projects.map(project -> new ProjectListResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getThumbnailUrl(),
                calculateCurrentRecruitment(project, projectParticipantRepository.findByProject(project)),
                project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount(),
                displayProjectPurpose(project.getProjectPurpose()),
                displayWorkMethod(project.getWorkMethod()),
                formatPositionText(project),
                splitTechStacks(project),
                project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt(),
                formatRecruitmentDeadline(project, project.getRecruitmentDeadline() != null ? project.getRecruitmentDeadline() : project.getRecruitmentEndAt()),
                project.getRecruitStatus(),
                project.getProgressStatus(),
                bookmarkedProjectRepository.countByProject(project),
                project.getViewCount(),
                viewer != null && bookmarkedProjectRepository.findByProjectAndProfile(project, viewer).isPresent(),
                project.getCreatedAt()
        ));
    }

    private Specification<Project> buildProjectFilterSpec(String keyword, String projectPurpose,
                                                          String position, List<String> techStacks,
                                                          boolean bookmarkedOnly, Profile viewer) {
        return (root, query, cb) -> {
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(cb.coalesce(root.get("title"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern)
                ));
            }

            if (projectPurpose != null && !projectPurpose.isBlank()) {
                Expression<String> storedPurpose = cb.lower(cb.coalesce(root.get("projectPurpose"), ""));
                List<Predicate> purposePredicates = ProjectOptionCatalog.projectPurposeSearchTokens(projectPurpose).stream()
                        .map(token -> cb.like(storedPurpose, "%" + token.toLowerCase(Locale.ROOT) + "%"))
                        .toList();
                predicates.add(cb.or(purposePredicates.toArray(Predicate[]::new)));
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

            if (bookmarkedOnly) {
                if (viewer == null) {
                    predicates.add(cb.disjunction());
                } else {
                    var subquery = query.subquery(Long.class);
                    var bookmarkedRoot = subquery.from(BookmarkedProject.class);
                    subquery.select(bookmarkedRoot.get("project").get("id"));
                    subquery.where(cb.equal(bookmarkedRoot.get("profile").get("id"), viewer.getId()));
                    predicates.add(root.get("id").in(subquery));
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    public ProjectDetailResponseDto getProjectById(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + projectId));

        return toDetailResponse(project);
    }

    public List<ProjectPositionEditConstraint> getProjectPositionEditConstraints(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found with id: " + projectId));
        return buildPositionEditConstraints(project);
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
        validateMinimumRecruitment(totalRecruitment);
        validateLeaderPositionIncluded(ownerProfile, normalizedPositions);
        LocalDateTime now = LocalDateTime.now();
        Project project = new Project(
                request.getTitle(),
                request.getDescription(),
                normalizeProjectPurposeForStorage(request.getProjectPurpose()),
                normalizeWorkMethod(request.getWorkMethod()),
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
        String currentPositionText = formatPositionText(project);
        boolean positionsChanged = !Objects.equals(currentPositionText, normalizedPositions);
        if (positionsChanged) {
            validateMinimumRecruitment(totalRecruitment);
            validateLeaderPositionIncluded(project.getOwnerProfile(), normalizedPositions);
            validatePositionUpdate(project, normalizedPositions);
        }
        if (Project.PROGRESS_STATUS_COMPLETED.equalsIgnoreCase(request.getProgressStatus())
                && !Project.RECRUIT_STATUS_CLOSED.equals(project.getRecruitStatus())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "프로젝트 완료는 모집 마감 이후에만 가능합니다.");
        }

        project.update(
                request.getTitle(),
                request.getDescription(),
                normalizeProjectPurposeForStorage(request.getProjectPurpose()),
                normalizeWorkMethod(request.getWorkMethod()),
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
        String currentPositionText = formatPositionText(project);
        if (Project.RECRUIT_STATUS_CLOSED.equals(project.getRecruitStatus())
                && !Objects.equals(currentPositionText, normalizedPositions)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "모집 마감 후에는 포지션을 추가, 수정, 삭제할 수 없습니다. 모집 마감 철회 후 다시 시도해주세요."
            );
        }
        List<ProjectApplication> applications = projectApplicationRepository.findByProject(project);
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
        Map<String, ProjectRecruitPosition> currentPositions = project.getPositionSelections().stream()
                .collect(Collectors.toMap(
                        ProjectRecruitPosition::getPositionName,
                        recruitPosition -> recruitPosition,
                        (left, right) -> left,
                        java.util.LinkedHashMap::new
                ));

        for (Map.Entry<String, ProjectRecruitPosition> entry : currentPositions.entrySet()) {
            String position = entry.getKey();
            ProjectRecruitPosition recruitPosition = entry.getValue();
            Integer nextCapacity = nextCapacities.get(position);
            boolean full = isRecruitPositionLocked(recruitPosition);
            long blockingCount = applicationCounts.getOrDefault(position, 0L);

            if (full) {
                if (nextCapacity == null) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "모집이 완료된 포지션은 삭제할 수 없습니다: " + position
                    );
                }
                if (nextCapacity != recruitPosition.getCapacity()) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "모집이 완료된 포지션은 수정할 수 없습니다: " + position
                    );
                }
                continue;
            }

            if (blockingCount > 0 && nextCapacity == null) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "지원자가 있는 포지션은 삭제할 수 없습니다: " + position
                );
            }
            if (blockingCount > 0 && nextCapacity != null && blockingCount > nextCapacity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "지원 인원보다 적게 모집 인원을 줄일 수 없습니다: " + position
                );
            }
        }
    }

    private void validateMinimumRecruitment(int totalRecruitment) {
        if (totalRecruitment < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 모집 인원은 최소 2명 이상이어야 합니다.");
        }
    }

    private void validateLeaderPositionIncluded(Profile ownerProfile, String normalizedPositions) {
        if (ownerProfile == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트 팀장 정보를 확인할 수 없습니다.");
        }
        String leaderPosition = normalizeProjectPosition(ownerProfile.getPosition());
        if (leaderPosition == null || leaderPosition.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "프로젝트를 등록하려면 먼저 팀장 포지션을 설정해주세요.");
        }
        boolean included = ProjectSelectionCatalog.parsePositionCapacities(normalizedPositions, null).stream()
                .anyMatch(position -> leaderPosition.equals(position.name()));
        if (!included) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "팀장의 포지션은 모집 포지션에 반드시 포함되어야 합니다.");
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

    private List<ProjectRecruitPositionStatus> buildPositionStatuses(Project project, List<ProjectParticipant> participants) {
        List<ProjectSelectionCatalog.PositionCapacity> capacities = positionCapacities(project);
        if (capacities.isEmpty()) {
            return List.of();
        }
        Map<String, Long> participantCounts = participants.stream()
                .map(this::participantPositionName)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        java.util.function.Function.identity(),
                        Collectors.counting()
                ));

        return capacities.stream()
                .map(position -> {
                    int current = participantCounts.getOrDefault(position.name(), 0L).intValue();
                    int capacity = position.capacity();
                    return new ProjectRecruitPositionStatus(position.name(), current, capacity, current >= capacity);
                })
                .toList();
    }

    private int calculateCurrentRecruitment(Project project, List<ProjectParticipant> participants) {
        return buildPositionStatuses(project, participants).stream()
                .mapToInt(ProjectRecruitPositionStatus::current)
                .sum();
    }

    private List<ProjectPositionEditConstraint> buildPositionEditConstraints(Project project) {
        Map<String, Long> blockingCounts = projectApplicationRepository.findByProject(project).stream()
                .filter(application -> application.getStatus() != null && application.getStatus().blocksPositionRemoval())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));
        Map<String, Long> acceptedCounts = projectApplicationRepository.findByProject(project).stream()
                .filter(application -> application.getStatus() != null && application.getStatus().isAccepted())
                .collect(Collectors.groupingBy(
                        this::applicationPositionName,
                        Collectors.counting()
                ));

        return project.getPositionSelections().stream()
                .map(position -> {
                    String positionName = position.getPositionName();
                    int appliedCount = blockingCounts.getOrDefault(positionName, 0L).intValue();
                    int acceptedCount = acceptedCounts.getOrDefault(positionName, 0L).intValue();
                    boolean full = isRecruitPositionLocked(position);
                    boolean nameLocked = full || appliedCount > 0;
                    boolean capacityLocked = full;
                    boolean deleteLocked = full || appliedCount > 0;
                    int minimumCapacity = appliedCount;
                    String message = full
                            ? "모집이 완료된 포지션은 수정 및 삭제가 불가합니다."
                            : appliedCount > 0
                            ? "지원자가 있는 포지션은 이름 변경/삭제가 불가하며, 인원은 지원 인원 이상으로만 유지할 수 있습니다."
                            : "";
                    return new ProjectPositionEditConstraint(
                            positionName,
                            appliedCount,
                            acceptedCount,
                            position.getCapacity(),
                            minimumCapacity,
                            full,
                            nameLocked,
                            capacityLocked,
                            deleteLocked,
                            message
                    );
                })
                .toList();
    }

    private boolean isRecruitPositionLocked(ProjectRecruitPosition recruitPosition) {
        return ProjectRecruitPosition.STATUS_CLOSED.equalsIgnoreCase(recruitPosition.getRecruitStatus())
                || recruitPosition.getApprovedUser() >= recruitPosition.getCapacity();
    }

    public List<ParticipatedProjectResponse> getParticipatedProjects(Profile profile) {
        return projectParticipantRepository.findByProfile(profile).stream()
                .map(participant -> {
                    Project project = participant.getProject();
                    boolean canProjectReview = project.isCompleted()
                            && "MEMBER".equals(participant.getRole())
                            && !projectReviewRepository.existsByProjectAndReviewer(project, profile);
                    boolean canPeerReview = project.isCompleted()
                            && "MEMBER".equals(participant.getRole())
                            && projectParticipantRepository.findByProject(project).stream()
                            .map(ProjectParticipant::getProfile)
                            .filter(Objects::nonNull)
                            .filter(memberProfile -> !Objects.equals(memberProfile.getId(), profile.getId()))
                            .count() > 0
                            && peerReviewRepository.countByProjectAndReviewer(project, profile) == 0;
                    Long dDay = project.getRecruitmentDeadline() != null
                            ? ChronoUnit.DAYS.between(LocalDate.now(), project.getRecruitmentDeadline())
                            : null;
                    String statusLabel = project.isCompleted() ? "COMPLETED" : "PROCEEDING";
                    LocalDateTime appliedAt = projectApplicationRepository.findAllByProjectAndProfile(project, profile).stream()
                            .map(ProjectApplication::getCreatedAt)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .orElse(null);
                    return ParticipatedProjectResponse.builder()
                            .projectId(project.getId())
                            .title(project.getTitle())
                            .participantRole(participant.getRole())
                            .status(statusLabel)
                            .canReview(canProjectReview || canPeerReview)
                            .canProjectReview(canProjectReview)
                            .canPeerReview(canPeerReview)
                            .dDay(dDay)
                            .recruitStatus(project.getRecruitStatus())
                            .progressStatus(project.getProgressStatus())
                            .createdAt(project.getCreatedAt())
                            .appliedAt(appliedAt)
                            .projectEndAt(project.getProjectEndAt())
                            .build();
                })
                .sorted(Comparator
                        .comparing((ParticipatedProjectResponse response) -> {
                            if ("COMPLETED".equals(response.getProgressStatus())) {
                                return response.getProjectEndAt() != null ? response.getProjectEndAt().atStartOfDay() : LocalDateTime.MIN;
                            }
                            if ("LEADER".equals(response.getParticipantRole())) {
                                return response.getCreatedAt() != null ? response.getCreatedAt() : LocalDateTime.MIN;
                            }
                            return response.getAppliedAt() != null ? response.getAppliedAt() : LocalDateTime.MIN;
                        })
                        .reversed())
                .toList();
    }

    private ProjectDetailResponseDto toDetailResponse(Project project) {
        List<ProjectParticipant> participants = projectParticipantRepository.findByProject(project);
        List<String> techStacks = splitTechStacks(project);
        Integer totalRecruitment = project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount();
        int currentRecruitment = calculateCurrentRecruitment(project, participants);
        return new ProjectDetailResponseDto(
                project.getId(),
                project.getTitle(),
                project.getSummary(),
                project.getDescription(),
                displayProjectPurpose(project.getProjectPurpose()),
                displayWorkMethod(project.getWorkMethod()),
                formatPositionText(project),
                resolveLeaderProfileId(project),
                project.getLeaderName(),
                project.getLeaderRole(),
                project.getLeaderAvatarUrl(),
                project.getThumbnailUrl(),
                currentRecruitment,
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
                buildPositionStatuses(project, participants),
                project.getViewCount()
        );
    }

    private Long resolveLeaderProfileId(Project project) {
        return resolveLeaderProfileId(project, projectParticipantRepository.findByProject(project));
    }

    private Long resolveLeaderProfileId(Project project, List<ProjectParticipant> participants) {
        if (project.getOwnerProfile() != null && project.getOwnerProfile().getId() != null) {
            return project.getOwnerProfile().getId();
        }
        Long leaderProfileId = participants.stream()
                .filter(participant -> "LEADER".equals(participant.getRole()))
                .map(ProjectParticipant::getProfile)
                .filter(Objects::nonNull)
                .map(Profile::getId)
                .findFirst()
                .orElse(null);
        return leaderProfileId;
    }

    private String resolveLeaderPosition(Project project, List<ProjectParticipant> participants) {
        String leaderPosition = participants.stream()
                .filter(participant -> "LEADER".equals(participant.getRole()))
                .map(this::participantPositionName)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        if (leaderPosition != null) {
            return leaderPosition;
        }
        if (project.getOwnerProfile() == null) {
            return "";
        }
        return normalizeProjectPosition(project.getOwnerProfile().getPosition());
    }

    private List<ProjectParticipantProfile> buildTeamMembers(Project project, List<ProjectParticipant> participants) {
        List<ProjectParticipantProfile> members = participants.stream()
                .filter(participant -> participant.getProfile() != null)
                .sorted((left, right) -> roleOrder(left.getRole()) - roleOrder(right.getRole()))
                .map(participant -> new ProjectParticipantProfile(
                        participant.getProfile().getId(),
                        participant.getProfile().getNickname(),
                        participant.getRole(),
                        fallbackPosition(participantPositionName(participant), participant.getProfile().getPosition())
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
                normalizeProjectPosition(project.getOwnerProfile().getPosition())
        ));
    }

    private String participantPositionName(ProjectParticipant participant) {
        if (participant == null) {
            return null;
        }
        ProjectRecruitPosition recruitPosition = participant.getRecruitPosition();
        if (recruitPosition != null && recruitPosition.getPositionName() != null && !recruitPosition.getPositionName().isBlank()) {
            return recruitPosition.getPositionName();
        }
        return normalizeProjectPosition(participant.getProfile() != null ? participant.getProfile().getPosition() : null);
    }

    private String normalizeProjectPosition(String positionName) {
        if (positionName == null || positionName.isBlank()) {
            return null;
        }
        try {
            return ProjectSelectionCatalog.positionName(positionName);
        } catch (IllegalArgumentException exception) {
            return positionName;
        }
    }

    private String fallbackPosition(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return normalizeProjectPosition(fallback);
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

    private String normalizeProjectPurposeFilter(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        try {
            return ProjectOptionCatalog.normalizeProjectPurpose(cleaned);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private String normalizeProjectPurposeForStorage(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        try {
            return ProjectOptionCatalog.normalizeProjectPurpose(cleaned);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private String displayProjectPurpose(String value) {
        return ProjectOptionCatalog.displayProjectPurpose(value);
    }

    private String normalizeWorkMethod(String value) {
        String cleaned = cleanText(value);
        if (cleaned == null || cleaned.isEmpty()) {
            return cleaned;
        }
        try {
            return ProjectOptionCatalog.normalizeWorkMethod(cleaned);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }

    private String displayWorkMethod(String value) {
        return ProjectOptionCatalog.displayWorkMethod(value);
    }
}
