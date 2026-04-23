package com.example.meetball.domain.project.entity;

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
@Table(name = "project_position",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "position_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "position_name", nullable = false, length = 80)
    private String positionName;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ProjectPosition(Project project, String positionName, int capacity, int sortOrder) {
        this.project = project;
        this.positionName = positionName;
        this.capacity = Math.max(1, capacity);
        this.sortOrder = sortOrder;
    }

    public void updateCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public void updateCapacityAndOrder(int capacity, int sortOrder) {
        this.capacity = Math.max(1, capacity);
        this.sortOrder = sortOrder;
    }
}
