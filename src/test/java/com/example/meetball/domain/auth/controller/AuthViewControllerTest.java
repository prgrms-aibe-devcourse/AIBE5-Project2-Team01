package com.example.meetball.domain.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "google.client.id=test-google-client.apps.googleusercontent.com")
@AutoConfigureMockMvc(addFilters = false)
class AuthViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("비로그인 홈 화면은 게스트용 헤더를 보여준다")
    void guestHomeShowsLoggedOutHeader() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그인")))
                .andExpect(content().string(not(containsString("김밋볼"))))
                .andExpect(content().string(not(containsString("meetball_dev@google.com"))));
    }

    @Test
    @DisplayName("로그인 세션이 있으면 홈 헤더에 현재 사용자 정보가 보인다")
    void loggedInHomeShowsCurrentUserHeader() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("초코푸들")))
                .andExpect(content().string(containsString("leader@meetball.com")));
    }

    @Test
    @DisplayName("로그인 페이지는 Google Identity Services 설정을 렌더링한다")
    void loginPageRendersGoogleSignIn() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("data-client_id=\"test-google-client.apps.googleusercontent.com\"")))
                .andExpect(content().string(containsString("/api/auth/google")));
    }

    @Test
    @DisplayName("비로그인 마이페이지 접근은 기본 사용자 대신 로그인 화면으로 유도한다")
    void guestMyPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?redirect=/user/mypage"));
    }
}
