package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class ApplicationServiceTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private com.example.meetball.domain.application.repository.ApplicationRepository applicationRepository;

    private User testUser;
    private User leaderUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        // 테스트용 유저 생성
        testUser = userRepository.save(User.builder()
                .email("applicant" + uid + "@test.com")
                .nickname("지원자" + uid)
                .role("USER") // 필수 필드 추가
                .techStack("Java")
                .isPublic(true)
                .build());

        leaderUser = userRepository.save(User.builder()
                .email("leader" + uid + "@test.com")
                .nickname("팀장명" + uid)
                .role("MEMBER")
                .techStack("Java")
                .isPublic(true)
                .build());

        // 테스트용 프로젝트 생성
        testProject = projectRepository.save(new Project(
                "테스트 프로젝트",
                "요약",
                "상세 설명",
                "타입",
                "포지션",
                leaderUser.getNickname(),
                "리더역할",
                "아바타",
                "썸네일",
                0,
                5,
                LocalDate.now().plusDays(7),
                LocalDate.now(),
                "Java"
        ));
    }

    @Test
    @DisplayName("프로젝트 지원하기 - 성공 (우리 코드 방식)")
    void createApplication_Success() {
        // given
        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "열심히 하겠습니다!");

        // when
        ApplicationResponseDto response = applicationService.createApplication(testProject.getId(), request, testUser.getId());

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getStatus()).isEqualTo("PENDING");
    }

    @Test
    @DisplayName("중복 지원 방지 - 실패")
    void createApplication_Duplicate_Fail() {
        // given
        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "첫 지원");
        applicationService.createApplication(testProject.getId(), request, testUser.getId());

        // when & then
        assertThatThrownBy(() -> applicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Already applied");
    }

    @Test
    @DisplayName("내 지원 목록 조회 - 마이페이지 연동 검증")
    void getMyApplications_Success() {
        // given
        applicationService.createApplication(testProject.getId(), new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());

        // when
        List<ApplicationResponseDto> myApps = applicationService.getMyApplications(testUser.getId());

        // then
        assertThat(myApps).hasSize(1);
        assertThat(myApps.get(0).getProjectId()).isEqualTo(testProject.getId());
    }

    @Test
    @DisplayName("지원 상태 변경 - 팀장 관리 기능 검증 (상대 코드 방식)")
    void updateApplicationStatus_Success() {
        // given
        ApplicationResponseDto app = applicationService.createApplication(testProject.getId(), new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());
        ApplicationStatusUpdateRequestDto updateRequest = new ApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");

        // when
        ApplicationResponseDto updatedApp = applicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        // then
        assertThat(updatedApp.getStatus()).isEqualTo("ACCEPTED");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 지원 시 실패")
    void createApplication_InvalidProject_Fail() {
        // given
        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        // when & then
        assertThatThrownBy(() -> applicationService.createApplication(999L, request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("올바르지 않은 상태값으로 변경 시 실패")
    void updateApplicationStatus_InvalidStatus_Fail() {
        // given
        ApplicationResponseDto app = applicationService.createApplication(testProject.getId(), new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());
        ApplicationStatusUpdateRequestDto updateRequest = new ApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("INVALID_STATUS");

        // when & then
        assertThatThrownBy(() -> applicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Invalid status");
    }
}
