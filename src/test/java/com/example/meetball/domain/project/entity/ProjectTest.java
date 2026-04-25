package com.example.meetball.domain.project.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectTest {

    @Test
    @DisplayName("수정 요청의 목적과 진행 방식이 비어 있으면 기존 저장 코드를 유지한다")
    void updateKeepsStoredCodesWhenPurposeAndWorkMethodAreBlank() {
        Project project = new Project(
                "제목",
                "설명",
                "공모전",
                "온/오프라인",
                5,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(45),
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1)
        );

        assertThat(project.getProjectPurpose()).isEqualTo("CONTEST");
        assertThat(project.getWorkMethod()).isEqualTo("HYBRID");

        project.update(
                "수정 제목",
                "수정 설명",
                "",
                null,
                6,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(50),
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now()
        );

        assertThat(project.getProjectPurpose()).isEqualTo("CONTEST");
        assertThat(project.getWorkMethod()).isEqualTo("HYBRID");
    }
}
