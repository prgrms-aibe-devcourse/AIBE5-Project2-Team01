package com.example.meetball.global.config;

import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectPosition;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.catalog-normalizer.enabled", havingValue = "true", matchIfMissing = true)
public class CatalogDataNormalizer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        normalizeUsers();
        normalizeProjects();
        normalizeApplications();
    }

    private void normalizeUsers() {
        for (User user : userRepository.findAll()) {
            String normalizedTechStack = ProjectSelectionCatalog.normalizeTechStackCsvOrDefault(user.getTechStack());
            if (!normalizedTechStack.equals(user.getTechStack())) {
                user.updateProfile(user.getNickname(), user.getJobTitle(), normalizedTechStack, user.isPublic());
            }
            if (user.getTechStackSelections().isEmpty()) {
                user.replaceTechStacks(splitTechStacks(normalizedTechStack));
            }
        }
    }

    private void normalizeProjects() {
        for (Project project : projectRepository.findAll()) {
            String normalizedPositions = ProjectSelectionCatalog.normalizePositionCsvOrDefault(
                    project.getPosition(),
                    project.getTotalRecruitment() != null ? project.getTotalRecruitment() : project.getRecruitmentCount()
            );
            String normalizedTechStacks = ProjectSelectionCatalog.normalizeTechStackCsvOrDefault(project.getTechStackCsv());
            if (!normalizedPositions.equals(project.getPosition()) || !normalizedTechStacks.equals(project.getTechStackCsv())) {
                project.updateDiscoveryFields(normalizedPositions, normalizedTechStacks, null);
            }
            if (project.getPositionSelections().isEmpty()) {
                project.replacePositions(ProjectSelectionCatalog.parsePositionCapacities(normalizedPositions, null));
            }
            if (project.getTechStackSelections().isEmpty()) {
                project.replaceTechStacks(splitTechStacks(normalizedTechStacks));
            }
        }
    }

    private void normalizeApplications() {
        for (Application application : applicationRepository.findAll()) {
            Project project = application.getProject();
            if (project == null) {
                continue;
            }
            if (application.getStatus() != null && !application.getStatus().blocksPositionRemoval()) {
                if (application.getProjectPosition() != null) {
                    application.updateProjectPosition(null);
                }
                continue;
            }
            List<ProjectSelectionCatalog.PositionCapacity> capacities =
                    project.getPositionSelections().isEmpty()
                            ? ProjectSelectionCatalog.parsePositionCapacities(project.getPosition(), project.getTotalRecruitment())
                            : project.getPositionSelections().stream()
                            .map(position -> new ProjectSelectionCatalog.PositionCapacity(position.getPositionName(), position.getCapacity()))
                            .toList();
            String normalizedPosition = resolveApplicationPosition(application.getPosition(), capacities);
            ProjectPosition targetPosition = project.getPositionSelections().stream()
                    .filter(position -> normalizedPosition.equals(position.getPositionName()))
                    .findFirst()
                    .orElse(null);
            if (targetPosition != null && application.getProjectPosition() != targetPosition) {
                application.updateProjectPosition(targetPosition);
            } else if (!normalizedPosition.equals(application.getPosition())) {
                application.updatePosition(normalizedPosition);
            }
        }
    }

    private String resolveApplicationPosition(String value, List<ProjectSelectionCatalog.PositionCapacity> capacities) {
        String fallback = capacities.isEmpty() ? "기타" : capacities.get(0).name();
        try {
            String normalized = ProjectSelectionCatalog.positionName(value);
            boolean available = capacities.stream().anyMatch(position -> position.name().equals(normalized));
            return available ? normalized : fallback;
        } catch (IllegalArgumentException exception) {
            return fallback;
        }
    }

    private List<String> splitTechStacks(String techStackCsv) {
        if (techStackCsv == null || techStackCsv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(techStackCsv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
