package com.example.meetball.global.config;

import com.example.meetball.domain.catalog.entity.CatalogPosition;
import com.example.meetball.domain.catalog.entity.CatalogTechStack;
import com.example.meetball.domain.catalog.repository.CatalogPositionRepository;
import com.example.meetball.domain.catalog.repository.CatalogTechStackRepository;
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

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
@ConditionalOnProperty(name = "app.catalog-bootstrap.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogBootstrapRunner implements ApplicationRunner {

    private final CatalogPositionRepository positionRepository;
    private final CatalogTechStackRepository techStackRepository;
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
            CatalogPosition position = positionRepository.findByName(definition.name())
                    .orElseGet(() -> CatalogPosition.from(definition));
            position.apply(definition);
            positionRepository.save(position);
        }
    }

    private void upsertTechStacks() {
        for (CatalogDefaults.TechStackDefinition definition : CatalogDefaults.TECH_STACKS) {
            CatalogTechStack techStack = techStackRepository.findByName(definition.name())
                    .orElseGet(() -> CatalogTechStack.from(definition));
            techStack.apply(definition);
            techStackRepository.save(techStack);
        }
    }
}
