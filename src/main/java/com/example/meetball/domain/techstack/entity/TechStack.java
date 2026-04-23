package com.example.meetball.domain.techstack.entity;

import com.example.meetball.domain.catalog.support.CatalogDefaults;
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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "tech_stack",
        uniqueConstraints = @UniqueConstraint(columnNames = {"position_id", "tech_stack_name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tech_stack_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Column(name = "tech_stack_name", nullable = false, length = 50)
    private String name;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Transient
    private String category;

    @Transient
    private boolean popular;

    @Transient
    private String badgeLabel;

    @Transient
    private String iconClass;

    @Transient
    private String accentColor;

    @Transient
    private String textColor;

    @Transient
    private String backgroundColor;

    @Transient
    private int sortOrder;

    @Transient
    private boolean active;

    @Transient
    private String aliases;

    public static TechStack from(CatalogDefaults.TechStackDefinition definition, Position position) {
        TechStack techStack = new TechStack();
        techStack.apply(definition, position);
        return techStack;
    }

    public void apply(CatalogDefaults.TechStackDefinition definition, Position position) {
        this.position = position;
        this.name = definition.name();
        this.iconUrl = definition.iconClass();
        this.category = definition.category();
        this.popular = definition.popular();
        this.badgeLabel = definition.badgeLabel();
        this.iconClass = definition.iconClass();
        this.accentColor = definition.accentColor();
        this.textColor = definition.textColor();
        this.backgroundColor = definition.backgroundColor();
        this.sortOrder = definition.sortOrder();
        this.active = true;
        this.aliases = joinAliases(definition.aliases());
    }

    public List<String> aliasList() {
        return splitAliases(aliases);
    }

    private static String joinAliases(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return String.join(", ", values);
    }

    private static List<String> splitAliases(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(alias -> !alias.isEmpty())
                .toList();
    }
}
