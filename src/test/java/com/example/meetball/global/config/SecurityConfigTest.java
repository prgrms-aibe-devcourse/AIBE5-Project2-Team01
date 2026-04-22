package com.example.meetball.global.config;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 2L);
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "CSRF 없는 댓글", null);

        mockMvc.perform(post("/api/projects/1/comments")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
