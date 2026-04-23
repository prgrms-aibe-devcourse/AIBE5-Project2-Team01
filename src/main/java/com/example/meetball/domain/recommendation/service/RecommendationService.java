package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectPosition;
import com.example.meetball.domain.project.entity.ProjectTechStack;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.entity.UserTechStack;
import com.example.meetball.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 규칙 기반 AI 추천 서비스
 *
 * 점수 산정 기준:
 *   - 기술스택 일치 1개당 20점 (user_tech_stack vs project_tech_stack)
 *   - 직무/포지션 유사 시 +30점 (jobTitle vs project_position, 부분 문자열 포함 비교)
 *
 * 추천 대상: 모집 마감(closed)과 프로젝트 완료(completed)가 아닌 프로젝트만 포함
 * 정렬: 점수 내림차순 → 점수가 동일하면 projectId 오름차순
 */
@Service
public class RecommendationService {

    // 기술스택 1개 일치당 부여하는 점수
    private static final int TECH_STACK_SCORE_PER_MATCH = 20;

    // 직무/포지션 유사 시 부여하는 추가 점수
    private static final int JOB_TITLE_MATCH_SCORE = 30;

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public RecommendationService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    /**
     * 특정 사용자 기준으로 추천 프로젝트 목록을 반환합니다.
     *
     * @param userId 추천 대상 사용자 ID
     * @return 점수 내림차순으로 정렬된 추천 프로젝트 목록
     */
    @Transactional(readOnly = true)
    public List<RecommendationResponseDto> recommend(Long userId) {
        // 1. 사용자 조회 (존재하지 않으면 예외)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다. userId=" + userId));

        // 2. 모집 중이고 아직 완료되지 않은 프로젝트만 조회
        List<Project> openProjects = projectRepository.findAll().stream()
                .filter(p -> !Boolean.TRUE.equals(p.getClosed()))
                .filter(p -> !Boolean.TRUE.equals(p.getCompleted()))
                .collect(Collectors.toList());

        // 3. 사용자 기술스택을 소문자 집합으로 변환
        Set<String> userTechSet = parseUserTechStack(user);

        // 4. 각 프로젝트에 대해 점수 및 이유 계산
        List<RecommendationResponseDto> result = new ArrayList<>();
        for (Project project : openProjects) {
            List<String> reasons = new ArrayList<>();
            int score = 0;

            // 기술스택 점수 계산
            int techMatchCount = countTechStackMatches(userTechSet, project);
            if (techMatchCount > 0) {
                score += techMatchCount * TECH_STACK_SCORE_PER_MATCH;
                reasons.add("보유 기술스택이 " + techMatchCount + "개 일치합니다");
            }

            // 직무/포지션 유사도 점수 계산
            if (isJobTitleMatched(user.getJobTitle(), project)) {
                score += JOB_TITLE_MATCH_SCORE;
                reasons.add("희망 직무와 모집 포지션이 유사합니다");
            }

            // 점수가 0이라도 목록에 포함 (프론트엔드에서 필터링 가능)
            result.add(new RecommendationResponseDto(project.getId(), project.getTitle(), score, reasons));
        }

        // 5. 점수 내림차순 정렬 (동점 시 projectId 오름차순)
        result.sort(Comparator.comparingInt(RecommendationResponseDto::getScore).reversed()
                .thenComparingLong(RecommendationResponseDto::getProjectId));

        return result;
    }

    /**
     * 지정 기술스택 카탈로그 기준으로 정규화한 뒤 비교용 키 집합으로 파싱합니다.
     *
     * @param techStackRaw 원본 기술스택 문자열 (null 허용)
     * @return 소문자로 정규화된 기술 항목 집합
     */
    private Set<String> parseTechStack(String techStackRaw) {
        if (techStackRaw == null || techStackRaw.isBlank()) {
            return Set.of();
        }
        String normalized = ProjectSelectionCatalog.normalizeTechStackText(techStackRaw);
        return java.util.Arrays.stream(normalized.split(","))
                .map(String::trim)
                .map(ProjectSelectionCatalog::searchKey)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    private Set<String> parseUserTechStack(User user) {
        if (user.getTechStackSelections() != null && !user.getTechStackSelections().isEmpty()) {
            return user.getTechStackSelections().stream()
                    .map(UserTechStack::getTechStackName)
                    .map(ProjectSelectionCatalog::searchKey)
                    .filter(value -> !value.isEmpty())
                    .collect(Collectors.toSet());
        }
        return parseTechStack(user.getTechStack());
    }

    /**
     * 사용자의 기술스택 집합과 프로젝트 기술스택 선택 목록을 비교하여 일치하는 항목 수를 반환합니다.
     *
     * @param userTechSet  사용자 기술 항목 집합 (소문자)
     * @return 일치하는 기술 항목 수
     */
    private int countTechStackMatches(Set<String> userTechSet, Project project) {
        if (userTechSet.isEmpty()) {
            return 0;
        }
        Set<String> projectTechSet = project.getTechStackSelections() != null && !project.getTechStackSelections().isEmpty()
                ? project.getTechStackSelections().stream()
                .map(ProjectTechStack::getTechStackName)
                .map(ProjectSelectionCatalog::searchKey)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toSet())
                : Set.of();
        if (projectTechSet.isEmpty()) {
            return 0;
        }

        // 사용자 기술 중 프로젝트 기술 집합에 포함된 것만 카운트
        return (int) userTechSet.stream()
                .filter(projectTechSet::contains)
                .count();
    }

    /**
     * 사용자 직무(jobTitle)와 프로젝트 포지션(position)이 유사한지 판단합니다.
     * 한쪽이 다른 쪽을 부분 문자열로 포함하면 유사하다고 판단합니다. (대소문자 무시)
     *
     * 예: jobTitle="백엔드 개발자", position="백엔드" → true
     *     jobTitle="Backend Developer", position="backend" → true
     *
     * @param jobTitle 사용자 직무 (null 허용)
     * @param position 프로젝트 모집 포지션 (null 허용)
     * @return 유사 여부
     */
    private boolean isJobTitleMatched(String jobTitle, Project project) {
        if (jobTitle == null || jobTitle.isBlank()) {
            return false;
        }
        String normalizedJob = jobTitle.trim().toLowerCase(Locale.ROOT);
        List<String> positions = project.getPositionSelections() != null && !project.getPositionSelections().isEmpty()
                ? project.getPositionSelections().stream()
                .map(ProjectPosition::getPositionName)
                .toList()
                : ProjectSelectionCatalog.parsePositionCapacities(project.getPosition(), null).stream()
                .map(ProjectSelectionCatalog.PositionCapacity::name)
                .toList();
        return positions.stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedPos -> normalizedJob.contains(normalizedPos) || normalizedPos.contains(normalizedJob));
    }
}
