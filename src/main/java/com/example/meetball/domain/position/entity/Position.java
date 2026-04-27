package com.example.meetball.domain.position.entity;

import com.example.meetball.domain.catalog.support.CatalogDefaults;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "position",
        uniqueConstraints = @UniqueConstraint(columnNames = "position_name"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Long id;

    @Column(name = "position_name", nullable = false, length = 50)
    private String name;

    @Transient
    private int sortOrder;

    @Transient
    private boolean active;

    @Transient
    private String aliases;

    public static Position from(CatalogDefaults.PositionDefinition definition) {
        Position position = new Position();
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
