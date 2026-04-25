package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * Google Gemini API를 사용해 추천 결과를 자연어로 해석합니다.
 */
@Service
public class GeminiRecommendationInterpreter implements LlmRecommendationInterpreter {

    private static final Logger log = LoggerFactory.getLogger(GeminiRecommendationInterpreter.class);

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public RecommendationListResponseDto interpret(Profile profile, List<RecommendationResponseDto> candidatePool, List<String> recentAxes) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[LLM] Gemini API Key가 비어 있어 fallback으로 전환합니다.");
            return null;
        }
        if (candidatePool == null || candidatePool.isEmpty()) {
            return null;
        }

        try {
            String prompt = buildPrompt(profile, candidatePool, recentAxes);
            String response = callGeminiApi(prompt);
            LlmResult result = parseLlmResponse(response);

            if (result == null || !isValidResult(result, candidatePool)) {
                log.warn("[LLM] Gemini 응답 검증 실패. fallback으로 전환합니다.");
                return null;
            }

            List<RecommendationResponseDto> finalProjects = new ArrayList<>();
            for (int i = 0; i < result.selectedProjectIds.size(); i++) {
                Long selectedProjectId = result.selectedProjectIds.get(i);
                RecommendationResponseDto chosenDto = candidatePool.stream()
                        .filter(project -> project.getProjectId().equals(selectedProjectId))
                        .findFirst()
                        .orElse(null);

                if (chosenDto == null) {
                    continue;
                }

                String axisValue = result.bubbles != null && !result.bubbles.isEmpty()
                        ? result.bubbles.get(Math.min(i, result.bubbles.size() - 1))
                        : "";
                String recommendationReason = result.recommendationReasons != null && !result.recommendationReasons.isEmpty()
                        ? result.recommendationReasons.get(Math.min(i, result.recommendationReasons.size() - 1))
                        : chosenDto.getRecommendationReason();

                finalProjects.add(RecommendationResponseDto.builder()
                        .projectId(chosenDto.getProjectId())
                        .title(chosenDto.getTitle())
                        .summary(chosenDto.getSummary())
                        .score(chosenDto.getScore())
                        .reasons(chosenDto.getReasons())
                        .recommendationReason(recommendationReason)
                        .axisValue(axisValue)
                        .progressMethod(chosenDto.getProgressMethod())
                        .projectType(chosenDto.getProjectType())
                        .positionSummary(chosenDto.getPositionSummary())
                        .build());
            }

            return new RecommendationListResponseDto(finalProjects, result.axis, result.bubbles, result.question);
        } catch (Exception exception) {
            log.error("[LLM] Gemini 해석 실패: {}", exception.getMessage());
            return null;
        }
    }

    private String buildPrompt(Profile profile, List<RecommendationResponseDto> candidatePool, List<String> recentAxes) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are Meetball, a warm and playful AI mascot for project matching.\n");
        builder.append("All output text must be in Korean. Return valid JSON only.\n\n");

        builder.append("Profile:\n");
        builder.append("- nickname: ").append(profile.getNickname()).append('\n');
        builder.append("- position: ").append(profile.getPosition() == null ? "" : profile.getPosition()).append('\n');
        builder.append("- techStack: ").append(profile.getTechStack() == null ? "" : profile.getTechStack()).append("\n\n");

        builder.append("Candidate Pool:\n");
        for (RecommendationResponseDto recommendation : candidatePool) {
            builder.append("- projectId: ").append(recommendation.getProjectId())
                    .append(", title: ").append(recommendation.getTitle())
                    .append(", summary: ").append(recommendation.getSummary())
                    .append(", progressMethod: ").append(recommendation.getProgressMethod())
                    .append(", projectType: ").append(recommendation.getProjectType())
                    .append(", positionSummary: ").append(recommendation.getPositionSummary())
                    .append('\n');
        }

        if (recentAxes != null && !recentAxes.isEmpty()) {
            builder.append("\nRecently used axes: ").append(String.join(", ", recentAxes)).append('\n');
            builder.append("Try hard to avoid reusing those axes.\n");
        }

        builder.append("""

                Tasks:
                1. Choose exactly 3 project IDs from the candidate pool.
                2. Create one meaningful axis that differentiates the chosen projects.
                3. Write one short Korean question for that axis.
                4. Create 2 or 3 short Korean bubbles for the axis.
                5. Write 3 short Korean recommendationReasons, each 10-20 characters max.

                JSON shape:
                {
                  "selectedProjectIds": [1, 2, 3],
                  "axis": "...",
                  "question": "...",
                  "bubbles": ["...", "...", "..."],
                  "recommendationReasons": ["...", "...", "..."]
                }
                """);
        return builder.toString();
    }

    private String callGeminiApi(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        String urlWithKey = apiUrl + "?key=" + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.exchange(urlWithKey, HttpMethod.POST, entity, String.class);
            return response.getBody();
        } catch (HttpStatusCodeException exception) {
            if (exception.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                log.warn("[LLM] Gemini rate limit 초과(429). fallback으로 전환합니다.");
                return null;
            }
            if (exception.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                log.warn("[LLM] Gemini 서비스 일시 불가(503). fallback으로 전환합니다.");
                return null;
            }
            throw exception;
        }
    }

    private LlmResult parseLlmResponse(String response) {
        try {
            if (response == null || response.isBlank()) {
                return null;
            }
            JsonNode root = objectMapper.readTree(response);
            JsonNode candidates = root.path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return null;
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return null;
            }

            String text = parts.get(0).path("text").asText();
            text = text.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(text, LlmResult.class);
        } catch (Exception exception) {
            log.error("[LLM] Gemini JSON 파싱 실패: {}", exception.getMessage());
            return null;
        }
    }

    private boolean isValidResult(LlmResult result, List<RecommendationResponseDto> candidatePool) {
        if (result == null
                || result.selectedProjectIds == null
                || result.selectedProjectIds.size() != 3
                || result.axis == null
                || result.axis.isBlank()
                || result.question == null
                || result.question.isBlank()
                || result.bubbles == null
                || result.bubbles.size() < 2
                || result.recommendationReasons == null
                || result.recommendationReasons.size() != 3) {
            return false;
        }

        List<Long> candidateIds = candidatePool.stream()
                .map(RecommendationResponseDto::getProjectId)
                .collect(Collectors.toList());

        return result.selectedProjectIds.stream().allMatch(candidateIds::contains);
    }

    private static class LlmResult {
        public List<Long> selectedProjectIds;
        public String axis;
        public String question;
        public List<String> bubbles;
        public List<String> recommendationReasons;
    }
}
