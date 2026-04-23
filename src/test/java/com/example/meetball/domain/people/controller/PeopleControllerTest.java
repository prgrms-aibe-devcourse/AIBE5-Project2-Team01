package com.example.meetball.domain.people.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class PeopleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    @Test
    @DisplayName("비회원은 사람 프로필 API를 조회할 수 없다")
    void guestCannotReadPeopleProfileApi() throws Exception {
        mockMvc.perform(get("/api/people/2/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("회원은 사람 프로필 API에서 공개 프로필을 조회한다")
    void memberCanReadPeopleProfileApi() throws Exception {
        mockMvc.perform(get("/api/people/2/profile").session(session(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.nickname").value("성실한리트리버"))
                .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    @DisplayName("회원은 사람 참여 프로젝트 API를 조회한다")
    void memberCanReadPeopleProjectsApi() throws Exception {
        mockMvc.perform(get("/api/people/2/projects").session(session(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].projectId").exists())
                .andExpect(jsonPath("$[0].canReview").doesNotExist());
    }
}
