package com.example.meetball.domain.recommendation.service;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.recommendation.dto.RecommendationListResponseDto;
import com.example.meetball.domain.recommendation.dto.RecommendationResponseDto;
import com.example.meetball.domain.recommendation.strategy.FallbackRecommendationFactory;
import com.example.meetball.domain.recommendation.strategy.RecommendationStrategy;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 규칙 기반 후보군 + 선택적 LLM 해석 + fallback을 제공하는 추천 서비스.
 */
@Service
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L;

    @Value("${llm.enabled:false}")
    private boolean llmEnabled;

    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final RecommendationStrategy recommendationStrategy;
    private final GeminiRecommendationInterpreter geminiRecommendationInterpreter;
    private final MockLlmRecommendationInterpreter mockLlmRecommendationInterpreter;
    private final FallbackRecommendationFactory fallbackRecommendationFactory;
    private final ConcurrentHashMap<String, CachedResult> resultCache = new ConcurrentHashMap<>();

    public RecommendationService(ProjectRepository projectRepository,
                                 ProfileRepository profileRepository,
                                 RecommendationStrategy recommendationStrategy,
                                 GeminiRecommendationInterpreter geminiRecommendationInterpreter,
                                 MockLlmRecommendationInterpreter mockLlmRecommendationInterpreter,
                                 FallbackRecommendationFactory fallbackRecommendationFactory) {
        this.projectRepository = projectRepository;
        this.profileRepository = profileRepository;
        this.recommendationStrategy = recommendationStrategy;
        this.geminiRecommendationInterpreter = geminiRecommendationInterpreter;
        this.mockLlmRecommendationInterpreter = mockLlmRecommendationInterpreter;
        this.fallbackRecommendationFactory = fallbackRecommendationFactory;
    }

    @Transactional(readOnly = true)
    public RecommendationListResponseDto recommend(Long profileId, List<Long> excludeIds, List<String> recentAxes) {
        String cacheKey = buildCacheKey(profileId, excludeIds);
        CachedResult cachedResult = resultCache.get(cacheKey);
        if (cachedResult != null && !cachedResult.isExpired()) {
            log.info("[Recommendation] cache hit. profileId={}, key={}", profileId, cacheKey);
            return cachedResult.result();
        }

        Profile profile = profileRepository.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("해당 프로필을 찾을 수 없습니다. profileId=" + profileId));

        List<Project> openProjects = projectRepository.findRecommendationCandidates(
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_COMPLETED
        );

        if (excludeIds != null && !excludeIds.isEmpty()) {
            List<Project> filtered = openProjects.stream()
                    .filter(project -> !excludeIds.contains(project.getId()))
                    .collect(Collectors.toList());
            if (filtered.size() >= 3) {
                openProjects = filtered;
            }
        }

        List<RecommendationResponseDto> candidatePool = recommendationStrategy.generateCandidatePool(profile, openProjects);

        RecommendationListResponseDto finalResult;
        if (llmEnabled) {
            log.info("[Recommendation] llm.enabled=true -> Gemini interpreter");
            finalResult = geminiRecommendationInterpreter.interpret(profile, candidatePool, recentAxes);
            if (finalResult == null) {
                log.warn("[Recommendation] Gemini 실패, fallback으로 전환합니다.");
                finalResult = fallbackRecommendationFactory.createFallback(candidatePool);
            }
        } else {
            log.info("[Recommendation] llm.enabled=false -> Mock interpreter");
            finalResult = mockLlmRecommendationInterpreter.interpret(profile, candidatePool, recentAxes);
            if (finalResult == null) {
                finalResult = fallbackRecommendationFactory.createFallback(candidatePool);
            }
        }

        resultCache.put(cacheKey, new CachedResult(finalResult));
        if (resultCache.size() > 100) {
            resultCache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        }
        return finalResult;
    }

    @Transactional(readOnly = true)
    public RecommendationListResponseDto recommend(Long profileId) {
        return recommend(profileId, null, null);
    }

    private String buildCacheKey(Long profileId, List<Long> excludeIds) {
        String ids = excludeIds == null || excludeIds.isEmpty()
                ? "none"
                : excludeIds.stream().sorted().map(Objects::toString).collect(Collectors.joining(","));
        return profileId + "|" + ids;
    }

    private record CachedResult(RecommendationListResponseDto result, long createdAt) {
        private CachedResult(RecommendationListResponseDto result) {
            this(result, System.currentTimeMillis());
        }

        private boolean isExpired() {
            return System.currentTimeMillis() - createdAt > CACHE_TTL_MS;
        }
    }
}
