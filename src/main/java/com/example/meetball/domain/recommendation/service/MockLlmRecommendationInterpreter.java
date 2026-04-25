package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 개발/테스트용 Mock 인터프리터.
 * 실제 Gemini 호출 없이 feat 브랜치가 기대하는 응답 구조를 동일하게 내려줍니다.
 */
@Component
public class MockLlmRecommendationInterpreter implements LlmRecommendationInterpreter {

    private static final Logger log = LoggerFactory.getLogger(MockLlmRecommendationInterpreter.class);

    private static final List<String[]> QUESTION_SETS = List.of(
            new String[]{"어떤 성격의 프로젝트가 끌리세요?", "핵심 지식 탄탄히", "진짜 서비스 경험", "진짜배기 실전 감각"},
            new String[]{"오늘은 어떤 분위기의 팀을 만나고 싶으세요?", "북적이는 팀", "조용히 몰입", "체계적으로 달리기"},
            new String[]{"어떤 방식으로 성장하고 싶으세요?", "빠르게 부딪혀보기", "차분하게 깊이 파기", "멘토와 함께 배우기"},
            new String[]{"지금 가장 필요한 경험은 뭔가요?", "실전 사용자 경험", "포트폴리오 완성", "팀워크 역량 강화"},
            new String[]{"어떤 협업 방식이 더 편하세요?", "자유롭게 원격으로", "직접 만나 몰입", "유연하게 조율하며"}
    );

    private static final Map<String, String> TYPE_REASON_MAP;

    static {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("스타트업", "진짜배기 실전 감각");
        map.put("공모전", "열정 가득 도전의 무대");
        map.put("해커톤", "열정 가득 도전의 무대");
        map.put("스터디", "차분하게 깊이 파기");
        map.put("기업연계", "현업 감각 제대로 익히기");
        map.put("정부연계", "현업 감각 제대로 익히기");
        TYPE_REASON_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public RecommendationListResponseDto interpret(Profile profile, List<RecommendationResponseDto> candidatePool, List<String> recentAxes) {
        log.info("[Mock-LLM] 개발 모드: profileId={}, candidatePool size={}",
                profile != null ? profile.getId() : null,
                candidatePool != null ? candidatePool.size() : 0);

        if (candidatePool == null || candidatePool.isEmpty()) {
            return null;
        }

        List<RecommendationResponseDto> top3 = candidatePool.stream()
                .limit(3)
                .collect(Collectors.toList());

        String[] selected = pickQuestionSet(top3, recentAxes);
        String axis = selected[0];
        String question = selected[0];
        List<String> bubbles = Arrays.stream(selected)
                .skip(1)
                .limit(top3.size())
                .collect(Collectors.toList());

        List<RecommendationResponseDto> result = new ArrayList<>();
        for (int i = 0; i < top3.size(); i++) {
            RecommendationResponseDto source = top3.get(i);
            String bubble = bubbles.isEmpty()
                    ? ""
                    : bubbles.get(Math.min(i, bubbles.size() - 1));

            result.add(RecommendationResponseDto.builder()
                    .projectId(source.getProjectId())
                    .title(source.getTitle())
                    .summary(source.getSummary())
                    .score(source.getScore())
                    .reasons(source.getReasons())
                    .recommendationReason(resolveReason(source))
                    .axisValue(bubble)
                    .progressMethod(source.getProgressMethod())
                    .projectType(source.getProjectType())
                    .positionSummary(source.getPositionSummary())
                    .build());
        }

        return new RecommendationListResponseDto(result, axis, bubbles, question);
    }

    private String[] pickQuestionSet(List<RecommendationResponseDto> projects, List<String> recentAxes) {
        Set<String> recentSet = recentAxes == null ? Set.of() : new HashSet<>(recentAxes);

        long typeVariety = projects.stream()
                .map(project -> project.getProjectType() == null ? "" : project.getProjectType())
                .distinct()
                .count();
        long methodVariety = projects.stream()
                .map(project -> project.getProgressMethod() == null ? "" : project.getProgressMethod())
                .distinct()
                .count();

        for (String[] set : QUESTION_SETS) {
            String axis = set[0];
            if (recentSet.contains(axis)) {
                continue;
            }
            if (typeVariety >= 2 && set == QUESTION_SETS.get(0)) {
                return set;
            }
            if (methodVariety >= 2 && set == QUESTION_SETS.get(4)) {
                return set;
            }
            return set;
        }

        return QUESTION_SETS.get(0);
    }

    private String resolveReason(RecommendationResponseDto dto) {
        String projectType = dto.getProjectType() == null ? "" : dto.getProjectType();
        for (Map.Entry<String, String> entry : TYPE_REASON_MAP.entrySet()) {
            if (projectType.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        String progressMethod = dto.getProgressMethod() == null ? "" : dto.getProgressMethod();
        if (progressMethod.contains("오프라인")) {
            return "직접 만나 불태워봐요";
        }
        if (progressMethod.contains("온라인")) {
            return "자유롭게 원격으로";
        }
        return "꾸준히 성장하는 곳";
    }
}
