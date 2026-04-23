package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private ProjectMemberRepository projectMemberRepository;

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
                .techStacks(List.of("Java"))
                .isPublic(true)
                .build());

        leaderUser = userRepository.save(User.builder()
                .email("leader" + uid + "@test.com")
                .nickname("팀장명" + uid)
                .role("MEMBER")
                .techStacks(List.of("Java"))
                .isPublic(true)
                .build());

        // 테스트용 프로젝트 생성
        testProject = saveProjectWithSelections(new Project(
                "테스트 프로젝트",
                "요약",
                "상세 설명",
                "타입",
                "백엔드:5",
                leaderUser.getNickname(),
                "리더역할",
                "아바타",
                "썸네일",
                0,
                5,
                LocalDate.now().plusDays(7),
                LocalDate.now(),
                List.of("Java")
        ));

        projectMemberRepository.save(ProjectMember.builder()
                .project(testProject)
                .user(leaderUser)
                .role("LEADER")
                .build());
    }

    private Project saveProjectWithSelections(Project project) {
        project.replacePositions(ProjectSelectionCatalog.parsePositionCapacities(project.getPosition(), project.getTotalRecruitment()));
        project.replaceTechStacks(List.of("Java"));
        return projectRepository.save(project);
    }

    @Test
    @DisplayName("프로젝트 지원하기 - 성공")
    void createApplication_Success() {
        // given
        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "열심히 하겠습니다!");

        // when
        ApplicationResponseDto response = applicationService.createApplication(testProject.getId(), request, testUser.getId());

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getUserNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getStatus()).isEqualTo("PENDING");
        Application savedApplication = applicationRepository.findById(response.getId()).orElseThrow();
        assertThat(savedApplication.getProjectPosition()).isNotNull();
        assertThat(savedApplication.getProjectPosition().getPositionName()).isEqualTo("백엔드");
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
        Project acceptedProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(acceptedProject.getCurrentRecruitment()).isEqualTo(1);
        assertThat(projectMemberRepository.existsByProjectAndUser(acceptedProject, testUser)).isTrue();

        ApplicationStatusUpdateRequestDto rejectRequest = new ApplicationStatusUpdateRequestDto();
        rejectRequest.setStatus("REJECTED");

        ApplicationResponseDto rejectedApp = applicationService.updateApplicationStatus(app.getId(), rejectRequest, leaderUser.getId());

        assertThat(rejectedApp.getStatus()).isEqualTo("REJECTED");
        Project rejectedProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(rejectedProject.getCurrentRecruitment()).isEqualTo(0);
        assertThat(projectMemberRepository.existsByProjectAndUser(rejectedProject, testUser)).isFalse();
    }

    @Test
    @DisplayName("정원이 1명인 포지션도 첫 승인에는 성공한다")
    void updateApplicationStatus_FirstAcceptedPositionCapacityOne_Success() {
        Project singleCapacityProject = saveProjectWithSelections(new Project(
                "단일 포지션 프로젝트",
                "요약",
                "상세 설명",
                "타입",
                "백엔드:1",
                leaderUser.getNickname(),
                "리더역할",
                "아바타",
                "썸네일",
                0,
                1,
                LocalDate.now().plusDays(7),
                LocalDate.now(),
                List.of("Java")
        ));
        projectMemberRepository.save(ProjectMember.builder()
                .project(singleCapacityProject)
                .user(leaderUser)
                .role("LEADER")
                .build());

        ApplicationResponseDto app = applicationService.createApplication(
                singleCapacityProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ApplicationStatusUpdateRequestDto updateRequest = new ApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");

        ApplicationResponseDto updatedApp = applicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        assertThat(updatedApp.getStatus()).isEqualTo("ACCEPTED");
        Project acceptedProject = projectRepository.findById(singleCapacityProject.getId()).orElseThrow();
        assertThat(acceptedProject.getCurrentRecruitment()).isEqualTo(1);
    }

    @Test
    @DisplayName("대기 중인 지원서는 지원자가 직접 철회할 수 있다")
    void withdrawApplication_Pending_Success() {
        ApplicationResponseDto app = applicationService.createApplication(
                testProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );

        ApplicationResponseDto withdrawn = applicationService.withdrawApplication(app.getId(), testUser.getId());

        assertThat(withdrawn.getStatus()).isEqualTo("WITHDRAWN");
        Application savedApplication = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.WITHDRAWN);
        assertThat(applicationService.getApplicationsByProjectId(testProject.getId(), leaderUser.getId())).isEmpty();

        ApplicationResponseDto reapplied = applicationService.createApplication(
                testProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "다시 지원합니다"),
                testUser.getId()
        );
        assertThat(reapplied.getStatus()).isEqualTo("PENDING");
        assertThat(applicationService.getApplicationsByProjectId(testProject.getId(), leaderUser.getId())).hasSize(1);
    }

    @Test
    @DisplayName("승인된 지원서는 지원자가 직접 철회할 수 없다")
    void withdrawApplication_Accepted_Fail() {
        ApplicationResponseDto app = applicationService.createApplication(
                testProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ApplicationStatusUpdateRequestDto updateRequest = new ApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");
        applicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        assertThatThrownBy(() -> applicationService.withdrawApplication(app.getId(), testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Only pending applications");
    }

    @Test
    @DisplayName("팀장은 승인된 팀원을 추방하고 모집 인원을 되돌릴 수 있다")
    void removeApplication_Accepted_ExpelsMember() {
        ApplicationResponseDto app = applicationService.createApplication(
                testProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ApplicationStatusUpdateRequestDto updateRequest = new ApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");
        applicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        ApplicationResponseDto removed = applicationService.removeApplication(app.getId(), leaderUser.getId());

        assertThat(removed.getStatus()).isEqualTo("REMOVED");
        Application removedApplication = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(removedApplication.getProjectPosition()).isNull();
        Project project = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(project.getCurrentRecruitment()).isEqualTo(0);
        assertThat(projectMemberRepository.existsByProjectAndUser(project, testUser)).isFalse();
        assertThat(applicationService.getApplicationsByProjectId(project.getId(), leaderUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("거절된 지원서는 포지션 연결이 해제되어 해당 포지션을 삭제할 수 있다")
    void rejectedApplication_DetachesProjectPosition() {
        ApplicationResponseDto app = applicationService.createApplication(
                testProject.getId(),
                new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ApplicationStatusUpdateRequestDto rejectRequest = new ApplicationStatusUpdateRequestDto();
        rejectRequest.setStatus("REJECTED");

        applicationService.updateApplicationStatus(app.getId(), rejectRequest, leaderUser.getId());

        Application rejectedApplication = applicationRepository.findById(app.getId()).orElseThrow();
        assertThat(rejectedApplication.getProjectPosition()).isNull();

        testProject.updateDiscoveryFields("프론트엔드:1", List.of("Java"), null);
        testProject.replacePositions(ProjectSelectionCatalog.parsePositionCapacities("프론트엔드:1", null));

        projectRepository.saveAndFlush(testProject);
        assertThat(testProject.getPositionSelections())
                .extracting("positionName")
                .containsExactly("프론트엔드");
    }

    @Test
    @DisplayName("이미 참여 중인 사용자는 같은 프로젝트에 지원할 수 없다")
    void createApplication_ProjectMember_Fail() {
        ApplicationRequestDto request = new ApplicationRequestDto(leaderUser.getId(), leaderUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> applicationService.createApplication(testProject.getId(), request, leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Project members cannot apply");
    }

    @Test
    @DisplayName("모집 인원이 가득 찬 프로젝트에는 지원할 수 없다")
    void createApplication_RecruitmentFull_Fail() {
        for (int i = 0; i < 5; i++) {
            testProject.incrementCurrentRecruitment();
        }
        projectRepository.saveAndFlush(testProject);

        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> applicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Recruitment count is already full");
    }

    @Test
    @DisplayName("프로젝트 완료 상태에서는 지원할 수 없다")
    void createApplication_CompletedProject_Fail() {
        testProject.update(
                testProject.getTitle(),
                testProject.getDescription(),
                testProject.getProjectType(),
                testProject.getProgressMethod(),
                testProject.getRecruitmentCount(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(8),
                LocalDate.now().plusDays(30),
                true,
                true,
                LocalDateTime.now()
        );
        projectRepository.saveAndFlush(testProject);

        ApplicationRequestDto request = new ApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> applicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Cannot apply");
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
