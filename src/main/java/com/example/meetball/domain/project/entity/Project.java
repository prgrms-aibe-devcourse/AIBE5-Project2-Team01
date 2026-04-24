package com.example.meetball.domain.project.entity;

import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.profile.entity.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "project")
public class Project {

    public static final String RECRUIT_STATUS_OPEN = "OPEN";
    public static final String RECRUIT_STATUS_CLOSED = "CLOSED";
    public static final String PROGRESS_STATUS_READY = "READY";
    public static final String PROGRESS_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String PROGRESS_STATUS_COMPLETED = "COMPLETED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_profile_id", nullable = false)
    private Profile ownerProfile;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(name = "project_type", nullable = false, length = 30)
    private String projectType;

    @Column(name = "project_purpose", length = 30)
    private String projectPurpose;

    @Column(name = "work_method", nullable = false, length = 30)
    private String workMethod;

    @Column(name = "tab_type", nullable = false, length = 20)
    private String tabType = "RECRUIT";

    @Column(name = "required_member", nullable = false)
    private Integer requiredMember;

    @Column(name = "recruit_start_date", nullable = false)
    private LocalDate recruitStartDate;

    @Column(name = "recruit_end_date", nullable = false)
    private LocalDate recruitEndDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "reference_url", length = 255)
    private String referenceUrl;

    @Column(name = "recruit_status", nullable = false, length = 20)
    private String recruitStatus = RECRUIT_STATUS_OPEN;

    @Column(name = "progress_status", nullable = false, length = 20)
    private String progressStatus = PROGRESS_STATUS_READY;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @Column(name = "is_public", nullable = false)
    private boolean publicProject = true;

    @Column(name = "bookmark_count", nullable = false)
    private int bookmarkCount;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProjectTechStack> techStackSelections = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ProjectRecruitPosition> positionSelections = new ArrayList<>();

    protected Project() {
    }

    public Project(String title, String description, String projectType, String progressMethod,
                   Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                   LocalDate projectStartAt, LocalDate projectEndAt, String recruitStatus, String progressStatus,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType == null || projectType.isBlank() ? "사이드 프로젝트" : projectType;
        this.workMethod = progressMethod == null || progressMethod.isBlank() ? "ONLINE" : progressMethod;
        this.requiredMember = recruitmentCount == null ? 0 : recruitmentCount;
        this.recruitStartDate = recruitmentStartAt != null ? recruitmentStartAt : LocalDate.now();
        this.recruitEndDate = recruitmentEndAt != null ? recruitmentEndAt : this.recruitStartDate.plusDays(14);
        this.startDate = projectStartAt;
        this.endDate = projectEndAt;
        this.recruitStatus = normalizeRecruitStatus(recruitStatus);
        this.progressStatus = normalizeProgressStatus(progressStatus);
        if (PROGRESS_STATUS_COMPLETED.equals(this.progressStatus)) {
            this.recruitStatus = RECRUIT_STATUS_CLOSED;
        }
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt;
    }

    public void assignOwner(Profile ownerProfile) {
        this.ownerProfile = Objects.requireNonNull(ownerProfile, "ownerProfile");
    }

    public void update(String title, String description, String projectType, String progressMethod,
                       Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                       LocalDate projectStartAt, LocalDate projectEndAt, String recruitStatus, String progressStatus,
                       LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType == null || projectType.isBlank() ? this.projectType : projectType;
        this.workMethod = progressMethod == null || progressMethod.isBlank() ? this.workMethod : progressMethod;
        this.requiredMember = recruitmentCount == null ? this.requiredMember : recruitmentCount;
        this.recruitStartDate = recruitmentStartAt != null ? recruitmentStartAt : this.recruitStartDate;
        this.recruitEndDate = recruitmentEndAt != null ? recruitmentEndAt : this.recruitEndDate;
        this.startDate = projectStartAt != null ? projectStartAt : this.startDate;
        this.endDate = projectEndAt;
        if (recruitStatus != null) {
            this.recruitStatus = normalizeRecruitStatus(recruitStatus);
        }
        if (progressStatus != null) {
            this.progressStatus = normalizeProgressStatus(progressStatus);
        }
        if (isCompleted()) {
            this.recruitStatus = RECRUIT_STATUS_CLOSED;
        }
        this.updatedAt = updatedAt;
    }

    public void updateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    public void replacePositions(List<ProjectSelectionCatalog.PositionCapacity> positions,
                                 java.util.function.Function<String, Position> positionResolver) {
        List<ProjectSelectionCatalog.PositionCapacity> requested = positions == null ? List.of() : positions;
        List<Long> requestedIds = requested.stream()
                .map(position -> positionResolver.apply(position.name()))
                .filter(Objects::nonNull)
                .map(Position::getId)
                .toList();

        this.positionSelections.removeIf(existing -> !requestedIds.contains(existing.getPositionId()));
        for (ProjectSelectionCatalog.PositionCapacity position : requested) {
            Position positionEntity = positionResolver.apply(position.name());
            ProjectRecruitPosition existing = this.positionSelections.stream()
                    .filter(current -> Objects.equals(positionEntity.getId(), current.getPositionId()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                this.positionSelections.add(new ProjectRecruitPosition(this, positionEntity, position.capacity()));
            } else {
                existing.updateCapacity(position.capacity());
            }
        }
    }

    public void replaceTechStacks(List<TechStack> techStacks) {
        List<TechStack> requestedTechStacks = normalizeTechStacks(techStacks);
        List<Long> requestedIds = requestedTechStacks.stream().map(TechStack::getId).toList();
        this.techStackSelections.removeIf(existing -> !requestedIds.contains(existing.getTechStackId()));

        for (TechStack techStack : requestedTechStacks) {
            boolean alreadySelected = this.techStackSelections.stream()
                    .anyMatch(existing -> Objects.equals(techStack.getId(), existing.getTechStackId()));
            if (!alreadySelected) {
                this.techStackSelections.add(new ProjectTechStack(this, techStack));
            }
        }
    }

    private List<TechStack> normalizeTechStacks(List<TechStack> techStacks) {
        if (techStacks == null) {
            return List.of();
        }
        return new ArrayList<>(techStacks.stream()
                .filter(Objects::nonNull)
                .filter(techStack -> techStack.getId() != null)
                .collect(Collectors.toMap(TechStack::getId, techStack -> techStack, (left, right) -> left, java.util.LinkedHashMap::new))
                .values());
    }

    public void incrementCurrentRecruitment() {
        ProjectRecruitPosition target = positionSelections.stream()
                .filter(position -> position.getApprovedUser() < position.getCapacity())
                .findFirst()
                .orElse(null);
        if (target == null) {
            throw new IllegalStateException("Recruitment count is already full.");
        }
        target.incrementApprovedUser();
    }

    public void decrementCurrentRecruitment() {
        positionSelections.stream()
                .filter(position -> position.getApprovedUser() > 0)
                .reduce((first, second) -> second)
                .ifPresent(ProjectRecruitPosition::decrementApprovedUser);
    }

    public boolean isRecruitmentFull() {
        Integer capacity = getTotalRecruitment();
        return capacity != null && capacity > 0 && getCurrentRecruitment() >= capacity;
    }

    private static String normalizeRecruitStatus(String recruitStatus) {
        if (recruitStatus == null || recruitStatus.isBlank()) {
            return RECRUIT_STATUS_OPEN;
        }
        return RECRUIT_STATUS_CLOSED.equalsIgnoreCase(recruitStatus) ? RECRUIT_STATUS_CLOSED : RECRUIT_STATUS_OPEN;
    }

    private static String normalizeProgressStatus(String progressStatus) {
        if (progressStatus == null || progressStatus.isBlank()) {
            return PROGRESS_STATUS_READY;
        }
        if (PROGRESS_STATUS_COMPLETED.equalsIgnoreCase(progressStatus)) {
            return PROGRESS_STATUS_COMPLETED;
        }
        if (PROGRESS_STATUS_IN_PROGRESS.equalsIgnoreCase(progressStatus)) {
            return PROGRESS_STATUS_IN_PROGRESS;
        }
        return PROGRESS_STATUS_READY;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() {
        if (description == null) {
            return "";
        }
        return description.length() > 50 ? description.substring(0, 50) : description;
    }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getPosition() {
        return positionSelections.stream()
                .map(position -> position.getPositionName() + ":" + position.getCapacity())
                .collect(Collectors.joining(", "));
    }
    public String getProgressMethod() { return workMethod; }
    public String getLeaderName() { return ownerProfile != null ? ownerProfile.getNickname() : ""; }
    public String getLeaderRole() { return "LEADER"; }
    public String getLeaderAvatarUrl() { return ownerProfile != null ? ownerProfile.getProfileImage() : ""; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getCurrentRecruitment() {
        return positionSelections.stream().mapToInt(ProjectRecruitPosition::getApprovedUser).sum();
    }
    public Integer getTotalRecruitment() { return requiredMember; }
    public Integer getRecruitmentCount() { return requiredMember; }
    public LocalDate getRecruitmentStartAt() { return recruitStartDate; }
    public LocalDate getRecruitmentDeadline() { return recruitEndDate; }
    public LocalDate getRecruitmentEndAt() { return recruitEndDate; }
    public LocalDate getProjectStartAt() { return startDate; }
    public LocalDate getProjectEndAt() { return endDate; }
    public String getRecruitStatus() { return recruitStatus; }
    public String getProgressStatus() { return progressStatus; }
    public boolean isCompleted() { return PROGRESS_STATUS_COMPLETED.equals(progressStatus); }
    public LocalDate getCreatedDate() { return createdAt != null ? createdAt.toLocalDate() : null; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public List<ProjectTechStack> getTechStackSelections() { return techStackSelections; }
    public List<ProjectRecruitPosition> getPositionSelections() { return positionSelections; }
    public Profile getOwnerProfile() { return ownerProfile; }
    public int getBookmarkCount() { return bookmarkCount; }
    public int getViewCount() { return viewCount; }
    public void incrementBookmarkCount() { this.bookmarkCount++; }
    public void decrementBookmarkCount() { this.bookmarkCount = Math.max(0, this.bookmarkCount - 1); }
    public void incrementViewCount() { this.viewCount++; }
}
