package com.example.meetball.domain.project.support;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ProjectSelectionCatalog {

    private static final List<String> DEFAULT_POSITION_OPTIONS = List.of(
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

    private static final Map<String, String> DEFAULT_POSITION_ALIASES = Map.ofEntries(
            Map.entry(key("프론트엔드 개발"), "프론트엔드"),
            Map.entry(key("프론트엔드 개발자"), "프론트엔드"),
            Map.entry(key("프론트엔드 리더"), "프론트엔드"),
            Map.entry(key("백엔드 개발"), "백엔드"),
            Map.entry(key("백엔드 개발자"), "백엔드"),
            Map.entry(key("풀스택 개발"), "풀스택"),
            Map.entry(key("풀스택 개발자"), "풀스택"),
            Map.entry(key("웹 서버"), "서버"),
            Map.entry(key("UI/UX 디자인"), "디자이너"),
            Map.entry(key("UI/UX 디자이너"), "디자이너"),
            Map.entry(key("iOS"), "IOS"),
            Map.entry(key("기획/PM"), "매니저(PM)"),
            Map.entry(key("프로젝트 매니저"), "매니저(PM)"),
            Map.entry(key("PM"), "매니저(PM)"),
            Map.entry(key("DevOps"), "데브옵스"),
            Map.entry(key("AI"), "데이터/AI"),
            Map.entry(key("AI/데이터"), "데이터/AI"),
            Map.entry(key("AI/데이터 엔지니어"), "데이터/AI")
    );

    private static final List<String> DEFAULT_TECH_STACK_OPTIONS = List.of(
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

    private static final Map<String, String> DEFAULT_TECH_STACK_ALIASES = Map.ofEntries(
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

    private static volatile List<String> positionOptions = DEFAULT_POSITION_OPTIONS;
    private static volatile Map<String, String> positionAliases = DEFAULT_POSITION_ALIASES;
    private static volatile List<String> techStackOptions = DEFAULT_TECH_STACK_OPTIONS;
    private static volatile Map<String, String> techStackAliases = DEFAULT_TECH_STACK_ALIASES;

    private ProjectSelectionCatalog() {
    }

    public static void configure(List<String> positions, Map<String, String> positionAliasMap,
                                 List<String> techStacks, Map<String, String> techStackAliasMap) {
        List<String> nextPositions = normalizeOptions(positions);
        List<String> nextTechStacks = normalizeOptions(techStacks);
        if (!nextPositions.isEmpty()) {
            positionOptions = nextPositions;
        }
        if (!nextTechStacks.isEmpty()) {
            techStackOptions = nextTechStacks;
        }
        positionAliases = normalizeAliasMap(positionAliasMap);
        techStackAliases = normalizeAliasMap(techStackAliasMap);
    }

    public static void resetDefaults() {
        positionOptions = DEFAULT_POSITION_OPTIONS;
        positionAliases = DEFAULT_POSITION_ALIASES;
        techStackOptions = DEFAULT_TECH_STACK_OPTIONS;
        techStackAliases = DEFAULT_TECH_STACK_ALIASES;
    }

    public static List<String> positionOptions() {
        return positionOptions;
    }

    public static List<String> techStackOptions() {
        return techStackOptions;
    }

    public static String searchKey(String value) {
        return key(value);
    }

    public static String normalizePositionText(String value) {
        LinkedHashMap<String, Integer> capacities = new LinkedHashMap<>();
        for (String token : splitCommaSeparated(value)) {
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

    public static String normalizeTechStackText(String value) {
        return String.join(", ", normalizeTechStackNames(splitCommaSeparated(value)));
    }

    public static List<String> normalizeTechStackNames(List<String> values) {
        List<String> techStacks = (values == null ? List.<String>of() : values).stream()
                .map(ProjectSelectionCatalog::canonicalTechStackName)
                .distinct()
                .toList();
        if (techStacks.isEmpty()) {
            throw new IllegalArgumentException("기술 스택을 1개 이상 선택해주세요.");
        }
        return techStacks;
    }

    public static List<String> normalizeTechStackFilters(String value) {
        return splitCommaSeparated(value).stream()
                .map(ProjectSelectionCatalog::canonicalTechStackName)
                .distinct()
                .toList();
    }

    public static List<PositionCapacity> parsePositionCapacities(String value, Integer fallbackTotal) {
        List<String> tokens = splitCommaSeparated(value);
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

    public static String normalizeSinglePositionName(String value) {
        return parsePositionToken(value, true).name();
    }

    public static String normalizeSinglePositionNameOrDefault(String value) {
        try {
            return normalizeSinglePositionName(value);
        } catch (IllegalArgumentException exception) {
            return "기타";
        }
    }

    private static List<String> splitCommaSeparated(String value) {
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
        if (!positionOptions.contains(canonical)) {
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
        return positionOptions.stream()
                .filter(position -> key(position).equals(key(trimmed)))
                .findFirst()
                .orElse(positionAliases.get(key(trimmed)));
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
        String option = techStackOptions.stream()
                .filter(tech -> key(tech).equals(key(trimmed)))
                .findFirst()
                .orElse(techStackAliases.get(key(trimmed)));
        return option;
    }

    private static String key(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private static List<String> normalizeOptions(List<String> values) {
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(value -> value == null ? "" : value.trim())
                .filter(value -> !value.isEmpty())
                .distinct()
                .toList();
    }

    private static Map<String, String> normalizeAliasMap(Map<String, String> aliases) {
        if (aliases == null || aliases.isEmpty()) {
            return Map.of();
        }
        return aliases.entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .filter(entry -> !entry.getKey().isBlank() && !entry.getValue().isBlank())
                .collect(Collectors.toUnmodifiableMap(
                        entry -> key(entry.getKey()),
                        entry -> entry.getValue().trim(),
                        (left, right) -> left
                ));
    }

    private record ParsedPosition(String name, int capacity) {
    }

    public record PositionCapacity(String name, int capacity) {
    }
}
