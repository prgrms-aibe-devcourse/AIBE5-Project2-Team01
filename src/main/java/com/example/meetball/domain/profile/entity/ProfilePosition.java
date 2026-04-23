package com.example.meetball.domain.profile.entity;

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
@Table(name = "profile_position",
        uniqueConstraints = @UniqueConstraint(columnNames = {"profile_id", "position_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfilePosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_position_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "experience_years", length = 255)
    private String experienceYears;

    public ProfilePosition(Profile profile, Position position) {
        this.profile = profile;
        this.position = position;
    }

    public String getPositionName() {
        return position != null ? position.getName() : "";
    }

    public Long getPositionId() {
        return position != null ? position.getId() : null;
    }
}
