package com.example.meetball.domain.projectapplication.service;

import com.example.meetball.domain.projectapplication.dto.ProjectApplicationRequestDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationResponseDto;
import com.example.meetball.domain.projectapplication.dto.ProjectApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.entity.ProjectApplicationStatus;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
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
public class ProjectApplicationServiceTest {

    @Autowired
    private ProjectApplicationService projectApplicationService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectParticipantRepository projectParticipantRepository;

    @Autowired
    private com.example.meetball.domain.projectapplication.repository.ProjectApplicationRepository projectApplicationRepository;

    @Autowired
    private PositionRepository positionRepository;

    @Autowired
    private TechStackRepository techStackRepository;

    private Profile testUser;
    private Profile leaderUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        String uid = java.util.UUID.randomUUID().toString().substring(0, 8);
        // 테스트용 유저 생성
        testUser = profileRepository.save(Profile.builder()
                .email("applicant" + uid + "@test.com")
                .nickname("지원자" + uid)
                .isPublic(true)
                .build());

        leaderUser = profileRepository.save(Profile.builder()
                .email("leader" + uid + "@test.com")
                .nickname("팀장명" + uid)
                .isPublic(true)
                .build());

        // 테스트용 프로젝트 생성
        testProject = saveProjectWithSelections(createProject("테스트 프로젝트", 5), "백엔드:5", List.of("Java"));

