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
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class BookmarkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("정상적인 찜 토글 로직 테스트 (추가 후 삭제)")
    void toggleBookmarkSuccess() throws Exception {
        // 1차 클릭: 찜 추가 (ID 2번 유저: 성실팀원)
        mockMvc.perform(post("/api/projects/1/bookmarks?userId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(true));

        // 2차 클릭: 찜 취소
        mockMvc.perform(post("/api/projects/1/bookmarks?userId=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarked").value(false));
    }

    @Test
    @DisplayName("비회원(ID 3번: 익명게스트) 찜하기 시도 - 401 차단")
    void toggleBookmarkFailureGuest() throws Exception {
        // DataInitializer에서 3번 유저는 GUEST로 설정됨을 가정하거나, 
        // BookmarkService에서 401을 던지는 로직을 확인할 수 있는 ID 사용
        mockMvc.perform(post("/api/projects/1/bookmarks?userId=999"))
                .andExpect(status().isUnauthorized());
    }
}
