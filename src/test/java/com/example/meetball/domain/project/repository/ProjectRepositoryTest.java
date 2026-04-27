package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private Project saveProject(String title, String projectPurpose) {
        Profile owner = profileRepository.save(Profile.builder()
                .email(title.replaceAll("\\s+", "").toLowerCase() + "@test.local")
                .nickname(title + "리더")
                .isPublic(true)
                .build());
        Project project = new Project(
                title,
                "설명",
                projectPurpose,
                "ONLINE",
                5,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(7),
                LocalDate.now().plusDays(14),
                null,
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now()
        );
        project.assignOwner(owner);
        return projectRepository.save(project);
    }

    @Test
    @DisplayName("프로젝트 필터 검색 - 키워드 테스트")
    void findProjectsWithFilters_Keyword() {
        // given
        saveProject("Spring Boot 스터디", "PROJECT");
        saveProject("React 프로젝트", "PROJECT");

        // when
        Page<Project> results = projectRepository.findProjectsWithFilters("Spring", null, null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("프로젝트 필터 검색 - 목적 테스트")
    void findProjectsWithFilters_Type() {
        // given
        saveProject("스터디", "STUDY");
        saveProject("공모전", "CONTEST");

        // when
        Page<Project> results = projectRepository.findProjectsWithFilters(null, "STUDY", null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getProjectPurpose()).isEqualTo("STUDY");
    }
}
