package com.example.meetball.domain.catalog.service;

import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.catalog.support.CatalogDefaults;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private static final List<String> TECH_STACK_CATEGORY_ORDER = List.of("프론트엔드", "백엔드", "모바일", "기타");

    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;

    public List<String> positionOptions() {
        return activePositions().stream()
                .map(Position::getName)
                .toList();
    }

    public List<String> techStackOptions() {
        return activeTechStacks().stream()
                .map(TechStack::getName)
                .toList();
    }

    public Map<String, List<String>> techStackCategories() {
        List<TechStack> techStacks = activeTechStacks();
        Map<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("인기", techStacks.stream()
                .filter(TechStack::isPopular)
                .map(TechStack::getName)
                .toList());
        for (String category : TECH_STACK_CATEGORY_ORDER) {
            categories.put(category, techStacks.stream()
                    .filter(techStack -> category.equals(techStack.getCategory()))
                    .map(TechStack::getName)
                    .toList());
        }
        categories.put("모두보기", techStacks.stream()
                .map(TechStack::getName)
                .toList());
        return categories;
    }

    public Map<String, Map<String, String>> techStackMeta() {
        Map<String, Map<String, String>> meta = new LinkedHashMap<>();
        for (TechStack techStack : activeTechStacks()) {
            Map<String, String> item = new LinkedHashMap<>();
            putIfPresent(item, "label", techStack.getBadgeLabel());
            putIfPresent(item, "icon", techStack.getIconClass());
            putIfPresent(item, "accent", techStack.getAccentColor());
            putIfPresent(item, "text", techStack.getTextColor());
            putIfPresent(item, "bg", techStack.getBackgroundColor());
            meta.put(techStack.getName(), item);
        }
        return meta;
    }

    public void refreshProjectSelectionCatalog() {
        ProjectSelectionCatalog.configure(
                positionOptions(),
                positionAliases(),
                techStackOptions(),
                techStackAliases()
        );
    }

    private List<Position> activePositions() {
        List<Position> positions = positionRepository.findAllByOrderByIdAsc();
        if (!positions.isEmpty()) {
            return withPositionMetadata(positions);
        }
        return CatalogDefaults.POSITIONS.stream()
                .map(Position::from)
                .toList();
    }

    private List<TechStack> activeTechStacks() {
        List<TechStack> techStacks = techStackRepository.findAllByOrderByIdAsc();
        if (!techStacks.isEmpty()) {
            return withTechStackMetadata(techStacks);
        }
        return CatalogDefaults.TECH_STACKS.stream()
                .map(definition -> TechStack.from(definition, fallbackPosition(definition)))
                .toList();
    }

    private List<Position> withPositionMetadata(List<Position> positions) {
        Map<String, CatalogDefaults.PositionDefinition> defaults = CatalogDefaults.POSITIONS.stream()
                .collect(Collectors.toMap(CatalogDefaults.PositionDefinition::name, Function.identity()));
        for (Position position : positions) {
            CatalogDefaults.PositionDefinition definition = defaults.get(position.getName());
            if (definition != null) {
                position.apply(definition);
            }
        }
        return positions;
    }

    private List<TechStack> withTechStackMetadata(List<TechStack> techStacks) {
        Map<String, CatalogDefaults.TechStackDefinition> defaults = CatalogDefaults.TECH_STACKS.stream()
                .collect(Collectors.toMap(CatalogDefaults.TechStackDefinition::name, Function.identity()));
        for (TechStack techStack : techStacks) {
            CatalogDefaults.TechStackDefinition definition = defaults.get(techStack.getName());
            if (definition != null) {
                techStack.apply(definition, techStack.getPosition());
            }
        }
        return techStacks;
    }

    private Position fallbackPosition(CatalogDefaults.TechStackDefinition definition) {
        String positionName = "기타";
        if ("프론트엔드".equals(definition.category())) {
            positionName = "프론트엔드";
        } else if ("백엔드".equals(definition.category())) {
            positionName = "백엔드";
        }
        String finalPositionName = positionName;
        return CatalogDefaults.POSITIONS.stream()
                .filter(position -> finalPositionName.equals(position.name()))
                .findFirst()
                .map(Position::from)
                .orElseGet(() -> Position.from(CatalogDefaults.position("기타", 999)));
    }

    private Map<String, String> positionAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (Position position : activePositions()) {
            for (String alias : position.aliasList()) {
                aliases.put(alias, position.getName());
            }
        }
        return aliases;
    }

    private Map<String, String> techStackAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (TechStack techStack : activeTechStacks()) {
            for (String alias : techStack.aliasList()) {
                aliases.put(alias, techStack.getName());
            }
        }
        return aliases;
    }

    private void putIfPresent(Map<String, String> values, String key, String value) {
        if (value != null && !value.isBlank()) {
            values.put(key, value);
        }
    }
}
