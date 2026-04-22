package com.example.meetball.domain.recommendation.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class RecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비로그인 추천 조회는 userId 파라미터가 있어도 실패한다")
    void unauthenticatedRecommendationsRejectQueryUserId() throws Exception {
        mockMvc.perform(get("/api/recommendations")
                .param("userId", "1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 추천 조회는 세션 사용자 기준으로 성공한다")
    void authenticatedRecommendationsUseSessionUserId() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/api/recommendations")
                .session(session)
                .param("userId", "2"))
                .andExpect(status().isOk());
    }
}
