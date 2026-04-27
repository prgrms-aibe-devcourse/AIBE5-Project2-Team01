package com.example.meetball.domain.recommendation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비로그인 추천 조회는 profileId 파라미터가 있어도 실패한다")
    void unauthenticatedRecommendationsRejectQueryUserId() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                .param("profileId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 추천 조회는 세션 사용자 기준으로 성공한다")
    void authenticatedRecommendationsUseSessionProfileId() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(get("/api/recommendations")
                .session(session)
                .param("profileId", "2")
                .param("recentAxes", "어떤 성격의 프로젝트가 끌리세요?")
                .param("excludeIds", "999999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations[0].projectId").isNumber())
                .andExpect(jsonPath("$.recommendations[0].title").isString())
                .andExpect(jsonPath("$.recommendations[0].recommendationReason").isString())
                .andExpect(jsonPath("$.recommendations[0].progressMethod").isString())
                .andExpect(jsonPath("$.recommendations[0].projectType").isString())
                .andExpect(jsonPath("$.axis").value(notNullValue()))
                .andExpect(jsonPath("$.question").value(notNullValue()))
                .andExpect(jsonPath("$.bubbles").isArray());
    }
}
