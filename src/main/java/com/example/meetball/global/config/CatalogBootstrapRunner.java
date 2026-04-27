package com.example.meetball.global.config;

import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.catalog.service.CatalogService;
import com.example.meetball.domain.catalog.support.CatalogDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(name = "app.catalog-bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogBootstrapRunner implements ApplicationRunner {

    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;
    private final CatalogService catalogService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        upsertPositions();
        upsertTechStacks();
        positionRepository.flush();
        techStackRepository.flush();
        catalogService.refreshProjectSelectionCatalog();
    }

    private void upsertPositions() {
        for (CatalogDefaults.PositionDefinition definition : CatalogDefaults.POSITIONS) {
            Position position = positionRepository.findByName(definition.name())
                    .orElseGet(() -> Position.from(definition));
            position.apply(definition);
            positionRepository.save(position);
        }
    }

    private void upsertTechStacks() {
        Map<String, Position> positionsByName = positionRepository.findAllByOrderByIdAsc().stream()
                .collect(Collectors.toMap(Position::getName, position -> position, (left, right) -> left));
        for (CatalogDefaults.TechStackDefinition definition : CatalogDefaults.TECH_STACKS) {
            Position position = positionsByName.get(positionNameFor(definition));
            if (position == null) {
                position = positionsByName.get("기타");
            }
            if (position == null) {
                throw new IllegalStateException("기술스택 기본값을 저장하려면 기타 포지션이 먼저 필요합니다.");
            }
            Position resolvedPosition = position;
            TechStack techStack = techStackRepository.findByName(definition.name())
                    .orElseGet(() -> TechStack.from(definition, resolvedPosition));
            techStack.apply(definition, resolvedPosition);
            techStackRepository.save(techStack);
        }
    }

    private String positionNameFor(CatalogDefaults.TechStackDefinition definition) {
        return switch (definition.name()) {
            case "Swift" -> "IOS";
            case "Kotlin" -> "안드로이드";
            case "ReactNative", "Flutter" -> "기타";
            default -> switch (definition.category()) {
                case "프론트엔드" -> "프론트엔드";
                case "백엔드" -> "백엔드";
                default -> "기타";
            };
        };
    }
}
