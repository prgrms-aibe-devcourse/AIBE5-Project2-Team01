package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.review.repository.ProjectReviewRepository;
import com.example.meetball.domain.review.repository.PeerReviewRepository;
import com.example.meetball.domain.profile.entity.Profile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectParticipantRepository projectParticipantRepository;

    @Mock
    private PeerReviewRepository peerReviewRepository;

    @Mock
    private ProjectReviewRepository projectReviewRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project createProject(String title, LocalDate recruitEndDate) {
        return new Project(
                title,
                "설명",
                "타입",
                "ONLINE",
                5,
                LocalDate.now().minusDays(1),
                recruitEndDate,
                LocalDate.now(),
                null,
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("오늘이 마감일인 경우 D-Day는 0이어야 한다")
    void calculateDDayZeroTest() {
        // given
        Profile profile = Profile.builder().nickname("테스트유저").build();
        Project project = createProject("D-Day 0 테스트", LocalDate.now());
        
        com.example.meetball.domain.project.entity.ProjectParticipant pm = 
            new com.example.meetball.domain.project.entity.ProjectParticipant(profile, project, "MEMBER");
        
        when(projectParticipantRepository.findByProfile(profile)).thenReturn(List.of(pm));

        // when
        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(profile);

        // then
        assertThat(results.get(0).getDDay()).isEqualTo(0L);
    }

    @Test
    @DisplayName("모집 마감과 프로젝트 완료 상태는 참여 프로젝트 응답에서 분리된다")
    void recruitmentClosedAndCompletedAreSeparated() {
        Profile profile = Profile.builder().nickname("테스트유저").build();
        Project recruitmentClosedProject = createProject("모집 마감 테스트", LocalDate.now().minusDays(1));
        recruitmentClosedProject.update(
                recruitmentClosedProject.getTitle(),
                recruitmentClosedProject.getDescription(),
                recruitmentClosedProject.getProjectType(),
                recruitmentClosedProject.getProgressMethod(),
                recruitmentClosedProject.getRecruitmentCount(),
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1),
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                Project.RECRUIT_STATUS_CLOSED,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now()
        );
        Project completedProject = createProject("완료 테스트", LocalDate.now().minusDays(1));
        completedProject.update(
                completedProject.getTitle(),
                completedProject.getDescription(),
                completedProject.getProjectType(),
                completedProject.getProgressMethod(),
                completedProject.getRecruitmentCount(),
                LocalDate.now().minusDays(5),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(4),
                LocalDate.now().minusDays(1),
                Project.RECRUIT_STATUS_CLOSED,
                Project.PROGRESS_STATUS_COMPLETED,
                LocalDateTime.now()
        );
        com.example.meetball.domain.project.entity.ProjectParticipant closedMember =
                new com.example.meetball.domain.project.entity.ProjectParticipant(profile, recruitmentClosedProject, "MEMBER");
        com.example.meetball.domain.project.entity.ProjectParticipant completedMember =
                new com.example.meetball.domain.project.entity.ProjectParticipant(profile, completedProject, "MEMBER");

        when(projectParticipantRepository.findByProfile(profile)).thenReturn(List.of(closedMember, completedMember));
        when(projectReviewRepository.existsByProjectAndReviewer(completedProject, profile)).thenReturn(false);

        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(profile);

        assertThat(results.get(0).getRecruitStatus()).isEqualTo(Project.RECRUIT_STATUS_CLOSED);
        assertThat(results.get(0).getProgressStatus()).isEqualTo(Project.PROGRESS_STATUS_READY);
        assertThat(results.get(0).isCanReview()).isFalse();
        assertThat(results.get(0).getStatus()).isEqualTo("PROCEEDING");
        assertThat(results.get(1).getRecruitStatus()).isEqualTo(Project.RECRUIT_STATUS_CLOSED);
        assertThat(results.get(1).getProgressStatus()).isEqualTo(Project.PROGRESS_STATUS_COMPLETED);
        assertThat(results.get(1).isCanReview()).isTrue();
        assertThat(results.get(1).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("내일이 마감일인 경우 D-Day는 1이어야 한다")
    void calculateDDayOneTest() {
        // given
        Profile profile = Profile.builder().nickname("테스트유저").build();
        Project project = createProject("D-Day 1 테스트", LocalDate.now().plusDays(1));
        
        com.example.meetball.domain.project.entity.ProjectParticipant pm = 
            new com.example.meetball.domain.project.entity.ProjectParticipant(profile, project, "MEMBER");
        
        when(projectParticipantRepository.findByProfile(profile)).thenReturn(List.of(pm));

        // when
        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(profile);

        // then
        assertThat(results.get(0).getDDay()).isEqualTo(1L);
    }
}
