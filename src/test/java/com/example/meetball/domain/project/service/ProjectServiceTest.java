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

    @Test
    @DisplayName("오늘이 마감일인 경우 D-Day는 0이어야 한다")
    void calculateDDayZeroTest() {
        // given
        Profile profile = Profile.builder().nickname("테스트유저").build();
        Project project = new Project(
                "D-Day 0 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now(), LocalDate.now().minusDays(1), List.of("Java")
        );
        
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
        Project recruitmentClosedProject = new Project(
                "모집 마감 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now().minusDays(1), LocalDate.now().minusDays(3), List.of("Java")
        );
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
                true,
                false,
                LocalDateTime.now()
        );
        Project completedProject = new Project(
                "완료 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now().minusDays(1), LocalDate.now().minusDays(3), List.of("Java")
        );
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
                true,
                true,
                LocalDateTime.now()
        );
        com.example.meetball.domain.project.entity.ProjectParticipant closedMember =
                new com.example.meetball.domain.project.entity.ProjectParticipant(profile, recruitmentClosedProject, "MEMBER");
        com.example.meetball.domain.project.entity.ProjectParticipant completedMember =
                new com.example.meetball.domain.project.entity.ProjectParticipant(profile, completedProject, "MEMBER");

        when(projectParticipantRepository.findByProfile(profile)).thenReturn(List.of(closedMember, completedMember));
        when(projectReviewRepository.existsByProjectAndReviewer(completedProject, profile)).thenReturn(false);

        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(profile);

        assertThat(results.get(0).isClosed()).isTrue();
        assertThat(results.get(0).isCompleted()).isFalse();
        assertThat(results.get(0).isCanReview()).isFalse();
        assertThat(results.get(0).getStatus()).isEqualTo("PROCEEDING");
        assertThat(results.get(1).isClosed()).isTrue();
        assertThat(results.get(1).isCompleted()).isTrue();
        assertThat(results.get(1).isCanReview()).isTrue();
        assertThat(results.get(1).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("내일이 마감일인 경우 D-Day는 1이어야 한다")
    void calculateDDayOneTest() {
        // given
        Profile profile = Profile.builder().nickname("테스트유저").build();
        Project project = new Project(
                "D-Day 1 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now().plusDays(1), LocalDate.now().minusDays(1), List.of("Java")
        );
        
        com.example.meetball.domain.project.entity.ProjectParticipant pm = 
            new com.example.meetball.domain.project.entity.ProjectParticipant(profile, project, "MEMBER");
        
        when(projectParticipantRepository.findByProfile(profile)).thenReturn(List.of(pm));

        // when
        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(profile);

        // then
        assertThat(results.get(0).getDDay()).isEqualTo(1L);
    }
}
