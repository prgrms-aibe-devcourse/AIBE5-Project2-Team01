package com.example.meetball.domain.catalog.support;

import java.util.List;

public final class CatalogDefaults {

    private CatalogDefaults() {
    }

    public static final List<PositionDefinition> POSITIONS = List.of(
            position("프론트엔드", 0, "프론트엔드 개발", "프론트엔드 개발자", "프론트엔드 리더"),
            position("백엔드", 1, "백엔드 개발", "백엔드 개발자"),
            position("풀스택", 2, "풀스택 개발", "풀스택 개발자"),
            position("디자이너", 3, "UI/UX 디자인", "UI/UX 디자이너"),
            position("IOS", 4, "iOS"),
            position("안드로이드", 5),
            position("임베디드SW", 6),
            position("게임", 7),
            position("서버", 8, "웹 서버"),
            position("데브옵스", 9, "DevOps"),
            position("매니저(PM)", 10, "기획/PM", "프로젝트 매니저", "PM"),
            position("기획자", 11),
            position("마케터", 12),
            position("사업", 13),
            position("데이터/AI", 14, "AI", "AI/데이터", "AI/데이터 엔지니어"),
            position("보안", 15),
            position("DBA", 16),
            position("QA", 17),
            position("기타", 18)
    );

    public static final List<TechStackDefinition> TECH_STACKS = List.of(
            tech("JavaScript", "프론트엔드", true, "JS", svg("javascript"), "#F7DF1E", "#2D2D2D", "#FFF8CC", 0, "JS"),
            tech("TypeScript", "프론트엔드", true, "TS", svg("typescript"), "#3178C6", "#FFFFFF", "#3178C6", 1, "TS"),
            tech("React", "프론트엔드", true, null, svg("react"), "#61DAFB", "#15AABF", "#ECFEFF", 2),
            tech("Vue", "프론트엔드", false, null, svg("vue"), "#42B883", "#2F855A", "#ECFDF5", 3),
            tech("Nodejs", "백엔드", false, null, svg("nodejs"), "#6CC24A", "#44883E", "#F0FDF4", 4,
                    "Node", "Node.js", "Node JS"),
            tech("Spring", "백엔드", true, "S", svg("spring"), "#6DB33F", "#6DB33F", "#F0FDF4", 5, "Spring Boot"),
            tech("Java", "백엔드", true, null, svg("java"), "#E76F00", "#E76F00", "#FFF7ED", 6),
            tech("Nextjs", "프론트엔드", true, "N", svg("nextjs"), "#000000", "#FFFFFF", "#000000", 7, "Next", "Next.js"),
            tech("Nestjs", "백엔드", false, "N", svg("nestjs"), "#E0234E", "#E0234E", "#FFF1F2", 8, "Nest", "Nest.js"),
            tech("Express", "백엔드", false, "ex", svg("express"), "#111827", "#111827", "#F9FAFB", 9),
            tech("Go", "백엔드", false, "Go", svg("go"), "#00ADD8", "#00ADD8", "#ECFEFF", 10),
            tech("C", "기타", false, "C", svg("c"), "#5C8DBC", "#315C8A", "#EFF6FF", 11),
            tech("Python", "백엔드", false, null, svg("python"), "#3776AB", "#3776AB", "#EFF6FF", 12),
            tech("Django", "백엔드", false, "dj", svg("django"), "#0C4B33", "#0C4B33", "#ECFDF5", 13),
            tech("Swift", "모바일", false, null, svg("swift"), "#FA7343", "#EA580C", "#FFF7ED", 14),
            tech("Kotlin", "모바일", false, "K", svg("kotlin"), "#7F52FF", "#7F52FF", "#F5F3FF", 15),
            tech("MySQL", "기타", false, "My", svg("mysql"), "#00758F", "#00758F", "#ECFEFF", 16, "My SQL"),
            tech("MongoDB", "기타", false, "M", svg("mongodb"), "#47A248", "#2F7D32", "#F0FDF4", 17),
            tech("php", "백엔드", false, "php", svg("php"), "#777BB4", "#5B5F9E", "#F5F3FF", 18, "PHP"),
            tech("GraphQL", "백엔드", false, "G", svg("graphql"), "#E10098", "#E10098", "#FDF2F8", 19),
            tech("Firebase", "기타", false, "F", svg("firebase"), "#FFCA28", "#F57C00", "#FFFBEB", 20),
            tech("ReactNative", "모바일", false, null, svg("reactnative"), "#61DAFB", "#15AABF", "#ECFEFF", 21,
                    "React Native"),
            tech("Unity", "기타", false, null, svg("unity"), "#111827", "#111827", "#F9FAFB", 22),
            tech("Flutter", "모바일", false, "F", svg("flutter"), "#54C5F8", "#027DFD", "#EFF6FF", 23),
            tech("AWS", "기타", true, null, svg("aws"), "#FF9900", "#232F3E", "#FFF7ED", 24),
            tech("Kubernetes", "기타", false, "K8s", svg("kubernetes"), "#326CE5", "#326CE5", "#EFF6FF", 25),
            tech("Docker", "기타", false, null, svg("docker"), "#2496ED", "#0B75C9", "#EFF6FF", 26),
            tech("Git", "기타", false, null, svg("git"), "#F05032", "#E24329", "#FFF1F2", 27),
            tech("Figma", "기타", true, null, svg("figma"), "#A259FF", "#A259FF", "#FAF5FF", 28),
            tech("Zeplin", "기타", false, "Z", svg("zeplin"), "#FDBD39", "#D97706", "#FFFBEB", 29),
            tech("Jest", "기타", false, "J", svg("jest"), "#99425B", "#99425B", "#FFF1F2", 30),
            tech("Svelte", "프론트엔드", false, "S", svg("svelte"), "#FF3E00", "#FF3E00", "#FFF7ED", 31)
    );

    private static String svg(String slug) {
        return "/img/tech-stack/" + slug + ".svg";
    }

    public static PositionDefinition position(String name, int sortOrder, String... aliases) {
        return new PositionDefinition(name, sortOrder, List.of(aliases));
    }

    public static TechStackDefinition tech(String name, String category, boolean popular, String badgeLabel,
                                           String iconClass, String accentColor, String textColor,
                                           String backgroundColor, int sortOrder, String... aliases) {
        return new TechStackDefinition(
                name,
                category,
                popular,
                badgeLabel,
                iconClass,
                accentColor,
                textColor,
                backgroundColor,
                sortOrder,
                List.of(aliases)
        );
    }

    public record PositionDefinition(String name, int sortOrder, List<String> aliases) {
    }

    public record TechStackDefinition(String name, String category, boolean popular, String badgeLabel,
                                      String iconClass, String accentColor, String textColor,
                                      String backgroundColor, int sortOrder, List<String> aliases) {
    }
}
