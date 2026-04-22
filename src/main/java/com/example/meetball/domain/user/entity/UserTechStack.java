package com.example.meetball.domain.user.entity;

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
@Table(name = "user_tech_stack",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tech_stack_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "tech_stack_name", nullable = false, length = 80)
    private String techStackName;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public UserTechStack(User user, String techStackName, int sortOrder) {
        this.user = user;
        this.techStackName = techStackName;
        this.sortOrder = sortOrder;
    }
}
