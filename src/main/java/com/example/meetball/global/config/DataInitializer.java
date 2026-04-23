package com.example.meetball.global.config;

import com.example.meetball.domain.bookmarkedproject.entity.BookmarkedProject;
import com.example.meetball.domain.bookmarkedproject.repository.BookmarkedProjectRepository;
import com.example.meetball.domain.position.entity.Position;
import com.example.meetball.domain.techstack.entity.TechStack;
import com.example.meetball.domain.position.repository.PositionRepository;
import com.example.meetball.domain.techstack.repository.TechStackRepository;
import com.example.meetball.domain.projectapplication.entity.ProjectApplication;
import com.example.meetball.domain.projectapplication.entity.ProjectApplicationStatus;
import com.example.meetball.domain.projectapplication.repository.ProjectApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectParticipant;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.project.support.ProjectSelectionCatalog;
import com.example.meetball.domain.viewhistory.entity.ViewHistory;
import com.example.meetball.domain.viewhistory.repository.ViewHistoryRepository;
import com.example.meetball.domain.review.entity.ProjectReview;
import com.example.meetball.domain.review.entity.PeerReview;
import com.example.meetball.domain.review.repository.ProjectReviewRepository;
import com.example.meetball.domain.review.repository.PeerReviewRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 200)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final ProfileRepository profileRepository;
    private final ProjectRepository projectRepository;
    private final ProjectParticipantRepository projectParticipantRepository;
    private final ProjectApplicationRepository projectApplicationRepository;
    private final BookmarkedProjectRepository bookmarkedProjectRepository;
    private final ViewHistoryRepository viewHistoryRepository;
    private final PeerReviewRepository peerReviewRepository;
    private final ProjectReviewRepository projectReviewRepository;
    private final PositionRepository positionRepository;
    private final TechStackRepository techStackRepository;

    @Override
    @Transactional
    public void run(String... args) {
        Profile leader = ensureProfile("leader@meetball.local", "초코푸들", "프론트엔드", List.of("React", "TypeScript"));
        Profile member = ensureProfile("member@meetball.local", "성실한리트리버", "백엔드", List.of("Java", "Spring"));
        Profile guest = ensureProfile("guest@meetball.local", "열정고양이", "디자이너", List.of("Figma"));
        Profile dev = ensureProfile("dev@meetball.local", "코딩하는비글", "풀스택", List.of("Nextjs", "Nodejs"));

        if (projectRepository.count() > 0) {
            return;
        }

        Project project1 = createProject(
                "AI 기반 헬스케어 모바일 앱 개발",
                "사용자의 건강 데이터를 분석하여 맞춤형 운동 및 식단을 추천하는 AI 헬스케어 앱 개발입니다.",
                "사이드 프로젝트",
                "ONLINE",
                "프론트엔드:2, 백엔드:1, 디자이너:1",
                leader,
                List.of("ReactNative", "Python"),
                false
        );
        projectParticipantRepository.save(ProjectParticipant.builder().project(project1).profile(member).role("MEMBER").build());
        projectApplicationRepository.save(ProjectApplication.builder()
                .project(project1)
                .profile(guest)
                .applicantName(guest.getNickname())
                .position("디자이너")
                .recruitPosition(findRecruitPosition(project1, "디자이너"))
                .message("디자인 시스템과 화면 플로우를 함께 만들고 싶습니다.")
                .status(ProjectApplicationStatus.PENDING)
                .build());

        Project project2 = createProject(
                "반려견 케어 서비스 [멍멍 비서]",
                "반려견의 일정을 관리하고 산책 메이트를 매칭해주는 서비스입니다.",
                "스타트업",
                "ONLINE",
                "매니저(PM):1, 프론트엔드:2, 백엔드:2",
                leader,
                List.of("Nextjs", "Spring"),
                true
        );
        projectParticipantRepository.save(ProjectParticipant.builder().project(project2).profile(dev).role("MEMBER").build());

        Project project3 = createProject(
                "여행 일정 공유 플랫폼 [TripMate]",
                "전 세계 여행자들과 일정을 공유하고 동행을 구하는 소셜 플랫폼입니다.",
                "사이드 프로젝트",
                "HYBRID",
                "IOS:1, 안드로이드:2, 백엔드:2",
                member,
                List.of("Flutter", "Firebase"),
                false
        );

        bookmarkedProjectRepository.save(BookmarkedProject.builder().profile(guest).project(project1).build());
        project1.incrementBookmarkCount();
        bookmarkedProjectRepository.save(BookmarkedProject.builder().profile(guest).project(project3).build());
        project3.incrementBookmarkCount();

        viewHistoryRepository.save(ViewHistory.builder().profile(guest).project(project1).build());
        project1.incrementViewCount();
        viewHistoryRepository.save(ViewHistory.builder().profile(leader).project(project2).build());
        project2.incrementViewCount();

        peerReviewRepository.save(PeerReview.builder()
                .project(project2)
                .reviewer(dev)
                .reviewee(leader)
                .content("리더님의 명확한 디렉션 덕분에 일정 내에 멋진 결과물을 낼 수 있었습니다!")
                .score(5.0)
                .build());
        projectReviewRepository.save(new ProjectReview(project2, leader, "완료 후에도 다시 참여하고 싶은 좋은 프로젝트였습니다.", 5.0));
    }

    private Profile ensureProfile(String email, String nickname, String positionName, List<String> techStackNames) {
        return profileRepository.findByEmail(email)
                .orElseGet(() -> {
                    Profile profile = Profile.builder()
                            .email(email)
                            .nickname(nickname)
                            .jobTitle(positionName)
                            .techStacks(techStackNames)
                            .isPublic(true)
                            .role("MEMBER")
                            .build();
                    profile.updateProfile(nickname, resolvePosition(positionName), resolveTechStacks(techStackNames), true);
                    return profileRepository.save(profile);
                });
    }

    private Project createProject(String title, String description, String type, String method, String positions,
                                  Profile owner, List<String> techStacks, boolean completed) {
        int requiredMember = ProjectSelectionCatalog.totalCapacity(positions);
        LocalDate now = LocalDate.now();
        Project project = new Project(
                title,
                description,
                type,
                method,
                requiredMember,
                now.minusDays(3),
                completed ? now.minusDays(1) : now.plusDays(14),
                now.plusDays(21),
                completed ? now.minusDays(1) : null,
                completed,
                completed,
                LocalDateTime.now().minusDays(3),
                LocalDateTime.now()
        );
        project.assignOwner(owner);
        project.replacePositions(ProjectSelectionCatalog.parsePositionCapacities(positions, requiredMember), this::resolvePosition);
        project.replaceTechStacks(resolveTechStacks(techStacks));
        project.updateDiscoveryFields(positions, techStacks, "");
        Project saved = projectRepository.save(project);
        projectParticipantRepository.save(ProjectParticipant.builder().project(saved).profile(owner).role("LEADER").build());
        return saved;
    }

    private Position resolvePosition(String positionName) {
        return positionRepository.findByName(positionName)
                .orElseThrow(() -> new IllegalStateException("Seed position not found: " + positionName));
    }

    private List<TechStack> resolveTechStacks(List<String> techStackNames) {
        return techStackNames.stream()
                .map(techStackName -> techStackRepository.findByName(techStackName)
                        .orElseThrow(() -> new IllegalStateException("Seed tech stack not found: " + techStackName)))
                .toList();
    }

    private com.example.meetball.domain.project.entity.ProjectRecruitPosition findRecruitPosition(Project project, String positionName) {
        return project.getPositionSelections().stream()
                .filter(position -> positionName.equals(position.getPositionName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed project position not found: " + positionName));
    }
}
