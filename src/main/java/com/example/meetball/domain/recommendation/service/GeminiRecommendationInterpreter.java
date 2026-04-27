package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.dto.BubbleResponseDto;
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
                        ? result.bubbles.get(Math.min(i, result.bubbles.size() - 1)).value
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

            List<BubbleResponseDto> finalBubbles = result.bubbles.stream()
                    .map(b -> new BubbleResponseDto(b.label, b.value))
                    .collect(Collectors.toList());

            return new RecommendationListResponseDto(finalProjects, result.axis, finalBubbles, result.question);
        } catch (Exception exception) {
            log.error("[LLM] Gemini 해석 실패: {}", exception.getMessage());
            return null;
        }
    }

    private String buildPrompt(Profile profile, List<RecommendationResponseDto> candidatePool, List<String> recentAxes) {
        StringBuilder builder = new StringBuilder();
        builder.append("You are 'Meetball', an AI-powered brown poodle mascot for a team-matching service. ");
        builder.append("Your mission is to act as an AI Curator. You will receive a Candidate Pool of projects and the User's Profile. ");
        builder.append("You MUST select the 3 BEST projects for this user, determine a logical comparison axis, and generate engaging text.\n\n");

        builder.append("Profile:\n");
        builder.append("- nickname: ").append(profile.getNickname()).append('\n');
        builder.append("- position: ").append(profile.getPosition() == null ? "" : profile.getPosition()).append('\n');
        builder.append("- techStack: ").append(profile.getTechStack() == null ? "" : profile.getTechStack()).append("\n\n");

        builder.append("Candidate Pool (up to 10 projects):\n");
        for (RecommendationResponseDto recommendation : candidatePool) {
            builder.append("- projectId: ").append(recommendation.getProjectId())
                    .append(", title: ").append(recommendation.getTitle())
                    .append(", summary: ").append(recommendation.getSummary())
                    .append(", progressMethod: ").append(recommendation.getProgressMethod())
                    .append(", projectType: ").append(recommendation.getProjectType())
                    .append(", positionSummary: ").append(recommendation.getPositionSummary())
                    .append('\n');
        }

        builder.append("\nYour Task (Output in Korean):\n");
        builder.append("1. Select exactly 3 Project IDs from the Candidate Pool that are the best fit for the user.\n");
        builder.append("2. Determine a single comparison AXIS that meaningfully differentiates the 3 chosen projects.\n");
        builder.append("   - CRITICAL: NEVER use boring, superficial axes like '진행 방식', '프로젝트 유형', '팀 규모'. ");
        builder.append("Dig deeper into the project summaries to find psychological or vibe-based differences.\n");

        if (recentAxes != null && !recentAxes.isEmpty()) {
            builder.append("\n   - FIRM CONSTRAINT: These axes were RECENTLY used: [").append(String.join(", ", recentAxes)).append("]. ");
            builder.append("Try very hard to choose a DIFFERENT angle.\n");
        }

        builder.append("""

                3. Create a short, character-rich Korean question (1-2 sentences max).
                4. Create 3 distinct bubbles. Each bubble MUST be an object with { "label": "...", "value": "..." }.
                   - label: A natural, evocative Korean sentence (e.g., "스타트업에서 실전처럼").
                   - value: A single representative keyword found in the project's tags (e.g., "스타트업", "오프라인", "스터디"). This is used for internal matching.
                5. Write a recommendation reason (recommendationReasons) for each chosen project.

                Response Requirements:
                - Language: All output text in Korean except JSON keys.
                - Tone: Enthusiastic, cute, and helpful (Meetball vibe).
                - Response MUST be ONLY valid JSON.

                JSON Structure:
                {
                  "selectedProjectIds": [id1, id2, id3],
                  "axis": "...",
                  "question": "...",
                  "bubbles": [
                    { "label": "버블 문구 1", "value": "매칭 키워드 1" },
                    { "label": "버블 문구 2", "value": "매칭 키워드 2" },
                    { "label": "버블 문구 3", "value": "매칭 키워드 3" }
                  ],
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
                log.warn("[LLM] Gemini rate limit 초과(429). 2초 대기 후 1회 재시도합니다.");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return null;
                }
                try {
                    ResponseEntity<String> retryResponse = restTemplate.exchange(urlWithKey, HttpMethod.POST, entity, String.class);
                    return retryResponse.getBody();
                } catch (Exception retryEx) {
                    log.error("[LLM] Gemini 재시도 실패: {}", retryEx.getMessage());
                    return null;
                }
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
        public List<BubbleResult> bubbles;
        public List<String> recommendationReasons;
    }

    private static class BubbleResult {
        public String label;
        public String value;
    }
}
