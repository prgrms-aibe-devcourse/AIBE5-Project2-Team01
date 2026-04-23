package com.example.meetball.domain.auth.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
    @DisplayName("최초 로그인 프로필 모달은 직무 자유 입력 대신 포지션 선택을 렌더링한다")
    void welcomeProfileModalRendersPositionSelector() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);
        session.setAttribute("needsProfile", true);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"welcomeProfileModal\"")))
                .andExpect(content().string(containsString("id=\"welcomeJobTitle\"")))
                .andExpect(content().string(containsString("id=\"welcomePositionOptions\"")))
                .andExpect(content().string(containsString("meetballPositionOptions")))
                .andExpect(content().string(containsString("포지션을 선택해주세요.")))
                .andExpect(content().string(not(containsString("Current Role / Job"))))
                .andExpect(content().string(not(containsString("placeholder=\"예시) 백엔드 개발자, UI/UX 디자이너\""))));
    }

    @Test
    @DisplayName("게스트 공통 헤더는 Google 로그인 모달 설정을 렌더링한다")
    void guestHeaderRendersGoogleLoginModal() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"loginModal\"")))
                .andExpect(content().string(containsString("data-client_id=\"test-google-client.apps.googleusercontent.com\"")))
                .andExpect(content().string(containsString("data-type=\"standard\"")))
                .andExpect(content().string(containsString("data-width=\"420\"")))
                .andExpect(content().string(containsString("data-text=\"continue_with\"")))
                .andExpect(content().string(containsString("data-logo_alignment=\"right\"")))
                .andExpect(content().string(containsString("/api/auth/google")))
                .andExpect(content().string(containsString("Github 로그인 준비중")))
                .andExpect(content().string(containsString("Kakao 로그인 준비중")))
                .andExpect(content().string(containsString("Naver 로그인 준비중")))
                .andExpect(content().string(containsString("aria-label=\"Kakao logo\"")))
                .andExpect(content().string(containsString("aria-label=\"Naver logo\"")));
    }

    @Test
    @DisplayName("별도 로그인 페이지 라우트는 제공하지 않는다")
    void loginPageRouteDoesNotExist() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("비로그인 마이페이지 접근은 기본 사용자 대신 로그인 모달로 유도한다")
    void guestMyPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=1&redirect=/user/mypage"));
    }

    @Test
    @DisplayName("비로그인 타인 마이페이지 접근도 로그인 모달로 유도한다")
    void guestOtherUserMyPageRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/user/mypage").param("userId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=1&redirect=/people/1"));
    }

    @Test
    @DisplayName("로그인 사용자가 타인 마이페이지 URL로 접근하면 사람 프로필 페이지로 이동한다")
    void loggedInOtherUserMyPageRedirectsToPeopleProfile() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/user/mypage").param("userId", "2").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/people/2"));
    }

    @Test
    @DisplayName("마이페이지는 읽은 프로젝트를 독립 탭으로 렌더링한다")
    void myPageRendersRecentReadsAsIndependentTab() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/user/mypage").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"tab-reads\"")))
                .andExpect(content().string(containsString("id=\"content-reads\"")))
                .andExpect(content().string(containsString("id=\"editPositionOptions\"")))
                .andExpect(content().string(containsString("renderEditPositionOptions")))
                .andExpect(content().string(not(containsString("placeholder=\"직무를 입력하세요"))));
    }

    @Test
    @DisplayName("프로젝트 등록 화면은 여러 포지션 추가 UI를 렌더링한다")
    void registerPageRendersMultiPositionControls() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/register").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"positionRows\"")))
                .andExpect(content().string(containsString("id=\"techStackOptions\"")))
                .andExpect(content().string(containsString("type=\"hidden\" id=\"recruitmentCount\"")))
                .andExpect(content().string(containsString("'임베디드SW'")))
                .andExpect(content().string(containsString("'매니저(PM)'")))
                .andExpect(content().string(containsString("meetballTechStackMeta")))
                .andExpect(content().string(containsString("techStackLogoHtml")))
                .andExpect(content().string(containsString("fa-brands fa-react")))
                .andExpect(content().string(not(containsString("id=\"techStackTags\""))))
                .andExpect(content().string(not(containsString("id=\"position\""))));
    }

    @Test
    @DisplayName("프로젝트 API는 포지션별 인원을 저장하고 총 모집 인원을 계산한다")
    void createProjectAcceptsMultiplePositions() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(post("/api/projects")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "다중 포지션 테스트 프로젝트",
                          "description": "여러 포지션을 저장하는지 확인합니다.",
                          "projectType": "사이드 프로젝트",
                          "progressMethod": "온라인",
                          "position": "프론트엔드:2, 백엔드:1",
                          "techStacks": ["React", "Spring"],
                          "recruitmentCount": 99,
                          "recruitmentStartAt": "2026-04-23",
                          "recruitmentEndAt": "2026-05-23",
                          "projectStartAt": "2026-05-24",
                          "projectEndAt": "2026-06-24",
                          "closed": false,
                          "completed": false
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("프론트엔드:2, 백엔드:1"))
                .andExpect(jsonPath("$.totalRecruitment").value(3));
    }

    @Test
    @DisplayName("지원자가 있는 포지션은 프로젝트 수정에서 삭제할 수 없다")
    void updateProjectCannotRemovePositionWithApplications() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(put("/api/projects/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "AI 기반 헬스케어 모바일 앱 개발",
                          "description": "지원자가 있는 디자이너 포지션 삭제를 막는지 확인합니다.",
                          "projectType": "사이드 프로젝트",
                          "progressMethod": "온라인",
                          "position": "프론트엔드:2, 백엔드:1, 데이터/AI:1",
                          "techStacks": ["ReactNative", "Python"],
                          "recruitmentStartAt": "2026-04-23",
                          "recruitmentEndAt": "2026-05-23",
                          "projectStartAt": "2026-05-24",
                          "projectEndAt": "2026-06-24",
                          "closed": false,
                          "completed": false
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("비로그인 사람 프로필 페이지 접근은 로그인 모달로 유도한다")
    void guestPeopleProfileRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/people/2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=1&redirect=/people/2"));
    }

    @Test
    @DisplayName("로그인 사용자는 사람 프로필 페이지를 조회한다")
    void loggedInPeopleProfileRendersPublicProfile() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 1L);

        mockMvc.perform(get("/people/2").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("성실한리트리버")))
                .andExpect(content().string(containsString("프로필 정보")))
                .andExpect(content().string(containsString("참여 프로젝트")))
                .andExpect(content().string(containsString("만든 프로젝트")))
                .andExpect(content().string(containsString("참여 중인 프로젝트")))
                .andExpect(content().string(containsString("종료된 프로젝트")))
                .andExpect(content().string(containsString("여행 일정 공유 플랫폼 [TripMate]")))
                .andExpect(content().string(containsString("AI 기반 헬스케어 모바일 앱 개발")))
                .andExpect(content().string(not(containsString("member@meetball.com"))));
    }

    @Test
    @DisplayName("사람 프로필 페이지는 완료된 프로젝트 이력을 별도 섹션에 표시한다")
    void peopleProfileRendersCompletedProjects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", 2L);

        mockMvc.perform(get("/people/1").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("종료된 프로젝트")))
                .andExpect(content().string(containsString("반려견 케어 서비스 [멍멍 비서]")))
                .andExpect(content().string(containsString("리더로 참여")))
                .andExpect(content().string(not(containsString("leader@meetball.com"))));
    }

    @Test
    @DisplayName("프로젝트 상세 페이지는 사이드바와 팀 정보를 함께 렌더링한다")
    void projectDetailRendersSidebar() throws Exception {
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Active Project")))
                .andExpect(content().string(containsString("id=\"detailReadCount\"")))
                .andExpect(content().string(containsString("팀 멤버 소개")))
                .andExpect(content().string(containsString("/people/1")))
                .andExpect(content().string(containsString("/people/2")));
    }
}
