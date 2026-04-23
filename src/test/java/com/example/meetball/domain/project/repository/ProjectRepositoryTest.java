package com.example.meetball.domain.project.repository;

import com.example.meetball.domain.project.entity.Project;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    @DisplayName("프로젝트 필터 검색 - 키워드 테스트")
    void findProjectsWithFilters_Keyword() {
        // given
        projectRepository.save(new Project("Spring Boot 스터디", "요약", "설명", "사이드", "온라인", "팀장", "역할", "아", "썸", 0, 5, LocalDate.now(), LocalDate.now(), List.of("Java")));
        projectRepository.save(new Project("React 프로젝트", "요약", "설명", "사이드", "온라인", "팀장", "역할", "아", "썸", 0, 5, LocalDate.now(), LocalDate.now(), List.of("JavaScript")));

        // when
        Page<Project> results = projectRepository.findProjectsWithFilters("Spring", null, null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getTitle()).contains("Spring");
    }

    @Test
    @DisplayName("프로젝트 필터 검색 - 타입 테스트")
    void findProjectsWithFilters_Type() {
        // given
        projectRepository.save(new Project("스터디", "요약", "설명", "STUDY", "온라인", "팀장", "역할", "아", "썸", 0, 5, LocalDate.now(), LocalDate.now(), List.of("Java")));
        projectRepository.save(new Project("공모전", "요약", "설명", "COMPETITION", "온라인", "팀장", "역할", "아", "썸", 0, 5, LocalDate.now(), LocalDate.now(), List.of("JavaScript")));

        // when
        Page<Project> results = projectRepository.findProjectsWithFilters(null, "STUDY", null, null, null, PageRequest.of(0, 10));

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getProjectType()).isEqualTo("STUDY");
    }
}
