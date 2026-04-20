package com.example.meetball.domain.review.controller;

import com.example.meetball.domain.review.dto.ReviewRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 중복 등록 방지 테스트")
    void duplicateReviewTest() throws Exception {
        // given: 이미 '팀장님'이 리뷰를 남긴 상태 (DataInitializer)
        ReviewRequestDto dto = new ReviewRequestDto(5.0, "팀장님", "LEADER", "", "중복 테스트");

        // when & then: 동일한 프로젝트에 다시 등록 시도
        mockMvc.perform(post("/api/projects/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().is4xxClientError()); // 400 에러 기대
    }

    @Test
    @DisplayName("리뷰 100자 초과 등록 시 실패 테스트")
    void contentLimitTest() throws Exception {
        // given: 100자가 넘는 문자열 생성
        String longContent = "A".repeat(101);
        ReviewRequestDto dto = new ReviewRequestDto(5.0, "성실팀원", "MEMBER", "", longContent);

        // when & then
        mockMvc.perform(post("/api/projects/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("프로젝트 미참여자의 리뷰 등록 시도 실패 테스트")
    void nonParticipantReviewTest() throws Exception {
        // given: 프로젝트에 참여하지 않은 사용자 닉네임
        ReviewRequestDto dto = new ReviewRequestDto(5.0, "불청객", "USER", "", "몰래 리뷰 남기기");

        // when & then: 등록 시도 시 실패(400 에러) 기대
        mockMvc.perform(post("/api/projects/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("요약 리포트 조회 (별점 평균 검증)")
    void getReviewSummary() throws Exception {
        // when & then
        mockMvc.perform(get("/api/projects/1/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(1));
    }
}
