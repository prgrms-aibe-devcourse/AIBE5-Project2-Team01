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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    @Test
    @DisplayName("댓글 작성 성공 테스트")
    void writeComment() throws Exception {
        // given
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "테스트 댓글입니다.", null);

        // when & then
        mockMvc.perform(post("/api/projects/1/comments")
                .session(session(2L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("테스트 댓글입니다."))
                .andExpect(jsonPath("$.authorNickname").value("성실한리트리버"));
    }

    @Test
    @DisplayName("프로젝트 비멤버 대댓글 작성 시도 실패 테스트")
    void writeReplyAsNonMember() throws Exception {
        // given
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "비멤버 대댓글 시도", 1L);

        // when & then
        mockMvc.perform(post("/api/projects/1/comments")
                .session(session(3L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("다른 사용자의 댓글 수정 시도 실패 테스트")
    void updateOtherUsersCommentFails() throws Exception {
        // given
        Long commentId = createCommentAndReturnId("수정 권한 테스트 댓글");
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, "남의 댓글 수정", null);

        // when & then
        mockMvc.perform(put("/api/projects/1/comments/" + commentId)
                .session(session(3L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("다른 사용자의 댓글 삭제 시도 실패 테스트")
    void deleteOtherUsersCommentFails() throws Exception {
        // given
        Long commentId = createCommentAndReturnId("삭제 권한 테스트 댓글");

        // when & then
        mockMvc.perform(delete("/api/projects/1/comments/" + commentId)
                .session(session(3L)))
                .andExpect(status().isForbidden());
    }

    private Long createCommentAndReturnId(String content) throws Exception {
        CommentRequestDto dto = new CommentRequestDto(1L, null, null, content, null);
        MvcResult result = mockMvc.perform(post("/api/projects/1/comments")
                .session(session(2L))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
