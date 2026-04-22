package com.example.meetball.global.config;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("세션 기반 쓰기 요청은 CSRF 토큰 없이 거부된다")
    void stateChangingRequestWithoutCsrfIsRejected() throws Exception {
        MockHttpSession session = authenticatedSession(2L);
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "CSRF 없는 댓글", null);

        mockMvc.perform(post("/api/projects/1/comments")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("공개 프로젝트 목록 API는 로그인 없이 조회된다")
    void publicProjectListIsAvailableWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].bookmarkCount").isNumber())
                .andExpect(jsonPath("$.content[0].readCount").isNumber())
                .andExpect(jsonPath("$.content[0].bookmarked").value(false));
    }

    @Test
    @DisplayName("댓글 목록은 게스트가 조회할 수 있지만 작성은 로그인 없이는 차단된다")
    void commentReadIsPublicButWriteRequiresLogin() throws Exception {
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "게스트 댓글", null);

        mockMvc.perform(get("/api/projects/1/comments"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/projects/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("마이페이지 공개 항목 API도 로그인 없이 조회할 수 없다")
    void myPagePublicSectionsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/mypage/profile").param("userId", "1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/mypage/projects").param("userId", "1"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/mypage/projects/completed").param("userId", "1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("인증 세션과 CSRF 토큰이 있는 쓰기 요청은 허용된다")
    void stateChangingRequestWithCsrfIsAllowed() throws Exception {
        MockHttpSession session = authenticatedSession(2L);
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "CSRF 포함 댓글", null);
        MvcResult pageResult = mockMvc.perform(get("/projects/1").session(session))
                .andExpect(status().isOk())
                .andReturn();
        String html = pageResult.getResponse().getContentAsString();
        String csrfToken = extractMetaContent(html, "_csrf");
        String csrfHeader = extractMetaContent(html, "_csrf_header");
        Cookie csrfCookie = pageResult.getResponse().getCookie("XSRF-TOKEN");

        mockMvc.perform(post("/api/projects/1/comments")
                .session(session)
                .cookie(csrfCookie)
                .header(csrfHeader, csrfToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    private MockHttpSession authenticatedSession(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        session.setAttribute("userNickname", "security-test-user");

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(
                "security-test-user",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        ));
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        return session;
    }

    private String extractMetaContent(String html, String name) {
        String marker = "name=\"" + name + "\"";
        int nameIndex = html.indexOf(marker);
        if (nameIndex < 0) {
            throw new AssertionError("CSRF meta tag not found: " + name);
        }
        int contentIndex = html.indexOf("content=\"", nameIndex);
        int valueStart = contentIndex + "content=\"".length();
        int valueEnd = html.indexOf("\"", valueStart);
        return html.substring(valueStart, valueEnd);
    }
}
