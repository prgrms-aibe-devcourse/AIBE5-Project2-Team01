package com.example.meetball.global.config;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class ProjectDataInitializer {

    @Bean
    CommandLineRunner initializeProjects(ProjectRepository projectRepository) {
        return args -> {
            if (projectRepository.count() > 0) {
                return;
            }

            projectRepository.saveAll(List.of(
                    new Project(
                            "AI 기반 헬스케어 모바일 앱 개발",
                            "사용자 건강 데이터를 분석하여 맞춤형 운동과 식단을 추천하는 모바일 헬스케어 프로젝트입니다.",
                            "운동 기록, 식단 관리, 수면 패턴 분석 기능을 제공하는 AI 헬스케어 앱을 함께 만들 팀원을 모집합니다.",
                            "사이드 프로젝트",
                            "백엔드",
                            "김밋볼",
                            "Backend Developer",
                            "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                            "https://picsum.photos/seed/health/800/400",
                            3,
                            5,
                            LocalDate.now().plusDays(14),
                            LocalDate.now().minusDays(2),
                            "Spring Boot, Thymeleaf, JPA, MySQL"
                    ),
                    new Project(
                            "대학생 팀 매칭 플랫폼 구축",
                            "프로젝트 팀원을 빠르게 찾고 협업 이력을 관리할 수 있는 캠퍼스 기반 매칭 서비스를 만듭니다.",
                            "학생들이 프로젝트 주제와 포지션을 등록하고 팀원을 모집할 수 있는 웹 서비스를 개발합니다.",
                            "스타트업",
                            "프론트엔드",
                            "초코푸들",
                            "Frontend Developer",
                            "https://api.dicebear.com/7.x/adventurer/svg?seed=choco",
                            "https://picsum.photos/seed/teamup/800/400",
                            2,
                            4,
                            LocalDate.now().plusDays(7),
                            LocalDate.now().minusDays(5),
                            "Java, Spring Boot, Tailwind, H2"
                    ),
                    new Project(
                            "공모전 협업 관리 대시보드",
                            "공모전 일정, 산출물, 역할을 한 번에 관리할 수 있는 협업 대시보드를 제작합니다.",
                            "공모전 준비 과정에서 필요한 일정 관리와 파일 공유, 역할 분담 기능을 중심으로 MVP를 개발합니다.",
                            "공모전",
                            "디자이너",
                            "디자인푸들",
                            "Product Designer",
                            "https://api.dicebear.com/7.x/adventurer/svg?seed=design",
                            "https://picsum.photos/seed/dashboard/800/400",
                            1,
                            3,
                            LocalDate.now().plusDays(21),
                            LocalDate.now().minusDays(1),
                            "Thymeleaf, JavaScript, H2, Figma"
                    )
            ));
        };
    }
}
