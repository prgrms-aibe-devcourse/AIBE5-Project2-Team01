package com.example.meetball.domain.bookmark.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상적인 찜 토글 로직 테스트 (추가 후 삭제)")
    void toggleBookmarkSuccess() throws Exception {
        // 1차 클릭: 찜 추가
        mockMvc.perform(post("/api/projects/1/bookmarks?userNickname=tester1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true))
                .andExpect(jsonPath("$.totalBookmarks").value(1));

        // 2차 클릭: 찜 취소
        mockMvc.perform(post("/api/projects/1/bookmarks?userNickname=tester1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(false))
                .andExpect(jsonPath("$.totalBookmarks").value(0));
    }

    @Test
    @DisplayName("비회원(GUEST) 찜하기 시도 - 401 차단")
    void toggleBookmarkFailureGuest() throws Exception {
        mockMvc.perform(post("/api/projects/1/bookmarks?userNickname=GUEST"))
                .andExpect(status().isUnauthorized()); // BookmarkController에서 401 Body로 반환
    }
}
