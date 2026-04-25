package com.example.meetball.domain.project.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProjectOptionCatalogTest {

    @Test
    @DisplayName("프로젝트 목적 옵션은 요구된 고정 목록을 유지한다")
    void projectPurposeOptions_matchRequiredValues() {
        assertThat(ProjectOptionCatalog.projectPurposeOptions())
                .extracting(ProjectOptionCatalog.Option::getCode)
                .containsExactly("PROJECT", "STUDY", "HACKATHON", "CONTEST", "STARTUP", "ENTERPRISE_LINK", "GOVERNMENT_LINK");
        assertThat(ProjectOptionCatalog.projectPurposeOptions())
                .extracting(ProjectOptionCatalog.Option::getLabel)
                .containsExactly("프로젝트", "스터디", "해커톤", "공모전", "스타트업", "기업연계", "정부연계");
    }

    @Test
    @DisplayName("프로젝트 진행 방식 옵션은 요구된 고정 목록을 유지한다")
    void workMethodOptions_matchRequiredValues() {
        assertThat(ProjectOptionCatalog.workMethodOptions())
                .extracting(ProjectOptionCatalog.Option::getCode)
                .containsExactly("ONLINE", "OFFLINE", "HYBRID");
        assertThat(ProjectOptionCatalog.workMethodOptions())
                .extracting(ProjectOptionCatalog.Option::getLabel)
                .containsExactly("온라인", "오프라인", "온/오프라인");
    }

    @Test
    @DisplayName("프로젝트 목적은 정의된 값만 허용한다")
    void normalizeProjectPurpose_acceptsOnlyCatalogValues() {
        assertThat(ProjectOptionCatalog.normalizeProjectPurpose("프로젝트")).isEqualTo("PROJECT");
        assertThat(ProjectOptionCatalog.normalizeProjectPurpose("PROJECT")).isEqualTo("PROJECT");
        assertThat(ProjectOptionCatalog.displayProjectPurpose("PROJECT")).isEqualTo("프로젝트");
        assertThatThrownBy(() -> ProjectOptionCatalog.normalizeProjectPurpose("사이드 프로젝트"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("프로젝트 진행 방식은 정의된 값만 허용한다")
    void normalizeWorkMethod_acceptsOnlyCatalogValues() {
        assertThat(ProjectOptionCatalog.normalizeWorkMethod("온라인")).isEqualTo("ONLINE");
        assertThat(ProjectOptionCatalog.normalizeWorkMethod("HYBRID")).isEqualTo("HYBRID");
        assertThat(ProjectOptionCatalog.displayWorkMethod("HYBRID")).isEqualTo("온/오프라인");
        assertThatThrownBy(() -> ProjectOptionCatalog.normalizeWorkMethod("온/오프 혼합"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
