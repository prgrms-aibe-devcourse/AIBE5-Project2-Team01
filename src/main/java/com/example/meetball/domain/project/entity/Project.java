package com.example.meetball.domain.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

@Entity
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(length = 120)
    private String title;

    @Column(length = 1000)
    private String summary;

    @Column(length = 4000)
    private String description;

    @Column(name = "project_type", length = 40)
    private String projectType;

    @Column(length = 1000)
    private String position;

    @Column(name = "progress_method")
    private String progressMethod;

    @Column(length = 60)
    private String leaderName;

    @Column(length = 60)
    private String leaderRole;

    private String leaderAvatarUrl;

    private String thumbnailUrl;

    private Integer currentRecruitment;

    private Integer totalRecruitment;

    @Column(name = "recruitment_count")
    private Integer recruitmentCount;

    @Column(name = "recruitment_start_at")
    private LocalDate recruitmentStartAt;

    private LocalDate recruitmentDeadline;

    @Column(name = "recruitment_end_at")
    private LocalDate recruitmentEndAt;

    @Column(name = "project_start_at")
    private LocalDate projectStartAt;

    @Column(name = "project_end_at")
    private LocalDate projectEndAt;

    private Boolean closed;

    private Boolean completed;

    private LocalDate createdDate;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String techStackCsv;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProjectTechStack> techStackSelections = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    private List<ProjectPosition> positionSelections = new ArrayList<>();

    protected Project() {
    }


    public Project(String title, String description, String projectType, String progressMethod,
                   Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                   LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this(title, description, projectType, progressMethod, recruitmentCount, recruitmentStartAt, recruitmentEndAt,
                projectStartAt, projectEndAt, closed, false, createdAt, updatedAt);
    }

    public Project(String title, String description, String projectType, String progressMethod,
                   Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                   LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed, Boolean completed,
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.recruitmentCount = recruitmentCount;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
        this.projectStartAt = projectStartAt;
        this.projectEndAt = projectEndAt;
        this.closed = Boolean.TRUE.equals(closed);
        this.completed = Boolean.TRUE.equals(completed);
        if (Boolean.TRUE.equals(this.completed)) {
            this.closed = true;
        }
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        // Sync fields for safety
        this.createdDate = createdAt != null ? createdAt.toLocalDate() : null;
        this.recruitmentDeadline = recruitmentEndAt;
        this.totalRecruitment = recruitmentCount;
        this.currentRecruitment = 0;
        this.position = "";
        this.leaderName = "Unknown";
        this.leaderRole = "Member";
        this.thumbnailUrl = "";
        this.leaderAvatarUrl = "";
        this.techStackCsv = "";
        this.summary = description != null && description.length() > 50 ? description.substring(0, 50) : description;
    }

    // --- Constructor from front2 ---
    public Project(
            String title,
            String summary,
            String description,
            String projectType,
            String position,
            String leaderName,
            String leaderRole,
            String leaderAvatarUrl,
            String thumbnailUrl,
            Integer currentRecruitment,
            Integer totalRecruitment,
            LocalDate recruitmentDeadline,
            LocalDate createdDate,
            String techStackCsv
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.projectType = projectType;
        this.position = position;
        this.leaderName = leaderName;
        this.leaderRole = leaderRole;
        this.leaderAvatarUrl = leaderAvatarUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.currentRecruitment = currentRecruitment;
        this.totalRecruitment = totalRecruitment;
        this.recruitmentDeadline = recruitmentDeadline;
        this.createdDate = createdDate;
        this.techStackCsv = techStackCsv;
        // Sync fields for safety
        this.recruitmentCount = totalRecruitment;
        this.recruitmentEndAt = recruitmentDeadline;
        this.createdAt = createdDate != null ? createdDate.atStartOfDay() : LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.closed = false;
        this.completed = false;
        this.progressMethod = "ONLINE"; // default
    }

    public void update(String title, String description, String projectType, String progressMethod,
                       Integer recruitmentCount, LocalDate recruitmentStartAt, LocalDate recruitmentEndAt,
                       LocalDate projectStartAt, LocalDate projectEndAt, Boolean closed, Boolean completed,
                       LocalDateTime updatedAt) {
        this.title = title;
        this.description = description;
        this.projectType = projectType;
        this.progressMethod = progressMethod;
        this.recruitmentCount = recruitmentCount;
        this.recruitmentStartAt = recruitmentStartAt;
        this.recruitmentEndAt = recruitmentEndAt;
        this.projectStartAt = projectStartAt;
        this.projectEndAt = projectEndAt;
        if (closed != null) {
            this.closed = closed;
        } else if (this.closed == null) {
            this.closed = false;
        }
        if (completed != null) {
            this.completed = completed;
        } else if (this.completed == null) {
            this.completed = false;
        }
        if (Boolean.TRUE.equals(this.completed)) {
            this.closed = true;
        }
        this.updatedAt = updatedAt;

        this.recruitmentDeadline = recruitmentEndAt;
        this.totalRecruitment = recruitmentCount;
        this.summary = description != null && description.length() > 50 ? description.substring(0, 50) : description;
    }

    public void updateDiscoveryFields(String position, String techStackCsv, String thumbnailUrl) {
        if (position != null) {
            this.position = position;
        }
        if (techStackCsv != null) {
            this.techStackCsv = techStackCsv;
        }
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    public void replaceTechStacks(List<String> techStacks) {
        List<String> requestedTechStacks = normalizeTechStackNames(techStacks);
        this.techStackSelections.removeIf(existing -> !requestedTechStacks.contains(existing.getTechStackName()));

        for (int i = 0; i < requestedTechStacks.size(); i++) {
            String techStackName = requestedTechStacks.get(i);
            ProjectTechStack existing = this.techStackSelections.stream()
                    .filter(current -> techStackName.equals(current.getTechStackName()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                this.techStackSelections.add(new ProjectTechStack(this, techStackName, i));
            } else {
                existing.updateSortOrder(i);
            }
        }
    }

    private List<String> normalizeTechStackNames(List<String> techStacks) {
        if (techStacks == null) {
            return List.of();
        }
        return new ArrayList<>(new LinkedHashSet<>(techStacks.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList()));
    }

    public void replacePositions(List<com.example.meetball.domain.project.support.ProjectSelectionCatalog.PositionCapacity> positions) {
        List<String> requestedNames = new ArrayList<>();
        if (positions != null) {
            for (var position : positions) {
                requestedNames.add(position.name());
            }
        }

        this.positionSelections.removeIf(existing -> !requestedNames.contains(existing.getPositionName()));
        if (positions == null) {
            return;
        }

        for (int i = 0; i < positions.size(); i++) {
            var position = positions.get(i);
            ProjectPosition existing = this.positionSelections.stream()
                    .filter(current -> position.name().equals(current.getPositionName()))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                this.positionSelections.add(new ProjectPosition(this, position.name(), position.capacity(), i));
            } else {
                existing.updateCapacityAndOrder(position.capacity(), i);
            }
        }
        this.positionSelections.sort((left, right) -> Integer.compare(left.getSortOrder(), right.getSortOrder()));
    }

    public void incrementCurrentRecruitment() {
        int current = currentRecruitment == null ? 0 : currentRecruitment;
        Integer capacity = totalRecruitment != null ? totalRecruitment : recruitmentCount;
        if (capacity != null && capacity > 0 && current >= capacity) {
            throw new IllegalStateException("Recruitment count is already full.");
        }
        this.currentRecruitment = current + 1;
    }

    public void decrementCurrentRecruitment() {
        int current = currentRecruitment == null ? 0 : currentRecruitment;
        this.currentRecruitment = Math.max(0, current - 1);
    }

    public boolean isRecruitmentFull() {
        Integer capacity = totalRecruitment != null ? totalRecruitment : recruitmentCount;
        if (capacity == null || capacity <= 0) {
            return false;
        }
        int current = currentRecruitment == null ? 0 : currentRecruitment;
        return current >= capacity;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getPosition() { return position; }
    public String getProgressMethod() { return progressMethod; }
    public String getLeaderName() { return leaderName; }
    public void setLeaderName(String leaderName) { this.leaderName = leaderName; }
    public String getLeaderRole() { return leaderRole; }
    public String getLeaderAvatarUrl() { return leaderAvatarUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public Integer getCurrentRecruitment() { return currentRecruitment; }
    public Integer getTotalRecruitment() { return totalRecruitment; }
    public Integer getRecruitmentCount() { return recruitmentCount; }
    public LocalDate getRecruitmentStartAt() { return recruitmentStartAt; }
    public LocalDate getRecruitmentDeadline() { return recruitmentDeadline; }
    public LocalDate getRecruitmentEndAt() { return recruitmentEndAt; }
    public LocalDate getProjectStartAt() { return projectStartAt; }
    public LocalDate getProjectEndAt() { return projectEndAt; }
    public Boolean getClosed() { return Boolean.TRUE.equals(closed); }
    public Boolean getCompleted() { return Boolean.TRUE.equals(completed); }
    public boolean isCompleted() { return Boolean.TRUE.equals(completed); }
    public LocalDate getCreatedDate() { return createdDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getTechStackCsv() { return techStackCsv; }
    public List<ProjectTechStack> getTechStackSelections() { return techStackSelections; }
    public List<ProjectPosition> getPositionSelections() { return positionSelections; }
}
