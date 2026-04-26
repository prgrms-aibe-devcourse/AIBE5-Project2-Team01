package com.example.meetball.domain.project.entity;

import com.example.meetball.domain.catalog.support.CatalogDefaults;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Test
    @DisplayName("모집 마감을 호출하면 모든 모집 포지션도 함께 닫힌다")
    void closeRecruitmentClosesEveryRecruitPosition() {
        Project project = createProjectWithPositions("프론트엔드:2, 백엔드:1");

        assertThat(project.getPositionSelections()).hasSize(2);
        project.closeRecruitment();

        assertThat(project.getRecruitStatus()).isEqualTo(Project.RECRUIT_STATUS_CLOSED);
        assertThat(project.getPositionSelections())
                .extracting(ProjectRecruitPosition::getRecruitStatus)
                .containsOnly(ProjectRecruitPosition.STATUS_CLOSED);
    }

    @Test
    @DisplayName("모집 마감 철회는 정원이 남은 포지션만 다시 연다")
    void reopenRecruitmentOnlyReopensAvailablePositions() {
        Project project = createProjectWithPositions("프론트엔드:2, 백엔드:1");

        assertThat(project.getPositionSelections()).hasSize(2);
        ProjectRecruitPosition frontend = project.getPositionSelections().stream()
                .filter(position -> position.getCapacity() == 2)
                .findFirst()
                .orElseThrow();
        ProjectRecruitPosition backend = project.getPositionSelections().stream()
                .filter(position -> position.getCapacity() == 1)
                .findFirst()
                .orElseThrow();

        backend.incrementApprovedUser();
        project.closeRecruitment();

        project.reopenRecruitment();

        assertThat(project.getRecruitStatus()).isEqualTo(Project.RECRUIT_STATUS_OPEN);
        assertThat(frontend.getRecruitStatus()).isEqualTo(ProjectRecruitPosition.STATUS_OPEN);
        assertThat(backend.getRecruitStatus()).isEqualTo(ProjectRecruitPosition.STATUS_CLOSED);
    }

    private Project createProjectWithPositions(String positionText) {
        Project project = new Project(
                "제목",
                "설명",
                "프로젝트",
                "온라인",
                3,
                LocalDate.now(),
                LocalDate.now().plusDays(14),
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(45),
                Project.RECRUIT_STATUS_OPEN,
                Project.PROGRESS_STATUS_READY,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusHours(1)
        );

        project.replacePositions(
                ProjectSelectionCatalog.parsePositionCapacities(positionText, null),
                this::resolvePosition
        );
        return project;
    }

    private Position resolvePosition(String name) {
        return switch (name) {
            case "프론트엔드" -> createPosition(1L, name);
            case "백엔드" -> createPosition(2L, name);
            default -> createPosition(99L, name);
        };
    }

    private Position createPosition(Long id, String name) {
        Position position = Position.from(new CatalogDefaults.PositionDefinition(name, 1, List.of()));
        try {
            Field idField = Position.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(position, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("테스트용 포지션 ID를 설정하지 못했습니다.", exception);
        }
        return position;
    }
}
