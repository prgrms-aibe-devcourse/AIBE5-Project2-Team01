package com.example.meetball.domain.project.support;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ProjectSelectionCatalog {

    private static final List<String> POSITION_OPTIONS = List.of(
            "프론트엔드",
            "백엔드",
            "풀스택",
            "디자이너",
            "IOS",
            "안드로이드",
            "임베디드SW",
            "게임",
            "서버",
            "데브옵스",
            "매니저(PM)",
            "기획자",
            "마케터",
            "사업",
            "데이터/AI",
            "보안",
            "DBA",
            "QA",
            "기타"
    );

    private static final Map<String, String> POSITION_ALIASES = Map.ofEntries(
            Map.entry(key("프론트엔드 개발"), "프론트엔드"),
            Map.entry(key("프론트엔드 개발자"), "프론트엔드"),
            Map.entry(key("백엔드 개발"), "백엔드"),
            Map.entry(key("백엔드 개발자"), "백엔드"),
            Map.entry(key("웹 서버"), "서버"),
            Map.entry(key("UI/UX 디자인"), "디자이너"),
            Map.entry(key("iOS"), "IOS"),
            Map.entry(key("기획/PM"), "매니저(PM)"),
            Map.entry(key("프로젝트 매니저"), "매니저(PM)"),
            Map.entry(key("PM"), "매니저(PM)"),
            Map.entry(key("DevOps"), "데브옵스"),
            Map.entry(key("AI"), "데이터/AI"),
            Map.entry(key("AI/데이터"), "데이터/AI"),
            Map.entry(key("AI/데이터 엔지니어"), "데이터/AI")
    );

    private static final List<String> TECH_STACK_OPTIONS = List.of(
            "JavaScript",
            "TypeScript",
            "React",
            "Vue",
            "Nodejs",
            "Spring",
            "Java",
            "Nextjs",
            "Nestjs",
            "Express",
            "Go",
            "C",
            "Python",
            "Django",
            "Swift",
            "Kotlin",
            "MySQL",
            "MongoDB",
            "php",
            "GraphQL",
            "Firebase",
            "ReactNative",
            "Unity",
            "Flutter",
            "AWS",
            "Kubernetes",
            "Docker",
            "Git",
            "Figma",
            "Zeplin",
            "Jest",
            "Svelte"
    );

    private static final Map<String, String> TECH_STACK_ALIASES = Map.ofEntries(
            Map.entry(key("JS"), "JavaScript"),
            Map.entry(key("TS"), "TypeScript"),
            Map.entry(key("Node"), "Nodejs"),
            Map.entry(key("Node.js"), "Nodejs"),
            Map.entry(key("Node JS"), "Nodejs"),
            Map.entry(key("Next"), "Nextjs"),
            Map.entry(key("Next.js"), "Nextjs"),
            Map.entry(key("Nest"), "Nestjs"),
            Map.entry(key("Nest.js"), "Nestjs"),
            Map.entry(key("React Native"), "ReactNative"),
            Map.entry(key("Spring Boot"), "Spring"),
            Map.entry(key("My SQL"), "MySQL"),
            Map.entry(key("PHP"), "php")
    );

    private static final String DEFAULT_TECH_STACK = "JavaScript";

    private ProjectSelectionCatalog() {
    }

    public static List<String> positionOptions() {
        return POSITION_OPTIONS;
    }

    public static List<String> techStackOptions() {
        return TECH_STACK_OPTIONS;
    }

    public static String searchKey(String value) {
        return key(value);
    }

    public static String normalizePositionCsv(String value) {
        LinkedHashMap<String, Integer> capacities = new LinkedHashMap<>();
        for (String token : splitCsv(value)) {
            ParsedPosition parsed = parsePositionToken(token, true);
            capacities.merge(parsed.name(), parsed.capacity(), Integer::sum);
        }
        if (capacities.isEmpty()) {
            throw new IllegalArgumentException("모집 포지션을 1개 이상 선택해주세요.");
        }
        return capacities.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    public static String normalizePositionCsvOrDefault(String value, Integer fallbackTotal) {
        LinkedHashMap<String, Integer> capacities = new LinkedHashMap<>();
        for (String token : splitCsv(value)) {
            String rawName = stripCapacity(token);
            String canonicalName = canonicalPositionNameOrNull(rawName);
            int capacity;
            try {
                capacity = parseCapacity(token).orElse(1);
            } catch (IllegalArgumentException exception) {
                capacity = 1;
            }
            capacities.merge(canonicalName != null ? canonicalName : "기타", Math.max(1, capacity), Integer::sum);
        }
        if (capacities.isEmpty()) {
            capacities.put("기타", Math.max(1, fallbackTotal == null ? 1 : fallbackTotal));
        }
        return capacities.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", "));
    }

    public static String normalizeTechStackCsv(String value) {
        List<String> techStacks = splitCsv(value).stream()
                .map(ProjectSelectionCatalog::canonicalTechStackName)
                .distinct()
                .toList();
        if (techStacks.isEmpty()) {
            throw new IllegalArgumentException("기술 스택을 1개 이상 선택해주세요.");
        }
        return String.join(", ", techStacks);
    }

    public static String normalizeTechStackCsvOrDefault(String value) {
        List<String> techStacks = splitCsv(value).stream()
                .map(ProjectSelectionCatalog::canonicalTechStackNameOrNull)
                .filter(option -> option != null && !option.isBlank())
                .distinct()
                .toList();
        if (techStacks.isEmpty()) {
            return DEFAULT_TECH_STACK;
        }
        return String.join(", ", techStacks);
    }

    public static List<String> normalizeTechStackFilters(String value) {
        return splitCsv(value).stream()
                .map(ProjectSelectionCatalog::canonicalTechStackName)
                .distinct()
                .toList();
    }

    public static List<PositionCapacity> parsePositionCapacities(String value, Integer fallbackTotal) {
        List<String> tokens = splitCsv(value);
        if (tokens.isEmpty()) {
            return List.of();
        }
        int fallbackCapacity = fallbackTotal != null && fallbackTotal > 0 && tokens.size() == 1 ? fallbackTotal : 1;
        LinkedHashMap<String, Integer> capacities = new LinkedHashMap<>();
        for (String token : tokens) {
            ParsedPosition parsed = parsePositionTokenLenient(token, fallbackCapacity);
            capacities.merge(parsed.name(), parsed.capacity(), Integer::sum);
        }
        return capacities.entrySet().stream()
                .map(entry -> new PositionCapacity(entry.getKey(), entry.getValue()))
                .toList();
    }

    public static int totalCapacity(String normalizedPositions) {
        return parsePositionCapacities(normalizedPositions, null).stream()
                .mapToInt(PositionCapacity::capacity)
                .sum();
    }

    public static String positionName(String value) {
        return parsePositionTokenLenient(value, 1).name();
    }

    private static List<String> splitCsv(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    private static ParsedPosition parsePositionToken(String token, boolean strict) {
        String name = stripCapacity(token);
        int capacity = parseCapacity(token).orElse(1);
        String canonicalName = strict ? canonicalPositionName(name) : canonicalPositionNameOrOriginal(name);
        if (capacity < 1) {
            throw new IllegalArgumentException("포지션별 모집 인원은 1명 이상이어야 합니다.");
        }
        return new ParsedPosition(canonicalName, capacity);
    }

    private static ParsedPosition parsePositionTokenLenient(String token, int defaultCapacity) {
        String name = stripCapacity(token);
        int capacity = parseCapacity(token).orElse(defaultCapacity);
        return new ParsedPosition(canonicalPositionNameOrOriginal(name), Math.max(1, capacity));
    }

    private static String stripCapacity(String token) {
        int delimiterIndex = token.lastIndexOf(':');
        String name = delimiterIndex >= 0 ? token.substring(0, delimiterIndex) : token;
        return name.trim();
    }

    private static Optional<Integer> parseCapacity(String token) {
        int delimiterIndex = token.lastIndexOf(':');
        if (delimiterIndex < 0 || delimiterIndex == token.length() - 1) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.parseInt(token.substring(delimiterIndex + 1).trim()));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("포지션별 모집 인원은 숫자로 입력해주세요.");
        }
    }

    private static String canonicalPositionName(String value) {
        String canonical = canonicalPositionNameOrOriginal(value);
        if (!POSITION_OPTIONS.contains(canonical)) {
            throw new IllegalArgumentException("허용되지 않은 모집 포지션입니다: " + value);
        }
        return canonical;
    }

    private static String canonicalPositionNameOrOriginal(String value) {
        String option = canonicalPositionNameOrNull(value);
        if (option != null) {
            return option;
        }
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("모집 포지션을 선택해주세요.");
        }
        return trimmed;
    }

    private static String canonicalPositionNameOrNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return POSITION_OPTIONS.stream()
                .filter(position -> key(position).equals(key(trimmed)))
                .findFirst()
                .orElse(POSITION_ALIASES.get(key(trimmed)));
    }

    private static String canonicalTechStackName(String value) {
        String option = canonicalTechStackNameOrNull(value);
        if (option == null) {
            throw new IllegalArgumentException("허용되지 않은 기술 스택입니다: " + value);
        }
        return option;
    }

    private static String canonicalTechStackNameOrNull(String value) {
        String trimmed = value == null ? "" : value.trim();
        String option = TECH_STACK_OPTIONS.stream()
                .filter(tech -> key(tech).equals(key(trimmed)))
                .findFirst()
                .orElse(TECH_STACK_ALIASES.get(key(trimmed)));
        return option;
    }

    private static String key(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private record ParsedPosition(String name, int capacity) {
    }

    public record PositionCapacity(String name, int capacity) {
    }
}
