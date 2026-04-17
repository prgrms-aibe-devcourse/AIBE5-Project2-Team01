package com.example.meetball.domain.mypage.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("마이페이지 프로필 정보를 성공적으로 조회한다")
    void getMyProfileTest() throws Exception {
        // given: DataInitializer에 의해 1번 유저(팀장님)가 생성되어 있음
        Long userId = 1L;

        // when & then
        mockMvc.perform(get("/api/mypage/profile")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("팀장님"))
                .andExpect(jsonPath("$.role").value("LEADER"))
                .andExpect(jsonPath("$.meetBallIndex").value(36.7))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 참여 중인 프로젝트 목록을 조회한다")
    void getMyProjectsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/projects")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].userRole").exists())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 지원한 프로젝트 목록을 조회한다")
    void getMyApplicationsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/applications")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 최근에 읽은 프로젝트 목록을 조회한다")
    void getRecentReadsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/recent-reads")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 관심 등록한 프로젝트 목록을 조회한다")
    void getMyBookmarksTest() throws Exception {
        Long userId = 1L; // 팀장님

        mockMvc.perform(get("/api/mypage/bookmarks")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("블록체인 기반 투표 시스템"))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 받은 피어 리뷰 목록을 조회한다")
    void getMyReviewsTest() throws Exception {
        Long userId = 1L; // 팀장님

        mockMvc.perform(get("/api/mypage/reviews")
                        .param("userId", String.valueOf(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reviewerNickname").value("열정개발자"))
                .andExpect(jsonPath("$[0].projectTitle").value("AI 헬스케어 모바일 앱"))
                .andDo(print());
    }
}
