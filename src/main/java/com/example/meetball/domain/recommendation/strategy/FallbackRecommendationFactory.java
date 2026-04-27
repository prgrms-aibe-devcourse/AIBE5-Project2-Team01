package com.example.meetball.domain.recommendation.strategy;

import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.dto.BubbleResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FallbackRecommendationFactory {

    public RecommendationListResponseDto createFallback(List<RecommendationResponseDto> candidatePool) {
        if (candidatePool == null || candidatePool.isEmpty()) {
            return new RecommendationListResponseDto(List.of(), null, List.of(new BubbleResponseDto("이 프로젝트는 어떠신가요?", "추천")), "프로필을 바탕으로 다시 찾아볼게요.");
        }

        List<RecommendationResponseDto> results = candidatePool.stream()
                .limit(3)
                .collect(Collectors.toList());

        String axis = null;
        List<BubbleResponseDto> bubbles = new ArrayList<>();

        if (results.size() >= 2) {
            axis = selectBestAxis(results);
            if (axis != null) {
                bubbles = generateBubbles(axis, results);
                List<RecommendationResponseDto> updatedResults = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    RecommendationResponseDto dto = results.get(i);
                    String axisValue = resolveAxisValue(axis, dto);
                    updatedResults.add(RecommendationResponseDto.builder()
                            .projectId(dto.getProjectId())
                            .title(dto.getTitle())
                            .summary(dto.getSummary())
                            .score(dto.getScore())
                            .reasons(dto.getReasons())
                            .recommendationReason(dto.getRecommendationReason())
                            .axisValue(axisValue)
                            .progressMethod(dto.getProgressMethod())
                            .projectType(dto.getProjectType())
                            .positionSummary(dto.getPositionSummary())
                            .build());
                }
                results = updatedResults;
            } else {
                bubbles = List.of(
                    new BubbleResponseDto("나에게 딱 맞는 프로젝트!", "추천"),
                    new BubbleResponseDto("새로운 도전의 기회!", "도전"),
                    new BubbleResponseDto("함께 성장해요!", "성장")
                );
            }
        } else {
            bubbles = List.of(new BubbleResponseDto("이 프로젝트는 어떠신가요?", "추천"));
        }

        String question = axis == null ? "프로필을 바탕으로 추천 프로젝트를 골라봤어요." : buildFallbackQuestion(axis);
        return new RecommendationListResponseDto(results, axis, bubbles, question);
    }

    private String selectBestAxis(List<RecommendationResponseDto> projects) {
        long typeCount = projects.stream()
                .map(project -> normalize(project.getProjectType()))
                .distinct()
                .count();
        long sizeCount = projects.stream()
                .map(project -> normalize(project.getPositionSummary()))
                .distinct()
                .count();
        long progressCount = projects.stream()
                .map(project -> normalize(project.getProgressMethod()))
                .distinct()
                .count();

        if (typeCount >= 2) {
            return "프로젝트 성격";
        }
        if (sizeCount >= 2) {
            return "모집 포지션";
        }
        if (progressCount >= 2) {
            return "진행 방식";
        }
        return null;
    }

    private List<BubbleResponseDto> generateBubbles(String axis, List<RecommendationResponseDto> projects) {
        return projects.stream()
                .map(project -> {
                    if ("프로젝트 성격".equals(axis)) {
                        String type = normalize(project.getProjectType());
                        if (type.contains("스터디")) return new BubbleResponseDto("차분하게 깊이 파기", "스터디");
                        if (type.contains("스타트업")) return new BubbleResponseDto("진짜배기 실전 감각", "스타트업");
                        if (type.contains("공모전") || type.contains("해커톤")) return new BubbleResponseDto("열정 가득 도전", "해커톤");
                        if (type.contains("기업") || type.contains("정부")) return new BubbleResponseDto("현업 감각 가까이", "기업");
                        return new BubbleResponseDto("새로운 분야 탐험", "기타");
                    }
                    if ("모집 포지션".equals(axis)) {
                        String position = normalize(project.getPositionSummary());
                        if (position.contains("백엔드") || position.contains("서버")) return new BubbleResponseDto("든든한 백엔드 중심", "백엔드");
                        if (position.contains("프론트엔드") || position.contains("디자이너")) return new BubbleResponseDto("보이는 경험에 집중", "프론트엔드");
                        if (position.contains("데이터") || position.contains("ai")) return new BubbleResponseDto("데이터와 함께 성장", "데이터");
                        return new BubbleResponseDto("다양한 역할 함께", "기타");
                    }

                    String method = normalize(project.getProgressMethod());
                    if (method.contains("온라인")) return new BubbleResponseDto("집에서 편하게", "온라인");
                    if (method.contains("오프라인")) return new BubbleResponseDto("직접 만나서 몰입", "오프라인");
                    return new BubbleResponseDto("유연하게 조율해요", "혼합");
                })
                .collect(Collectors.toList());
    }

    private String resolveAxisValue(String axis, RecommendationResponseDto dto) {
        if ("프로젝트 성격".equals(axis)) {
            String type = normalize(dto.getProjectType());
            if (type.contains("스터디")) return "스터디";
            if (type.contains("스타트업")) return "스타트업";
            if (type.contains("공모전") || type.contains("해커톤")) return "해커톤";
            if (type.contains("기업") || type.contains("정부")) return "기업";
            return "기타";
        }
        if ("모집 포지션".equals(axis)) {
            String pos = normalize(dto.getPositionSummary());
            if (pos.contains("백엔드") || pos.contains("서버")) return "백엔드";
            if (pos.contains("프론트엔드") || pos.contains("디자이너")) return "프론트엔드";
            if (pos.contains("데이터") || pos.contains("ai")) return "데이터";
            return "기타";
        }
        if ("진행 방식".equals(axis)) {
            String method = normalize(dto.getProgressMethod());
            if (method.contains("온라인")) return "온라인";
            if (method.contains("오프라인")) return "오프라인";
            return "혼합";
        }
        return "";
    }

    private String buildFallbackQuestion(String axis) {
        if ("프로젝트 성격".equals(axis)) {
            return "오늘은 어떤 성격의 프로젝트가 끌리세요?";
        }
        if ("모집 포지션".equals(axis)) {
            return "이번에는 어떤 역할로 참여해보고 싶으세요?";
        }
        if ("진행 방식".equals(axis)) {
            return "어떤 방식으로 협업하는 팀이 더 잘 맞으세요?";
        }
        return "프로필을 바탕으로 추천 프로젝트를 골라봤어요.";
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
