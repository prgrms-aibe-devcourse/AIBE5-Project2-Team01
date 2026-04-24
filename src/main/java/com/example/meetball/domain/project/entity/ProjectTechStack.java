package com.example.meetball.domain.project.entity;

import com.example.meetball.domain.techstack.entity.TechStack;
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
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "tech_stack_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_tech_stack_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    public ProjectTechStack(Project project, TechStack techStack) {
        this.project = project;
        this.techStack = techStack;
    }

    public Long getTechStackId() {
        return techStack != null ? techStack.getId() : null;
    }

    public String getTechStackName() {
        return techStack != null ? techStack.getName() : "";
    }
}
