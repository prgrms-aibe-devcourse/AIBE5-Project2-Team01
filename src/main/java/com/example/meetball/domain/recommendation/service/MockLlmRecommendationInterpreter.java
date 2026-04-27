package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.dto.BubbleResponseDto;
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

    private static final List<BubbleSet> QUESTION_SETS = List.of(
            new BubbleSet("어떤 성격의 프로젝트가 끌리세요?", List.of(
                    new BubbleResponseDto("스타트업에서 실전 경험", "스타트업"),
                    new BubbleResponseDto("스터디로 핵심 지식 탄탄히", "스터디"),
                    new BubbleResponseDto("공모전/해커톤 도전", "해커톤")
            )),
            new BubbleSet("오늘은 어떤 분위기의 팀을 만나고 싶으세요?", List.of(
                    new BubbleResponseDto("북적이는 대규모 팀", "기업"),
                    new BubbleResponseDto("조용히 온라인 몰입", "온라인"),
                    new BubbleResponseDto("오프라인에서 체계적으로", "오프라인")
            )),
            new BubbleSet("어떤 방식으로 성장하고 싶으세요?", List.of(
                    new BubbleResponseDto("빠르게 부딪혀보기", "스타트업"),
                    new BubbleResponseDto("차분하게 스터디하며 파기", "스터디"),
                    new BubbleResponseDto("멘토와 함께 배우기", "기업")
            )),
            new BubbleSet("지금 가장 필요한 경험은 뭔가요?", List.of(
                    new BubbleResponseDto("실전 사용자 경험", "스타트업"),
                    new BubbleResponseDto("포트폴리오 완성", "공모전"),
                    new BubbleResponseDto("팀워크 역량 강화", "스터디")
            )),
            new BubbleSet("어떤 협업 방식이 더 편하세요?", List.of(
                    new BubbleResponseDto("자유롭게 온라인으로", "온라인"),
                    new BubbleResponseDto("오프라인 직접 만나 몰입", "오프라인"),
                    new BubbleResponseDto("유연하게 혼합 조율", "혼합")
            ))
    );

    private static class BubbleSet {
        String question;
        List<BubbleResponseDto> bubbles;
        BubbleSet(String question, List<BubbleResponseDto> bubbles) {
            this.question = question;
            this.bubbles = bubbles;
        }
    }

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

        BubbleSet selected = pickQuestionSet(top3, recentAxes);
        String axis = selected.question;
        String question = selected.question;
        List<BubbleResponseDto> bubbles = selected.bubbles;

        List<RecommendationResponseDto> result = new ArrayList<>();
        for (int i = 0; i < top3.size(); i++) {
            RecommendationResponseDto source = top3.get(i);
            
            // 프론트엔드 강조 및 재정렬을 위해 프로젝트의 실제 태그에서 키워드 추출하여 axisValue로 설정
            String mockAxisValue = extractKeywordForHighlight(source);

            result.add(RecommendationResponseDto.builder()
                    .projectId(source.getProjectId())
                    .title(source.getTitle())
                    .summary(source.getSummary())
                    .score(source.getScore())
                    .reasons(source.getReasons())
                    .recommendationReason(resolveReason(source))
                    .axisValue(mockAxisValue)
                    .progressMethod(source.getProgressMethod())
                    .projectType(source.getProjectType())
                    .positionSummary(source.getPositionSummary())
                    .build());
        }

        return new RecommendationListResponseDto(result, axis, bubbles, question);
    }

    private String extractKeywordForHighlight(RecommendationResponseDto dto) {
        String type = dto.getProjectType() != null ? dto.getProjectType() : "";
        String method = dto.getProgressMethod() != null ? dto.getProgressMethod() : "";

        if (type.contains("스타트업")) return "스타트업";
        if (type.contains("공모전") || type.contains("해커톤")) return "해커톤";
        if (type.contains("스터디")) return "스터디";
        if (type.contains("기업")) return "기업";
        
        if (method.contains("오프라인")) return "오프라인";
        if (method.contains("온라인")) return "온라인";
        if (method.contains("혼합")) return "혼합";

        return "";
    }

    private BubbleSet pickQuestionSet(List<RecommendationResponseDto> projects, List<String> recentAxes) {
        Set<String> recentSet = recentAxes == null ? Set.of() : new HashSet<>(recentAxes);

        for (BubbleSet set : QUESTION_SETS) {
            if (!recentSet.contains(set.question)) {
                return set;
            }
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
