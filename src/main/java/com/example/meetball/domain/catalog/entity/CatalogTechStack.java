package com.example.meetball.domain.catalog.entity;

import com.example.meetball.domain.catalog.support.CatalogDefaults;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "catalog_tech_stack",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 40)
    private String category;

    @Column(nullable = false)
    private boolean popular;

    @Column(name = "badge_label", length = 20)
    private String badgeLabel;

    @Column(name = "icon_class", length = 80)
    private String iconClass;

    @Column(name = "accent_color", length = 20)
    private String accentColor;

    @Column(name = "text_color", length = 20)
    private String textColor;

    @Column(name = "background_color", length = 20)
    private String backgroundColor;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String aliases;

    public static CatalogTechStack from(CatalogDefaults.TechStackDefinition definition) {
        CatalogTechStack techStack = new CatalogTechStack();
        techStack.apply(definition);
        return techStack;
    }

    public void apply(CatalogDefaults.TechStackDefinition definition) {
        this.name = definition.name();
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
