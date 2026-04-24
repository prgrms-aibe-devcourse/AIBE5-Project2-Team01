package com.example.meetball.domain.review.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.review.dto.ReviewRequestDto;
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
public class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectParticipantRepository projectParticipantRepository;

    private Profile leader;
    private Profile member;
    private Profile nonParticipant;
    private Project project;

    private Project createProject(String title, LocalDate recruitEndDate) {
        return new Project(
                title,
                "설명",
                "타입",
                "ONLINE",
                5,
                LocalDate.now().minusDays(10),
                recruitEndDate,
                LocalDate.now().minusDays(9),
                null,
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now()
        );
    }

    @BeforeEach
    void setUp() {
        leader = profileRepository.save(Profile.builder().email("l@t.com").nickname("리더").isPublic(true).build());
        member = profileRepository.save(Profile.builder().email("m@t.com").nickname("멤버").isPublic(true).build());
        nonParticipant = profileRepository.save(Profile.builder().email("n@t.com").nickname("불청객").isPublic(true).build());

        Project completedProject = createProject("리뷰 테스트 프로젝트", LocalDate.now().minusDays(1));
        completedProject.assignOwner(leader);
        completedProject.update(
                completedProject.getTitle(),
                completedProject.getDescription(),
                completedProject.getProjectType(),
                completedProject.getProgressMethod(),
                completedProject.getRecruitmentCount(),
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(9),
                LocalDate.now().minusDays(1),
                Project.RECRUIT_STATUS_CLOSED,
                Project.PROGRESS_STATUS_COMPLETED,
                LocalDateTime.now()
        );
        project = projectRepository.save(completedProject);

        // 멤버 관계 설정
        projectParticipantRepository.save(new ProjectParticipant(leader, project, "LEADER"));
        projectParticipantRepository.save(new ProjectParticipant(member, project, "MEMBER"));
    }

    @Test
    @DisplayName("리뷰 등록 성공 - 프로젝트 멤버인 경우")
    void addReview_Success() {
        // given
        // 멤버가 리더에게 리뷰를 남깁니다.
        ReviewRequestDto request = new ReviewRequestDto(5.0, "리더", "리더님 최고!");

        // when
        reviewService.addReview(project.getId(), member.getId(), request);

        // then
        double meetBallIndex = reviewService.calculateMeetBallIndex(leader);
        assertThat(meetBallIndex).isGreaterThan(36.5); // 지수 증가 확인
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 프로젝트 참여자가 아닌 경우")
    void addReview_NonParticipant_Fail() {
        // given
        ReviewRequestDto request = new ReviewRequestDto(5.0, "리더", "몰래 남기기");

        // when & then
        assertThatThrownBy(() -> reviewService.addReview(project.getId(), nonParticipant.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이 프로젝트의 참여 멤버만");
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 본인이 본인에게 리뷰 남기기")
    void addReview_SelfReview_Fail() {
        // given
        // 리더가 본인 '리더' 닉네임에게 리뷰 남기기 시도
        ReviewRequestDto request = new ReviewRequestDto(5.0, "리더", "나 최고!");

        // when & then
        assertThatThrownBy(() -> reviewService.addReview(project.getId(), leader.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("자신에 대한 리뷰는 작성할 수 없습니다");
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 모집 마감만 된 프로젝트는 완료 프로젝트가 아니다")
    void addReview_RecruitmentClosedButNotCompleted_Fail() {
        Project recruitmentClosedProject = createProject("모집만 마감된 프로젝트", LocalDate.now().minusDays(1));
        recruitmentClosedProject.assignOwner(leader);
        recruitmentClosedProject.update(
                recruitmentClosedProject.getTitle(),
                recruitmentClosedProject.getDescription(),
                recruitmentClosedProject.getProjectType(),
                recruitmentClosedProject.getProgressMethod(),
                recruitmentClosedProject.getRecruitmentCount(),
                LocalDate.now().minusDays(10),
                LocalDate.now().minusDays(1),
                LocalDate.now().minusDays(9),
                LocalDate.now().minusDays(1),
                Project.RECRUIT_STATUS_CLOSED,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now()
        );
        Project savedProject = projectRepository.save(recruitmentClosedProject);
        projectParticipantRepository.save(new ProjectParticipant(leader, savedProject, "LEADER"));
        projectParticipantRepository.save(new ProjectParticipant(member, savedProject, "MEMBER"));

        ReviewRequestDto request = new ReviewRequestDto(5.0, "리더", "모집 마감은 완료가 아님");

        assertThatThrownBy(() -> reviewService.addReview(savedProject.getId(), member.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("완료된 프로젝트");
    }

    @Test
    @DisplayName("리뷰 등록 실패 - 중복 리뷰 방지")
    void addReview_Duplicate_Fail() {
        // given
        ReviewRequestDto request = new ReviewRequestDto(5.0, "리더", "첫 리뷰");
        reviewService.addReview(project.getId(), member.getId(), request);

        // when & then
        assertThatThrownBy(() -> reviewService.addReview(project.getId(), member.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이미 해당 대상에 대해 리뷰를 제출하셨습니다");
    }
}
