package com.example.meetball.domain.mypage.controller;

import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.service.ProfileService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class MyPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProfileService profileService;

    private MockHttpSession session(Long profileId) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", profileId);
        return session;
    }

    @Test
    @DisplayName("마이페이지 프로필 정보를 성공적으로 조회한다")
    void getMyProfileTest() throws Exception {
        // given: DataInitializer에 의해 1번 프로필(초코푸들)이 생성되어 있음
        Long profileId = 1L;

        // when & then
        mockMvc.perform(get("/api/mypage/profile")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("초코푸들"))
                .andExpect(jsonPath("$.role").value("MEMBER"))
                .andExpect(jsonPath("$.meetBallIndex").value(36.7))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 참여 중인 프로젝트 목록을 조회한다")
    void getMyProjectsTest() throws Exception {
        Long profileId = 1L;

        mockMvc.perform(get("/api/mypage/projects")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].title").exists())
                .andExpect(jsonPath("$[0].userRole").exists())
                .andExpect(jsonPath("$[0].canReview").exists()) // 추가됨: 리뷰 가능 여부 필드 확인
                .andExpect(jsonPath("$[0].dDay").exists())     // 추가됨: D-Day 필드 확인
                .andDo(print());
    }

    @Test
    @DisplayName("마이페이지 API는 profileId 파라미터와 무관하게 세션 프로필 기준으로 조회한다")
    void myPageUsesOnlySessionProfileScope() throws Exception {
        mockMvc.perform(get("/api/mypage/profile")
                        .param("profileId", "2")
                        .session(session(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profileId").value(1));

        mockMvc.perform(get("/api/mypage/projects")
                        .param("profileId", "2")
                        .session(session(1L)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/mypage/projects/completed")
                        .param("profileId", "2")
                        .session(session(1L)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("마이페이지 프로필 수정 시 지정 포지션과 기술 스택이 저장된다")
    void updateProfilePersistsSelectedPositionAndTechStacks() throws Exception {
        MockHttpSession session = session(1L);

        mockMvc.perform(put("/api/mypage/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "초코푸들",
                                  "jobTitle": "백엔드",
                                  "techStacks": ["Java", "Spring"],
                                  "isPublic": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/mypage/profile")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nickname": "초코푸들",
                                  "jobTitle": "백엔드",
                                  "techStacks": ["Java", "Spring"],
                                  "isPublic": true
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/mypage/profile")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("백엔드"))
                .andExpect(jsonPath("$.techStack").value("Java, Spring"));
    }

    @Test
    @DisplayName("최초 로그인 온보딩은 account와 profile 정보를 함께 저장한다")
    void completeOnboardingPersistsAccountAndProfileData() throws Exception {
        MockHttpSession session = session(3L);
        session.setAttribute("needsProfile", true);

        mockMvc.perform(put("/api/mypage/onboarding")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "열정고양이",
                                  "phoneNumber": "010-9876-5432",
                                  "birthDate": "1998-07-15",
                                  "gender": "여자",
                                  "jobTitle": "디자이너",
                                  "experienceYears": "1~3년",
                                  "organization": "Meetball Studio",
                                  "orgVisible": true,
                                  "techStacks": ["Figma", "Zeplin"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttributeDoesNotExist("needsProfile"));

        Profile profile = profileService.getProfileById(3L);
        assertThat(profile.getAccount().getName()).isEqualTo("열정고양이");
        assertThat(profile.getPhoneNumber()).isEqualTo("010-9876-5432");
        assertThat(profile.getBirthDate()).hasToString("1998-07-15");
        assertThat(profile.getGender()).isEqualTo("여자");
        assertThat(profile.getJobTitle()).isEqualTo("디자이너");
        assertThat(profile.getExperienceYears()).isEqualTo("1~3년");
        assertThat(profile.getOrganization()).isEqualTo("Meetball Studio");
        assertThat(profile.isOrgVisible()).isTrue();
        assertThat(profile.getTechStackNames()).containsExactlyInAnyOrder("Figma", "Zeplin");
        assertThat(profile.isProfileComplete()).isTrue();
    }

    @Test
    @DisplayName("사용자가 지원한 프로젝트 목록을 조회한다")
    void getMyApplicationsTest() throws Exception {
        Long profileId = 1L;

        mockMvc.perform(get("/api/mypage/applications")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 최근에 읽은 프로젝트 목록을 조회한다")
    void getRecentReadsTest() throws Exception {
        Long profileId = 1L;

        mockMvc.perform(get("/api/mypage/recent-reads")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 관심 등록한 프로젝트 목록을 조회한다")
    void getMyBookmarksTest() throws Exception {
        Long profileId = 3L; // 열정고양이

        mockMvc.perform(get("/api/mypage/bookmarks")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("AI 기반 헬스케어 모바일 앱 개발"))
                .andDo(print());
    }

    @Test
    @DisplayName("사용자가 받은 피어 리뷰 목록을 조회한다")
    void getMyReviewsTest() throws Exception {
        Long profileId = 1L; // 초코푸들

        mockMvc.perform(get("/api/mypage/reviews")
                        .session(session(profileId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].reviewerNickname").value("코딩하는비글"))
                .andExpect(jsonPath("$[0].projectTitle").value("반려견 케어 서비스 [멍멍 비서]"))
                .andDo(print());
    }
}