        projectParticipantRepository.save(ProjectParticipant.builder()
                .project(testProject)
                .profile(leaderUser)
                .role("LEADER")
                .build());
    }

    private Project createProject(String title, int requiredMember) {
        return new Project(
                title,
                "상세 설명",
                "프로젝트",
                "온라인",
                requiredMember,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(14),
                null,
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Project saveProjectWithSelections(Project project, String positions, List<String> techStacks) {
        if (project.getOwnerProfile() == null) {
            project.assignOwner(leaderUser);
        }
        project.replacePositions(
                ProjectSelectionCatalog.parsePositionCapacities(positions, project.getTotalRecruitment()),
                this::resolvePosition
        );
        project.replaceTechStacks(resolveTechStacks(techStacks));
        return projectRepository.save(project);
    }

    private Position resolvePosition(String name) {
        return positionRepository.findByName(name).orElseThrow();
    }

    private List<TechStack> resolveTechStacks(List<String> names) {
        return names.stream()
                .map(name -> techStackRepository.findByName(name).orElseThrow())
                .toList();
    }

    private void completeProject(Project project) {
        project.update(
                project.getTitle(),
                project.getDescription(),
                project.getProjectPurpose(),
                project.getWorkMethod(),
                project.getRecruitmentCount(),
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(8),
                LocalDate.now().plusDays(30),
                Project.RECRUIT_STATUS_CLOSED,
                Project.PROGRESS_STATUS_COMPLETED,
                LocalDateTime.now()
        );
        projectRepository.saveAndFlush(project);
    }

    @Test
    @DisplayName("프로젝트 지원하기 - 성공")
    void createApplication_Success() {
        // given
        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "열심히 하겠습니다!");

        // when
        ProjectApplicationResponseDto response = projectApplicationService.createApplication(testProject.getId(), request, testUser.getId());

        // then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getProfileNickname()).isEqualTo(testUser.getNickname());
        assertThat(response.getStatus()).isEqualTo("PENDING");
        ProjectApplication savedApplication = projectApplicationRepository.findById(response.getId()).orElseThrow();
        assertThat(savedApplication.getRecruitPosition()).isNotNull();
        assertThat(savedApplication.getRecruitPosition().getPositionName()).isEqualTo("백엔드");
    }

    @Test
    @DisplayName("중복 지원 방지 - 실패")
    void createApplication_Duplicate_Fail() {
        // given
        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "첫 지원");
        projectApplicationService.createApplication(testProject.getId(), request, testUser.getId());

        // when & then
        assertThatThrownBy(() -> projectApplicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Already applied");
    }

    @Test
    @DisplayName("내 지원 목록 조회 - 마이페이지 연동 검증")
    void getMyApplications_Success() {
        // given
        projectApplicationService.createApplication(testProject.getId(), new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());

        // when
        List<ProjectApplicationResponseDto> myApps = projectApplicationService.getMyApplications(testUser.getId());

        // then
        assertThat(myApps).hasSize(1);
        assertThat(myApps.get(0).getProjectId()).isEqualTo(testProject.getId());
    }

    @Test
    @DisplayName("지원 상태 변경 - 팀장 관리 기능 검증 (상대 코드 방식)")
    void updateApplicationStatus_Success() {
        // given
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(testProject.getId(), new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());
        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");

        // when
        ProjectApplicationResponseDto updatedApp = projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        // then
        assertThat(updatedApp.getStatus()).isEqualTo("ACCEPTED");
        Project acceptedProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(acceptedProject.getCurrentRecruitment()).isEqualTo(1);
        assertThat(projectParticipantRepository.existsByProjectAndProfile(acceptedProject, testUser)).isTrue();

        ProjectApplicationStatusUpdateRequestDto rejectRequest = new ProjectApplicationStatusUpdateRequestDto();
        rejectRequest.setStatus("REJECTED");

        ProjectApplicationResponseDto rejectedApp = projectApplicationService.updateApplicationStatus(app.getId(), rejectRequest, leaderUser.getId());

        assertThat(rejectedApp.getStatus()).isEqualTo("REJECTED");
        Project rejectedProject = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(rejectedProject.getCurrentRecruitment()).isEqualTo(0);
        assertThat(projectParticipantRepository.existsByProjectAndProfile(rejectedProject, testUser)).isFalse();
    }

    @Test
    @DisplayName("정원이 1명인 포지션도 첫 승인에는 성공한다")
    void updateApplicationStatus_FirstAcceptedPositionCapacityOne_Success() {
        Project singleCapacityProject = saveProjectWithSelections(createProject("단일 포지션 프로젝트", 1), "백엔드:1", List.of("Java"));
        projectParticipantRepository.save(ProjectParticipant.builder()
                .project(singleCapacityProject)
                .profile(leaderUser)
                .role("LEADER")
                .build());

        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                singleCapacityProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");

        ProjectApplicationResponseDto updatedApp = projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        assertThat(updatedApp.getStatus()).isEqualTo("ACCEPTED");
        Project acceptedProject = projectRepository.findById(singleCapacityProject.getId()).orElseThrow();
        assertThat(acceptedProject.getCurrentRecruitment()).isEqualTo(1);
    }

    @Test
    @DisplayName("대기 중인 지원서는 지원자가 직접 철회할 수 있다")
    void withdrawApplication_Pending_Success() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );

        ProjectApplicationResponseDto withdrawn = projectApplicationService.withdrawApplication(app.getId(), testUser.getId());

        assertThat(withdrawn.getStatus()).isEqualTo("WITHDRAWN");
        ProjectApplication savedApplication = projectApplicationRepository.findById(app.getId()).orElseThrow();
        assertThat(savedApplication.getStatus()).isEqualTo(ProjectApplicationStatus.WITHDRAWN);
        assertThat(projectApplicationService.getApplicationsByProjectId(testProject.getId(), leaderUser.getId())).isEmpty();

        ProjectApplicationResponseDto reapplied = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "다시 지원합니다"),
                testUser.getId()
        );
        assertThat(reapplied.getStatus()).isEqualTo("PENDING");
        assertThat(projectApplicationService.getApplicationsByProjectId(testProject.getId(), leaderUser.getId())).hasSize(1);
    }

    @Test
    @DisplayName("승인된 지원서는 지원자가 직접 철회할 수 없다")
    void withdrawApplication_Accepted_Fail() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");
        projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        assertThatThrownBy(() -> projectApplicationService.withdrawApplication(app.getId(), testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Only pending applications");
    }

    @Test
    @DisplayName("팀장은 승인된 팀원을 추방하고 모집 인원을 되돌릴 수 있다")
    void removeApplication_Accepted_ExpelsMember() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");
        projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId());

        ProjectApplicationResponseDto removed = projectApplicationService.removeApplication(app.getId(), leaderUser.getId());

        assertThat(removed.getStatus()).isEqualTo("REMOVED");
        ProjectApplication removedApplication = projectApplicationRepository.findById(app.getId()).orElseThrow();
        assertThat(removedApplication.getRecruitPosition()).isNotNull();
        Project project = projectRepository.findById(testProject.getId()).orElseThrow();
        assertThat(project.getCurrentRecruitment()).isEqualTo(0);
        assertThat(projectParticipantRepository.existsByProjectAndProfile(project, testUser)).isFalse();
        assertThat(projectApplicationService.getApplicationsByProjectId(project.getId(), leaderUser.getId())).isEmpty();
    }

    @Test
    @DisplayName("거절된 지원서도 ERD 제약에 맞게 지원 포지션 관계를 유지한다")
    void rejectedApplication_KeepsRecruitPosition() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        ProjectApplicationStatusUpdateRequestDto rejectRequest = new ProjectApplicationStatusUpdateRequestDto();
        rejectRequest.setStatus("REJECTED");

        projectApplicationService.updateApplicationStatus(app.getId(), rejectRequest, leaderUser.getId());

        ProjectApplication rejectedApplication = projectApplicationRepository.findById(app.getId()).orElseThrow();
        assertThat(rejectedApplication.getRecruitPosition()).isNotNull();
    }

    @Test
    @DisplayName("이미 참여 중인 사용자는 같은 프로젝트에 지원할 수 없다")
    void createApplication_ProjectParticipant_Fail() {
        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(leaderUser.getId(), leaderUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> projectApplicationService.createApplication(testProject.getId(), request, leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Project members cannot apply");
    }

    @Test
    @DisplayName("모집 인원이 가득 찬 프로젝트에는 지원할 수 없다")
    void createApplication_RecruitmentFull_Fail() {
        for (int i = 0; i < 5; i++) {
            Profile occupiedMember = Profile.builder()
                    .email("occupied" + i + "@test.com")
                    .nickname("참여자" + i)
                    .isPublic(true)
                    .build();
            occupiedMember.replacePosition(resolvePosition("백엔드"));
            occupiedMember = profileRepository.save(occupiedMember);
            projectParticipantRepository.save(ProjectParticipant.builder()
                    .project(testProject)
                    .profile(occupiedMember)
                    .role("MEMBER")
                    .build());
        }
        projectParticipantRepository.flush();

        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> projectApplicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Recruitment count is already full");
    }

    @Test
    @DisplayName("프로젝트 완료 상태에서는 지원할 수 없다")
    void createApplication_CompletedProject_Fail() {
        completeProject(testProject);

        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        assertThatThrownBy(() -> projectApplicationService.createApplication(testProject.getId(), request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Cannot apply");
    }

    @Test
    @DisplayName("완료된 프로젝트에서는 팀장이 지원 상태를 변경할 수 없다")
    void updateApplicationStatus_CompletedProject_Fail() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        completeProject(testProject);

        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("ACCEPTED");

        assertThatThrownBy(() -> projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("완료된 프로젝트의 지원 내역은 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("완료된 프로젝트에서는 지원자가 지원을 철회할 수 없다")
    void withdrawApplication_CompletedProject_Fail() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        completeProject(testProject);

        assertThatThrownBy(() -> projectApplicationService.withdrawApplication(app.getId(), testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("완료된 프로젝트의 지원 내역은 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("완료된 프로젝트에서는 팀장이 지원자를 삭제하거나 추방할 수 없다")
    void removeApplication_CompletedProject_Fail() {
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(
                testProject.getId(),
                new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"),
                testUser.getId()
        );
        completeProject(testProject);

        assertThatThrownBy(() -> projectApplicationService.removeApplication(app.getId(), leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("완료된 프로젝트의 지원 내역은 변경할 수 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 지원 시 실패")
    void createApplication_InvalidProject_Fail() {
        // given
        ProjectApplicationRequestDto request = new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지");

        // when & then
        assertThatThrownBy(() -> projectApplicationService.createApplication(999L, request, testUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    @DisplayName("올바르지 않은 상태값으로 변경 시 실패")
    void updateApplicationStatus_InvalidStatus_Fail() {
        // given
        ProjectApplicationResponseDto app = projectApplicationService.createApplication(testProject.getId(), new ProjectApplicationRequestDto(testUser.getId(), testUser.getNickname(), "백엔드", "메시지"), testUser.getId());
        ProjectApplicationStatusUpdateRequestDto updateRequest = new ProjectApplicationStatusUpdateRequestDto();
        updateRequest.setStatus("INVALID_STATUS");

        // when & then
        assertThatThrownBy(() -> projectApplicationService.updateApplicationStatus(app.getId(), updateRequest, leaderUser.getId()))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class)
                .hasMessageContaining("Invalid status");
    }
}
