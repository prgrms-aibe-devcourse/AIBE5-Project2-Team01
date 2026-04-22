package com.example.meetball.domain.attachment.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session(Long userId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", userId);
        return session;
    }

    @Test
    @DisplayName("MultipartFile 첨부파일 업로드 성공 테스트")
    void uploadAttachmentSuccess() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-report.pdf",
                "application/pdf",
                "Test Content Data".getBytes()
        );

        // when & then
        mockMvc.perform(multipart("/api/projects/1/attachments")
                .file(file)
                .session(session(1L)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value("test-report.pdf"))
                .andExpect(jsonPath("$.fileSize").value("Test Content Data".getBytes().length));
    }
}
