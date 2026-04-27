package com.example.meetball.domain.auth.controller;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectRecruitPosition;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("비로그인 홈 화면은 게스트용 헤더를 보여준다")
    void guestHomeShowsLoggedOutHeader() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("로그인")))
                .andExpect(content().string(containsString("/img/tech-stack/react.svg")))
                .andExpect(content().string(containsString("/img/tech-stack/nodejs.svg")))
                .andExpect(content().string(not(containsString("김밋볼"))))
                .andExpect(content().string(not(containsString("meetball_dev@google.com"))));
    }

    @Test
    @DisplayName("로그인 세션이 있으면 홈 헤더에 현재 사용자 정보가 보인다")
    void loggedInHomeShowsCurrentProfileHeader() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("초코푸들")))
                .andExpect(content().string(containsString("leader@meetball.local")));
    }

    @Test
    @DisplayName("최초 로그인 온보딩 모달은 계정, 프로필, 기술스택 3단계를 렌더링한다")
    void welcomeProfileModalRendersThreeStepOnboarding() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);
        session.setAttribute("needsProfile", true);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"welcomeProfileModal\"")))
                .andExpect(content().string(containsString("data-welcome-step=\"1\"")))
                .andExpect(content().string(containsString("data-welcome-step=\"2\"")))
                .andExpect(content().string(containsString("data-welcome-step=\"3\"")))
                .andExpect(content().string(containsString("id=\"welcomeStepIndicators\"")))
                .andExpect(content().string(containsString("id=\"welcomeHeadline\"")))
                .andExpect(content().string(containsString("id=\"welcomeSubtitle\"")))
                .andExpect(content().string(containsString("id=\"welcomeName\"")))
                .andExpect(content().string(containsString("id=\"welcomeNickname\"")))
                .andExpect(content().string(containsString("id=\"welcomePhoneNumber\"")))
                .andExpect(content().string(containsString("id=\"welcomeBirthDate\"")))
                .andExpect(content().string(containsString("id=\"welcomeGenderOptions\"")))
                .andExpect(content().string(containsString("id=\"welcomePosition\"")))
                .andExpect(content().string(containsString("id=\"welcomePositionOptions\"")))
                .andExpect(content().string(containsString("id=\"welcomeExperienceOptions\"")))
                .andExpect(content().string(containsString("id=\"welcomeOrganization\"")))
                .andExpect(content().string(containsString("id=\"welcomeOrgVisibleOptions\"")))
                .andExpect(content().string(containsString("meetballPositionOptions")))
                .andExpect(content().string(containsString("orgVisible: true")))
                .andExpect(content().string(containsString("/api/mypage/onboarding")))
                .andExpect(content().string(containsString("포지션을 선택해주세요.")))
                .andExpect(content().string(containsString("name=\"nickname\"")))
                .andExpect(content().string(not(containsString("Current Role / Job"))));
    }

    @Test
    @DisplayName("잘못된 profileId와 needsProfile 세션이 남아 있어도 홈 템플릿은 깨지지 않는다")
    void homeDoesNotBreakWhenNeedsProfileExistsWithoutCurrentProfile() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 999999L);
        session.setAttribute("needsProfile", true);

        mockMvc.perform(get("/").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("id=\"welcomeProfileModal\""))))
                .andExpect(content().string(containsString("로그인")));
    }

    @Test
    @DisplayName("게스트 공통 헤더는 Google 로그인 모달 설정을 렌더링한다")
    void guestHeaderRendersGoogleLoginModal() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"loginModal\"")))
                .andExpect(content().string(containsString("data-client_id=\"test-google-client.apps.googleusercontent.com\"")))
                .andExpect(content().string(containsString("data-type=\"standard\"")))
                .andExpect(content().string(containsString("data-width=\"400\"")))
                .andExpect(content().string(containsString("data-text=\"continue_with\"")))
                .andExpect(content().string(containsString("data-logo_alignment=\"center\"")))
                .andExpect(content().string(containsString("/api/auth/google")))
                .andExpect(content().string(containsString("social-login-google")))
                .andExpect(content().string(containsString("Meetball에 오신 것을 환영합니다!")))
                .andExpect(content().string(containsString("Meetball에서 함께 할 팀원들을 찾으세요!")))
                .andExpect(content().string(containsString("id=\"loginError\"")));
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
        mockMvc.perform(get("/mypage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=1&redirect=/mypage"));
    }

    @Test
    @DisplayName("비로그인 마이페이지의 profileId 파라미터는 타인 프로필로 해석하지 않는다")
    void guestMyPageProfileIdParamStillRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/mypage").param("profileId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/?login=1&redirect=/mypage"));
    }

    @Test
    @DisplayName("로그인 마이페이지는 profileId 파라미터와 무관하게 본인 페이지만 렌더링한다")
    void loggedInMyPageIgnoresProfileIdParam() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(get("/mypage").param("profileId", "2").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"profileId\"")));
    }

    @Test
    @DisplayName("마이페이지는 읽은 프로젝트를 독립 탭으로 렌더링한다")
    void myPageRendersRecentReadsAsIndependentTab() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(get("/mypage").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"tab-reads\"")))
                .andExpect(content().string(containsString("id=\"content-reads\"")))
                .andExpect(content().string(containsString("id=\"inlinePositionOptions\"")))
                .andExpect(content().string(containsString("renderInlinePositionOptions")))
                .andExpect(content().string(not(containsString("placeholder=\"직무를 입력하세요"))));
    }

    @Test
    @DisplayName("프로젝트 등록 화면은 여러 포지션 추가 UI를 렌더링한다")
    void registerPageRendersMultiPositionControls() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(get("/register").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"positionRows\"")))
                .andExpect(content().string(containsString("id=\"techStackOptions\"")))
                .andExpect(content().string(containsString("type=\"hidden\" id=\"recruitmentCount\"")))
                .andExpect(content().string(containsString("'임베디드SW'")))
                .andExpect(content().string(containsString("'매니저(PM)'")))
                .andExpect(content().string(containsString("meetballTechStackMeta")))
                .andExpect(content().string(containsString("meetballTechStackLogoHtml")))
                .andExpect(content().string(containsString("/img/tech-stack/react.svg")))
                .andExpect(content().string(not(containsString("id=\"techStackTags\""))))
                .andExpect(content().string(not(containsString("id=\"position\""))));
    }

    @Test
    @DisplayName("프로젝트 API는 포지션별 인원을 저장하고 총 모집 인원을 계산한다")
    void createProjectAcceptsMultiplePositions() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(post("/api/projects")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "다중 포지션 테스트 프로젝트",
                          "description": "여러 포지션을 저장하는지 확인합니다.",
                          "projectPurpose": "프로젝트",
                          "workMethod": "온라인",
                          "position": "프론트엔드:2, 백엔드:1",
                          "techStacks": ["React", "Spring"],
                          "recruitmentCount": 99,
                          "recruitmentStartAt": "2026-04-23",
                          "recruitmentEndAt": "2026-05-23",
                          "projectStartAt": "2026-05-24",
                          "projectEndAt": "2026-06-24",
                          "recruitStatus": "OPEN",
                          "progressStatus": "READY"
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
        session.setAttribute("profileId", 1L);

        mockMvc.perform(put("/api/projects/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "AI 기반 헬스케어 모바일 앱 개발",
                          "description": "지원자가 있는 디자이너 포지션 삭제를 막는지 확인합니다.",
                          "projectPurpose": "프로젝트",
                          "workMethod": "온라인",
                          "position": "프론트엔드:2, 백엔드:1, 데이터/AI:1",
                          "techStacks": ["ReactNative", "Python"],
                          "recruitmentStartAt": "2026-04-23",
                          "recruitmentEndAt": "2026-05-23",
                          "projectStartAt": "2026-05-24",
                          "projectEndAt": "2026-06-24",
                          "recruitStatus": "OPEN",
                          "progressStatus": "READY"
                        }
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("프로젝트 상태를 모집 마감으로 수정하면 모든 모집 포지션도 함께 닫힌다")
    void updateProjectClosesAllRecruitPositionsWhenRecruitmentCloses() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(put("/api/projects/1")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "AI 기반 헬스케어 모바일 앱 개발",
                          "description": "모집 마감 시 포지션 상태도 함께 닫히는지 확인합니다.",
                          "projectPurpose": "프로젝트",
                          "workMethod": "온라인",
                          "position": "프론트엔드:2, 백엔드:1, 디자이너:1",
                          "techStacks": ["ReactNative", "Python"],
                          "recruitmentStartAt": "2026-04-23",
                          "recruitmentEndAt": "2026-05-23",
                          "projectStartAt": "2026-05-24",
                          "projectEndAt": "2026-06-24",
                          "recruitStatus": "CLOSED",
                          "progressStatus": "READY"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitStatus").value("CLOSED"));

        Project project = loadProjectWithPositions(1L);
        assertThat(project.getPositionSelections()).isNotEmpty();
        assertThat(project.getPositionSelections())
                .extracting(ProjectRecruitPosition::getRecruitStatus)
                .containsOnly(ProjectRecruitPosition.STATUS_CLOSED);
    }

    @Test
    @DisplayName("전용 상태 API는 모집 마감 이후 프로젝트 완료를 일관되게 처리한다")
    void statusActionEndpointsCloseAndCompleteProjectConsistently() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("profileId", 1L);

        mockMvc.perform(post("/api/projects/1/close-recruitment").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitStatus").value("CLOSED"));

        mockMvc.perform(post("/api/projects/1/complete").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recruitStatus").value("CLOSED"))
                .andExpect(jsonPath("$.progressStatus").value("COMPLETED"));

        Project project = loadProjectWithPositions(1L);
        assertThat(project.getRecruitStatus()).isEqualTo(Project.RECRUIT_STATUS_CLOSED);
        assertThat(project.getProgressStatus()).isEqualTo(Project.PROGRESS_STATUS_COMPLETED);
        assertThat(project.getPositionSelections())
                .extracting(ProjectRecruitPosition::getRecruitStatus)
                .containsOnly(ProjectRecruitPosition.STATUS_CLOSED);
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
        session.setAttribute("profileId", 1L);

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
        session.setAttribute("profileId", 2L);

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
                .andExpect(content().string(containsString("id=\"readStatusLabel\"")))
                .andExpect(content().string(containsString("조회수 : ")))
                .andExpect(content().string(containsString("팀 멤버 소개")))
                .andExpect(content().string(containsString("/people/1")))
                .andExpect(content().string(containsString("/people/2")));
    }

    private Project loadProjectWithPositions(Long projectId) {
        entityManager.clear();
        return entityManager.createQuery(
                        "select distinct p from Project p left join fetch p.positionSelections where p.id = :projectId",
                        Project.class
                )
                .setParameter("projectId", projectId)
                .getSingleResult();
    }
}
