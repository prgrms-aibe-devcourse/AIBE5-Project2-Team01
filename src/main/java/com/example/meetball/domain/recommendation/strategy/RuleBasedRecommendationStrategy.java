package com.example.meetball.domain.recommendation.strategy;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.entity.ProfileTechStack;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import com.example.meetball.domain.project.entity.ProjectTechStack;
import com.example.meetball.domain.project.support.ProjectOptionCatalog;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedRecommendationStrategy implements RecommendationStrategy {

    private static final int TECH_STACK_SCORE_PER_MATCH = 20;
    private static final int POSITION_MATCH_SCORE = 30;
    private static final int KEYWORD_MATCH_SCORE = 10;
    private static final int DIVERSITY_PENALTY = -15;

    @Override
    public List<RecommendationResponseDto> generateCandidatePool(Profile profile, List<Project> availableProjects) {
        Set<String> profileTechSet = parseProfileTechStack(profile);
        String normalizedPosition = profile.getPosition() == null
                ? ""
                : profile.getPosition().trim().toLowerCase(Locale.ROOT);

        List<ProjectScore> scoredProjects = new ArrayList<>();

        for (Project project : availableProjects) {
            List<String> reasons = new ArrayList<>();
            int score = ThreadLocalRandom.current().nextInt(6);

            int techMatchCount = countTechStackMatches(profileTechSet, project);
            if (techMatchCount > 0) {
                score += techMatchCount * TECH_STACK_SCORE_PER_MATCH;
                reasons.add("보유 기술스택이 " + techMatchCount + "개 일치합니다");
            }

            boolean positionMatched = isPositionMatched(normalizedPosition, project);
            if (positionMatched) {
                score += POSITION_MATCH_SCORE;
                reasons.add("프로필 포지션과 모집 포지션이 유사합니다");
            }

            if (isKeywordMatched(profileTechSet, normalizedPosition, project)) {
                score += KEYWORD_MATCH_SCORE;
                reasons.add("프로젝트 설명에 관련 키워드가 포함되어 있습니다");
            }

            scoredProjects.add(new ProjectScore(project, score, reasons, positionMatched));
        }

        scoredProjects.sort(Comparator.comparingInt(ProjectScore::score).reversed()
                .thenComparing(projectScore -> projectScore.project().getId()));

        List<RecommendationResponseDto> results = new ArrayList<>();
        Set<String> selectedWorkMethods = new LinkedHashSet<>();
        int maxCount = Math.min(10, scoredProjects.size());

        while (results.size() < maxCount && !scoredProjects.isEmpty()) {
            ProjectScore best = scoredProjects.remove(0);
            String recommendationReason = resolveRecommendationReason(best.project(), best.reasons());

            results.add(RecommendationResponseDto.builder()
                    .projectId(best.project().getId())
                    .title(best.project().getTitle())
                    .summary(best.project().getSummary())
                    .score(best.score())
                    .reasons(best.reasons())
                    .recommendationReason(recommendationReason)
                    .axisValue(null)
                    .progressMethod(ProjectOptionCatalog.displayWorkMethod(best.project().getWorkMethod()))
                    .projectType(ProjectOptionCatalog.displayProjectPurpose(best.project().getProjectPurpose()))
                    .positionSummary(extractPositionSummary(best.project()))
                    .build());

            String selectedWorkMethod = ProjectOptionCatalog.displayWorkMethod(best.project().getWorkMethod());
            if (!selectedWorkMethod.isBlank()) {
                selectedWorkMethods.add(selectedWorkMethod);
            }

            for (int i = 0; i < scoredProjects.size(); i++) {
                ProjectScore current = scoredProjects.get(i);
                String workMethod = ProjectOptionCatalog.displayWorkMethod(current.project().getWorkMethod());
                if (!workMethod.isBlank() && selectedWorkMethods.contains(workMethod)) {
                    scoredProjects.set(i, current.withAdjustedScore(current.score() + DIVERSITY_PENALTY));
                }
            }

            scoredProjects.sort(Comparator.comparingInt(ProjectScore::score).reversed()
                    .thenComparing(projectScore -> projectScore.project().getId()));
        }

        return results;
    }

    private Set<String> parseProfileTechStack(Profile profile) {
        if (profile.getTechStackSelections() != null && !profile.getTechStackSelections().isEmpty()) {
            return profile.getTechStackSelections().stream()
                    .map(ProfileTechStack::getTechStackName)
                    .map(ProjectSelectionCatalog::searchKey)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toSet());
        }
        return parseTechStack(profile.getTechStack());
    }

    private Set<String> parseTechStack(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return raw.lines()
                .flatMap(line -> java.util.Arrays.stream(line.split(",")))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .map(ProjectSelectionCatalog::searchKey)
                .filter(token -> !token.isEmpty())
                .collect(Collectors.toSet());
    }

    private int countTechStackMatches(Set<String> profileTechSet, Project project) {
        if (profileTechSet.isEmpty()) {
            return 0;
        }

        Set<String> projectTechSet = project.getTechStackSelections() == null || project.getTechStackSelections().isEmpty()
                ? Set.of()
                : project.getTechStackSelections().stream()
                .map(ProjectTechStack::getTechStackName)
                .map(ProjectSelectionCatalog::searchKey)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet());

        if (projectTechSet.isEmpty()) {
            return 0;
        }

        return (int) profileTechSet.stream()
                .filter(projectTechSet::contains)
                .count();
    }

    private boolean isPositionMatched(String normalizedPosition, Project project) {
        if (normalizedPosition == null || normalizedPosition.isBlank()) {
            return false;
        }

        return project.getPositionSelections().stream()
                .map(ProjectRecruitPosition::getPositionName)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(position -> normalizedPosition.contains(position) || position.contains(normalizedPosition));
    }

    private boolean isKeywordMatched(Set<String> profileTechSet, String normalizedPosition, Project project) {
        String fullText = (project.getTitle() + " " + project.getSummary() + " " + project.getDescription())
                .toLowerCase(Locale.ROOT);

        if (normalizedPosition != null && !normalizedPosition.isBlank() && fullText.contains(normalizedPosition)) {
            return true;
        }

        return profileTechSet.stream().anyMatch(fullText::contains);
    }

    private String extractPositionSummary(Project project) {
        if (project.getPositionSelections() == null || project.getPositionSelections().isEmpty()) {
            return "팀원 모집 중";
        }

        return project.getPositionSelections().stream()
                .map(ProjectRecruitPosition::getPositionName)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String resolveRecommendationReason(Project project, List<String> reasons) {
        String projectPurpose = ProjectOptionCatalog.displayProjectPurpose(project.getProjectPurpose());
        String workMethod = ProjectOptionCatalog.displayWorkMethod(project.getWorkMethod());

        if (projectPurpose.contains("스타트업")) {
            return "진짜배기 스타트업 실전";
        }
        if (projectPurpose.contains("공모전") || projectPurpose.contains("해커톤")) {
            return "열정 가득 도전의 무대";
        }
        if (projectPurpose.contains("스터디")) {
            return "차분하게 깊이 파기";
        }
        if (projectPurpose.contains("기업연계") || projectPurpose.contains("정부연계")) {
            return "현업 감각 제대로 익히기";
        }
        if (workMethod.contains("오프라인")) {
            return "직접 만나 불태워봐요";
        }
        if (!reasons.isEmpty() && reasons.get(0).contains("포지션")) {
            return "원하는 역할로 바로 도전";
        }
        return "꾸준히 성장하는 곳";
    }

    private record ProjectScore(Project project, int score, List<String> reasons, boolean positionMatched) {
        ProjectScore withAdjustedScore(int nextScore) {
            return new ProjectScore(project, nextScore, reasons, positionMatched);
        }
    }
}
