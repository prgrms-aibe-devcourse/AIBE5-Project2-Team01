package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("오늘이 마감일인 경우 D-Day는 0이어야 한다")
    void calculateDDayZeroTest() {
        // given
        User user = User.builder().nickname("테스트유저").build();
        Project project = new Project(
                "D-Day 0 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now(), LocalDate.now().minusDays(1), "Java"
        );
        
        com.example.meetball.domain.project.entity.ProjectMember pm = 
            new com.example.meetball.domain.project.entity.ProjectMember(user, project, "MEMBER");
        
        when(projectMemberRepository.findByUser(user)).thenReturn(List.of(pm));
        when(reviewRepository.existsByProjectAndReviewer(any(), any())).thenReturn(false);

        // when
        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(user);

        // then
        assertThat(results.get(0).getDDay()).isEqualTo(0L);
    }

    @Test
    @DisplayName("내일이 마감일인 경우 D-Day는 1이어야 한다")
    void calculateDDayOneTest() {
        // given
        User user = User.builder().nickname("테스트유저").build();
        Project project = new Project(
                "D-Day 1 테스트", "요약", "설명", "타입", "포지션", "리더", "역할", "아바타", "썸네일",
                0, 5, LocalDate.now().plusDays(1), LocalDate.now().minusDays(1), "Java"
        );
        
        com.example.meetball.domain.project.entity.ProjectMember pm = 
            new com.example.meetball.domain.project.entity.ProjectMember(user, project, "MEMBER");
        
        when(projectMemberRepository.findByUser(user)).thenReturn(List.of(pm));
        when(reviewRepository.existsByProjectAndReviewer(any(), any())).thenReturn(false);

        // when
        List<ParticipatedProjectResponse> results = projectService.getParticipatedProjects(user);

        // then
        assertThat(results.get(0).getDDay()).isEqualTo(1L);
    }
}
