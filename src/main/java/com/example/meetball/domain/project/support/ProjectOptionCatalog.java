package com.example.meetball.domain.project.support;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ProjectOptionCatalog {

    public static final class Option {
        private final String code;
        private final String label;

        public Option(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public String getLabel() {
            return label;
        }
    }

    private static final List<Option> PROJECT_PURPOSE_OPTIONS = List.of(
            new Option("PROJECT", "프로젝트"),
            new Option("STUDY", "스터디"),
            new Option("HACKATHON", "해커톤"),
            new Option("CONTEST", "공모전"),
            new Option("STARTUP", "스타트업"),
            new Option("ENTERPRISE_LINK", "기업연계"),
            new Option("GOVERNMENT_LINK", "정부연계")
    );

    private static final List<Option> WORK_METHOD_OPTIONS = List.of(
            new Option("ONLINE", "온라인"),
            new Option("OFFLINE", "오프라인"),
            new Option("HYBRID", "온/오프라인")
    );

    private static final Map<String, Option> PROJECT_PURPOSE_BY_CODE = indexByCode(PROJECT_PURPOSE_OPTIONS);
    private static final Map<String, Option> PROJECT_PURPOSE_BY_LABEL = indexByLabel(PROJECT_PURPOSE_OPTIONS);
    private static final Map<String, Option> WORK_METHOD_BY_CODE = indexByCode(WORK_METHOD_OPTIONS);
    private static final Map<String, Option> WORK_METHOD_BY_LABEL = indexByLabel(WORK_METHOD_OPTIONS);

    private ProjectOptionCatalog() {
    }

    public static List<Option> projectPurposeOptions() {
        return PROJECT_PURPOSE_OPTIONS;
    }

    public static List<Option> workMethodOptions() {
        return WORK_METHOD_OPTIONS;
    }

    public static String defaultProjectPurpose() {
        return PROJECT_PURPOSE_OPTIONS.get(0).getCode();
    }

    public static String defaultWorkMethod() {
        return WORK_METHOD_OPTIONS.get(0).getCode();
    }

    public static String normalizeProjectPurpose(String value) {
        return canonical(value, PROJECT_PURPOSE_BY_CODE, PROJECT_PURPOSE_BY_LABEL, "프로젝트 목적");
    }

    public static String normalizeWorkMethod(String value) {
        return canonical(value, WORK_METHOD_BY_CODE, WORK_METHOD_BY_LABEL, "프로젝트 진행 방식");
    }

    public static String displayProjectPurpose(String value) {
        return display(value, PROJECT_PURPOSE_BY_CODE, PROJECT_PURPOSE_BY_LABEL);
    }

    public static String displayWorkMethod(String value) {
        return display(value, WORK_METHOD_BY_CODE, WORK_METHOD_BY_LABEL);
    }

    public static List<String> projectPurposeSearchTokens(String value) {
        return List.of(normalizeProjectPurpose(value));
    }

    public static List<String> workMethodSearchTokens(String value) {
        return List.of(normalizeWorkMethod(value));
    }

    private static Map<String, Option> indexByCode(List<Option> options) {
        return options.stream().collect(Collectors.toUnmodifiableMap(Option::getCode, Function.identity()));
    }

    private static Map<String, Option> indexByLabel(List<Option> options) {
        return options.stream().collect(Collectors.toUnmodifiableMap(Option::getLabel, Function.identity()));
    }

    private static String display(String value, Map<String, Option> byCode, Map<String, Option> byLabel) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String cleaned = value.trim();
        Option option = byCode.get(cleaned);
        if (option != null) {
            return option.getLabel();
        }
        Option labeledOption = byLabel.get(cleaned);
        return labeledOption != null ? labeledOption.getLabel() : cleaned;
    }

    private static String canonical(String value, Map<String, Option> byCode, Map<String, Option> byLabel, String label) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String cleaned = value.trim();
        Option option = byCode.get(cleaned);
        if (option != null) {
            return option.getCode();
        }
        Option labeledOption = byLabel.get(cleaned);
        if (labeledOption != null) {
            return labeledOption.getCode();
        }
        throw new IllegalArgumentException(label + "이(가) 올바르지 않습니다.");
    }
}
