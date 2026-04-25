package com.example.meetball.global.config;

import com.example.meetball.domain.profile.repository.ProfileRepository;
import com.example.meetball.domain.project.dto.ProjectDetailView;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DataInitializerTest {

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectParticipantRepository projectParticipantRepository;

    @Autowired
    private ProjectService projectService;

    @Test
    @DisplayName("H2 샘플 데이터는 프로필 50개와 프로젝트 35개로 초기화된다")
    void seedsExpectedProfileAndProjectCounts() {
        assertThat(profileRepository.count()).isEqualTo(50);
        assertThat(projectRepository.count()).isEqualTo(35);
    }

    @Test
    @DisplayName("시드 프로젝트 정원은 소규모 23개, 5명 4개, 8명 8개로 구성되고 일부 프로젝트는 정원이 꽉 찬다")
    void seedsExpectedCapacityMix() {
        List<Project> projects = projectRepository.findAll();
        Map<Integer, Long> capacityCounts = projects.stream()
                .collect(groupingBy(Project::getTotalRecruitment, counting()));

        long smallProjectCount = projects.stream()
                .map(Project::getTotalRecruitment)
                .filter(capacity -> capacity != null && capacity >= 2 && capacity <= 4)
                .count();
        long fullProjectCount = projects.stream()
                .filter(project -> project.getTotalRecruitment() != null)
                .filter(project -> project.getCurrentRecruitment() >= project.getTotalRecruitment())
                .count();

        assertThat(smallProjectCount).isEqualTo(23);
        assertThat(capacityCounts.getOrDefault(5, 0L)).isEqualTo(4);
        assertThat(capacityCounts.getOrDefault(8, 0L)).isEqualTo(8);
        assertThat(fullProjectCount).isPositive();
    }

    @Test
    @DisplayName("시드 참가자는 모두 프로젝트 포지션이 연결되고 프로필 50명 전원이 최소 한 번은 팀원으로 배정된다")
    void seedsAssignProjectPositionsAndMemberRoles() {
        assertThat(projectParticipantRepository.findAll())
                .allSatisfy(participant -> assertThat(participant.getRecruitPosition()).isNotNull());

        long memberProfileCount = projectParticipantRepository.findAll().stream()
                .filter(participant -> "MEMBER".equals(participant.getRole()))
                .map(ProjectParticipant::getProfile)
                .map(profile -> profile.getId())
                .distinct()
                .count();

        assertThat(memberProfileCount).isEqualTo(50);
    }

    @Test
    @DisplayName("프로젝트 상세의 모집 포지션 현황과 리더 포지션은 실제 팀 배정과 일치한다")
    void projectDetailReflectsAssignedTeamPositions() {
        for (Project project : projectRepository.findAll()) {
            List<ProjectParticipant> participants = projectParticipantRepository.findByProject(project);
            Map<String, Long> participantCounts = participants.stream()
                    .map(ProjectParticipant::getRecruitPosition)
                    .collect(groupingBy(position -> position.getPositionName(), counting()));
            ProjectDetailView detail = projectService.getProjectDetail(project.getId());

            assertThat(detail.positionStatuses())
                    .allSatisfy(status -> assertThat(status.current())
                            .isEqualTo(participantCounts.getOrDefault(status.name(), 0L).intValue()));

            participants.stream()
                    .filter(participant -> "LEADER".equals(participant.getRole()))
                    .findFirst()
                    .ifPresent(leader -> assertThat(detail.leaderPosition())
                            .isEqualTo(leader.getRecruitPosition().getPositionName()));
        }
    }
}
