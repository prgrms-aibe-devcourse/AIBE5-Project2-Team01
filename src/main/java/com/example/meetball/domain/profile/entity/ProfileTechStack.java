package com.example.meetball.domain.profile.entity;

import com.example.meetball.domain.techstack.entity.TechStack;
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
@Table(name = "profile_tech_stack",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "tech_stack_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @jakarta.persistence.Column(name = "profile_tech_stack_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    public ProfileTechStack(Profile profile, TechStack techStack) {
        this.profile = profile;
        this.techStack = techStack;
    }

    public Long getTechStackId() {
        return techStack != null ? techStack.getId() : null;
    }

    public String getTechStackName() {
        return techStack != null ? techStack.getName() : "";
    }
}
