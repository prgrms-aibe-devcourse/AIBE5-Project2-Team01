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

    // ReviewRequestDto는 파라미터를 받는 생성자가 없어서 Reflection으로 세팅
    private ReviewRequestDto createRequestDto(int score) throws Exception {
        ReviewRequestDto dto = new ReviewRequestDto();
        Field scoreField = dto.getClass().getDeclaredField("score");
        scoreField.setAccessible(true);
        scoreField.set(dto, score);

        Field reviewerField = dto.getClass().getDeclaredField("reviewerNickname");
        reviewerField.setAccessible(true);
        reviewerField.set(dto, "테스터");
        return dto;
    }

    @Test
    @DisplayName("유효한 별점 등록 (201 Created) 테스트")
    void addValidReview() throws Exception {
        // given
        ReviewRequestDto dto = createRequestDto(5);

        // when & then
        mockMvc.perform(post("/api/projects/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("요약 리포트 조회 (별점 평균 검증)")
    void getReviewSummary() throws Exception {
        // given
        // DataInitializer에 의해 기본 10개의 데이터가 생성되어 4.8점이 나온다는 가정을 검증
        // when & then
        mockMvc.perform(get("/api/projects/1/reviews/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageScore").value(4.8))
                .andExpect(jsonPath("$.totalReviews").value(10));
    }
}
