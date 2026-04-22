package com.example.meetball.domain.mypage.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    @Test
    @DisplayName("마이페이지 프로필 정보를 성공적으로 조회한다")
    void getMyProfileTest() throws Exception {
        // given: DataInitializer에 의해 1번 유저(초코푸들)가 생성되어 있음
        Long userId = 1L;

        // when & then
        mockMvc.perform(get("/api/mypage/profile")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("초코푸들"))
                .andExpect(jsonPath("$.role").value("LEADER"))
                .andExpect(jsonPath("$.meetBallIndex").value(36.7))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 참여 중인 프로젝트 목록을 조회한다")
    void getMyProjectsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/projects")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].userRole").exists())
                .andExpect(jsonPath("$[0].canReview").exists()) // 추가됨: 리뷰 가능 여부 필드 확인
                .andExpect(jsonPath("$[0].dDay").exists())     // 추가됨: D-Day 필드 확인
                .andDo(print());
    }

    @Test
    @DisplayName("마이페이지 API로 타인의 프로필이나 프로젝트를 조회할 수 없다")
    void myPageRejectsOtherUserScope() throws Exception {
        mockMvc.perform(get("/api/mypage/profile")
                        .param("userId", "2")
                        .session(session(1L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/mypage/projects")
                        .param("userId", "2")
                        .session(session(1L)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/mypage/projects/completed")
                        .param("userId", "2")
                        .session(session(1L)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("사용자가 지원한 프로젝트 목록을 조회한다")
    void getMyApplicationsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/applications")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 최근에 읽은 프로젝트 목록을 조회한다")
    void getRecentReadsTest() throws Exception {
        Long userId = 1L;

        mockMvc.perform(get("/api/mypage/recent-reads")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 관심 등록한 프로젝트 목록을 조회한다")
    void getMyBookmarksTest() throws Exception {
        Long userId = 3L; // 열정고양이

        mockMvc.perform(get("/api/mypage/bookmarks")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("AI 기반 헬스케어 모바일 앱 개발"))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 받은 피어 리뷰 목록을 조회한다")
    void getMyReviewsTest() throws Exception {
        Long userId = 1L; // 초코푸들

        mockMvc.perform(get("/api/mypage/reviews")
                        .session(session(userId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reviewerNickname").value("코딩하는비글"))
                .andExpect(jsonPath("$[0].projectTitle").value("반려견 케어 서비스 [멍멍 비서]"))
                .andDo(print());
    }
}
