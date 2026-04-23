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
            tech("JavaScript", "프론트엔드", true, "JS", null, "#F7DF1E", "#2D2D2D", "#FFF8CC", 0, "JS"),
            tech("TypeScript", "프론트엔드", true, "TS", null, "#3178C6", "#FFFFFF", "#3178C6", 1, "TS"),
            tech("React", "프론트엔드", true, null, "fa-brands fa-react", "#61DAFB", "#15AABF", "#ECFEFF", 2),
            tech("Vue", "프론트엔드", false, null, "fa-brands fa-vuejs", "#42B883", "#2F855A", "#ECFDF5", 3),
            tech("Nodejs", "백엔드", false, null, "fa-brands fa-node-js", "#6CC24A", "#44883E", "#F0FDF4", 4,
                    "Node", "Node.js", "Node JS"),
            tech("Spring", "백엔드", true, "S", null, "#6DB33F", "#FFFFFF", "#6DB33F", 5, "Spring Boot"),
            tech("Java", "백엔드", true, null, "fa-brands fa-java", "#E76F00", "#E76F00", "#FFF7ED", 6),
            tech("Nextjs", "프론트엔드", true, "N", null, "#000000", "#FFFFFF", "#000000", 7, "Next", "Next.js"),
            tech("Nestjs", "백엔드", false, "N", null, "#E0234E", "#E0234E", "#FFF1F2", 8, "Nest", "Nest.js"),
            tech("Express", "백엔드", false, "ex", null, "#111827", "#111827", "#F9FAFB", 9),
            tech("Go", "백엔드", false, "Go", null, "#00ADD8", "#00ADD8", "#ECFEFF", 10),
            tech("C", "기타", false, "C", null, "#5C8DBC", "#315C8A", "#EFF6FF", 11),
            tech("Python", "백엔드", false, null, "fa-brands fa-python", "#3776AB", "#3776AB", "#EFF6FF", 12),
            tech("Django", "백엔드", false, "dj", null, "#0C4B33", "#0C4B33", "#ECFDF5", 13),
            tech("Swift", "모바일", false, null, "fa-brands fa-swift", "#FA7343", "#EA580C", "#FFF7ED", 14),
            tech("Kotlin", "모바일", false, "K", null, "#7F52FF", "#7F52FF", "#F5F3FF", 15),
            tech("MySQL", "기타", false, "My", null, "#00758F", "#00758F", "#ECFEFF", 16, "My SQL"),
            tech("MongoDB", "기타", false, "M", null, "#47A248", "#2F7D32", "#F0FDF4", 17),
            tech("php", "백엔드", false, "php", null, "#777BB4", "#5B5F9E", "#F5F3FF", 18, "PHP"),
            tech("GraphQL", "백엔드", false, "G", null, "#E10098", "#E10098", "#FDF2F8", 19),
            tech("Firebase", "기타", false, "F", null, "#FFCA28", "#F57C00", "#FFFBEB", 20),
            tech("ReactNative", "모바일", false, null, "fa-brands fa-react", "#61DAFB", "#15AABF", "#ECFEFF", 21,
                    "React Native"),
            tech("Unity", "기타", false, null, "fa-brands fa-unity", "#111827", "#111827", "#F9FAFB", 22),
            tech("Flutter", "모바일", false, "F", null, "#54C5F8", "#027DFD", "#EFF6FF", 23),
            tech("AWS", "기타", true, null, "fa-brands fa-aws", "#FF9900", "#232F3E", "#FFF7ED", 24),
            tech("Kubernetes", "기타", false, "K8s", null, "#326CE5", "#326CE5", "#EFF6FF", 25),
            tech("Docker", "기타", false, null, "fa-brands fa-docker", "#2496ED", "#0B75C9", "#EFF6FF", 26),
            tech("Git", "기타", false, null, "fa-brands fa-git-alt", "#F05032", "#E24329", "#FFF1F2", 27),
            tech("Figma", "기타", true, null, "fa-brands fa-figma", "#A259FF", "#A259FF", "#FAF5FF", 28),
            tech("Zeplin", "기타", false, "Z", null, "#FDBD39", "#D97706", "#FFFBEB", 29),
            tech("Jest", "기타", false, "J", null, "#99425B", "#99425B", "#FFF1F2", 30),
            tech("Svelte", "프론트엔드", false, "S", null, "#FF3E00", "#FF3E00", "#FFF7ED", 31)
    );

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
