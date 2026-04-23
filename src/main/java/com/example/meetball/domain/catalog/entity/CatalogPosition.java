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
@Table(name = "catalog_position",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean active;

    @Column(length = 1000)
    private String aliases;

    public static CatalogPosition from(CatalogDefaults.PositionDefinition definition) {
        CatalogPosition position = new CatalogPosition();
        position.apply(definition);
        return position;
    }

    public void apply(CatalogDefaults.PositionDefinition definition) {
        this.name = definition.name();
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
