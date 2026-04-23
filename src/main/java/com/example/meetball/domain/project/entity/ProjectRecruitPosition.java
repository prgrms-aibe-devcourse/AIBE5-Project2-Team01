package com.example.meetball.domain.project.entity;

import com.example.meetball.domain.position.entity.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "project_recruit_position",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "position_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectRecruitPosition {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_CLOSED = "CLOSED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recruit_position_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "role_description", columnDefinition = "TEXT")
    private String roleDescription;

    @Column(columnDefinition = "TEXT")
    private String qualification;

    @Column(name = "required_user", nullable = false)
    private int capacity;

    @Column(name = "approved_user", nullable = false)
    private int approvedUser;

    @Column(name = "recruit_status", nullable = false, length = 20)
    private String recruitStatus = STATUS_OPEN;

    public ProjectRecruitPosition(Project project, Position position, int capacity) {
        this.project = project;
        this.position = position;
        this.capacity = Math.max(1, capacity);
        this.approvedUser = 0;
        this.recruitStatus = STATUS_OPEN;
    }

    public void updateCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
        this.recruitStatus = approvedUser >= this.capacity ? STATUS_CLOSED : STATUS_OPEN;
    }

    public void updateCapacityAndOrder(int capacity, int sortOrder) {
        updateCapacity(capacity);
    }

    public void incrementApprovedUser() {
        if (approvedUser >= capacity) {
            throw new IllegalStateException("Recruit position is already full.");
        }
        this.approvedUser++;
        if (approvedUser >= capacity) {
            this.recruitStatus = STATUS_CLOSED;
        }
    }

    public void decrementApprovedUser() {
        this.approvedUser = Math.max(0, approvedUser - 1);
        if (approvedUser < capacity) {
            this.recruitStatus = STATUS_OPEN;
        }
    }

    public Long getPositionId() {
        return position != null ? position.getId() : null;
    }

    public String getPositionName() {
        return position != null ? position.getName() : "";
    }

    public int getSortOrder() {
        return id == null ? 0 : id.intValue();
    }
}
