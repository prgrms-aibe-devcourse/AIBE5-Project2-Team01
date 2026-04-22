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
@Table(name = "project_tech_stack",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "tech_stack_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "tech_stack_name", nullable = false, length = 80)
    private String techStackName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ProjectTechStack(Project project, String techStackName, int sortOrder) {
        this.project = project;
        this.techStackName = techStackName;
        this.sortOrder = sortOrder;
    }
}
