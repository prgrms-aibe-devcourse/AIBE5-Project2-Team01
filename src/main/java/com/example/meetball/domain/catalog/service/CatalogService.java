package com.example.meetball.domain.catalog.service;

import com.example.meetball.domain.catalog.entity.CatalogPosition;
import com.example.meetball.domain.catalog.entity.CatalogTechStack;
import com.example.meetball.domain.catalog.repository.CatalogPositionRepository;
import com.example.meetball.domain.catalog.repository.CatalogTechStackRepository;
import com.example.meetball.domain.catalog.support.CatalogDefaults;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogService {

    private static final List<String> TECH_STACK_CATEGORY_ORDER = List.of("프론트엔드", "백엔드", "모바일", "기타");

    private final CatalogPositionRepository positionRepository;
    private final CatalogTechStackRepository techStackRepository;

    public List<String> positionOptions() {
        return activePositions().stream()
                .map(CatalogPosition::getName)
                .toList();
    }

    public List<String> techStackOptions() {
        return activeTechStacks().stream()
                .map(CatalogTechStack::getName)
                .toList();
    }

    public Map<String, List<String>> techStackCategories() {
        List<CatalogTechStack> techStacks = activeTechStacks();
        Map<String, List<String>> categories = new LinkedHashMap<>();
        categories.put("인기", techStacks.stream()
                .filter(CatalogTechStack::isPopular)
                .map(CatalogTechStack::getName)
                .toList());
        for (String category : TECH_STACK_CATEGORY_ORDER) {
            categories.put(category, techStacks.stream()
                    .filter(techStack -> category.equals(techStack.getCategory()))
                    .map(CatalogTechStack::getName)
                    .toList());
        }
        categories.put("모두보기", techStacks.stream()
                .map(CatalogTechStack::getName)
                .toList());
        return categories;
    }

    public Map<String, Map<String, String>> techStackMeta() {
        Map<String, Map<String, String>> meta = new LinkedHashMap<>();
        for (CatalogTechStack techStack : activeTechStacks()) {
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

    private List<CatalogPosition> activePositions() {
        List<CatalogPosition> positions = positionRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc();
        if (!positions.isEmpty()) {
            return positions;
        }
        return CatalogDefaults.POSITIONS.stream()
                .map(CatalogPosition::from)
                .toList();
    }

    private List<CatalogTechStack> activeTechStacks() {
        List<CatalogTechStack> techStacks = techStackRepository.findAllByActiveTrueOrderBySortOrderAscIdAsc();
        if (!techStacks.isEmpty()) {
            return techStacks;
        }
        return CatalogDefaults.TECH_STACKS.stream()
                .map(CatalogTechStack::from)
                .toList();
    }

    private Map<String, String> positionAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (CatalogPosition position : activePositions()) {
            for (String alias : position.aliasList()) {
                aliases.put(alias, position.getName());
            }
        }
        return aliases;
    }

    private Map<String, String> techStackAliases() {
        Map<String, String> aliases = new LinkedHashMap<>();
        for (CatalogTechStack techStack : activeTechStacks()) {
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
