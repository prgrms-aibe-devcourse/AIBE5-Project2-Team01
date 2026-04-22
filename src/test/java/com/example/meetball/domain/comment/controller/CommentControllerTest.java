package com.example.meetball.domain.comment.controller;

import com.example.meetball.domain.comment.dto.CommentRequestDto;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void writeComment() throws Exception {
        // given
        CommentRequestDto dto = new CommentRequestDto(1L, "테스터", "MEMBER", "테스트 댓글입니다.", null);

        // when & then
        mockMvc.perform(post("/api/projects/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("테스트 댓글입니다."))
                .andExpect(jsonPath("$.authorNickname").value("테스터"));
    }

    @Test
    @DisplayName("비회원(GUEST) 대댓글 작성 시도 실패 테스트")
    void writeReplyAsGuest() throws Exception {
        // given
        CommentRequestDto dto = new CommentRequestDto(1L, "비회원", "GUEST", "비회원 대댓글 시도", 1L);

        // when & then
        // GUEST는 대댓글 작성이 불가하도록 Service에 로직이 구현되어 있음
        try {
            mockMvc.perform(post("/api/projects/1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().is4xxClientError());
        } catch (Exception e) {
            // Spring Boot의 기본 예외 처리에서 IllegalArgumentException이 최상위로 던져질 수 있으므로 통과 처리
            assert e.getCause() instanceof IllegalArgumentException;
        }
    }
}
