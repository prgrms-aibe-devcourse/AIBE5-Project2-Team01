package com.example.meetball.global.config;

import com.example.meetball.domain.comment.entity.Comment;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.review.entity.Review;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.bookmark.entity.Bookmark;
import com.example.meetball.domain.bookmark.repository.BookmarkRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectMember;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.projectread.entity.ProjectRead;
import com.example.meetball.domain.projectread.repository.ProjectReadRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataInitializer implements CommandLineRunner {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectReadRepository projectReadRepository;
    private final BookmarkRepository bookmarkRepository;

    @Override
    public void run(String... args) throws Exception {

        // 0. 로컬/테스트 검증용 유저 데이터 생성
        User leader = ensureSeedUser("leader@meetball.com", "초코푸들", "프론트엔드", List.of("React", "TypeScript"), true, "LEADER");
        User member = ensureSeedUser("member@meetball.com", "성실한리트리버", "백엔드", List.of("Java", "Spring"), true, "MEMBER");
        User guest = ensureSeedUser("guest@meetball.com", "열정고양이", "디자이너", List.of("Figma"), false, "GUEST");
        User dev = ensureSeedUser("dev@meetball.com", "코딩하는비글", "풀스택", List.of("Nextjs", "Nodejs"), true, "MEMBER");

        // 0-1. 로컬/테스트 검증용 프로젝트 및 참여 멤버 데이터 생성
        if (projectRepository.count() == 0) {
            // 프로젝트 1: AI 헬스케어 (모집 중 - 메인 프로젝트)
            Project project1 = projectRepository.save(new Project(
                    "AI 기반 헬스케어 모바일 앱 개발",
                    "사용자의 건강 데이터를 분석하여 맞춤형 식단과 운동을 추천하는 AI 플랫폼입니다.",
                    "사용자의 건강 데이터를 분석하여 맞춤형 운동 및 식단을 추천하는 AI 헬스케어 앱 개발입니다. 운동 기록, 식단 관리, 수면 패턴 분석 등의 기능을 통해 사용자에게 건강한 라이프스타일을 지원하는 것이 목표입니다.\n\n[주요 기능]\n- 운동 기록 및 분석\n- AI 기반 맞춤 운동 추천\n- 식단 관리 및 영양 분석\n- 수면 패턴 트래킹\n- 건강 목표 설정 및 달성 현황",
                    "사이드 프로젝트",
                    "프론트엔드:2, 백엔드:1, 디자이너:1, 데이터/AI:1",
                    "초코푸들",
                    "Frontend Leader",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                    "https://picsum.photos/seed/healthcare/1200/600",
                    2,
                    5,
                    LocalDate.now().plusDays(15),
                    LocalDate.now().minusDays(2),
                    List.of("ReactNative", "Python")
            ));

            // 프로젝트 2: 반려견 케어 서비스 "멍멍 비서" (완료된 프로젝트 - 성과 확인용)
            Project project2 = projectRepository.save(new Project(
                    "반려견 케어 서비스 [멍멍 비서]",
                    "바쁜 주인들을 위해 반려견의 일정을 관리하고 산책 메이트를 매칭해주는 서비스입니다.",
                    "이미 성공적으로 런칭하여 1,000명의 유저를 확보한 프로젝트입니다. 반려견의 사료 급여 정보, 산책 시간, 예방 접종 일정을 관리하고 근처의 산책 친구를 찾아주는 소셜 기능을 포함하고 있습니다.",
                    "스타트업",
                    "매니저(PM):1, 프론트엔드:2, 백엔드:2",
                    "초코푸들",
                    "Project Manager",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                    "https://picsum.photos/seed/dog/1200/600",
                    5,
                    5,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(100),
                    List.of("Nextjs", "Spring")
            ));
            project2.update(project2.getTitle(), project2.getDescription(), project2.getProjectType(), "ONLINE", 5, LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), LocalDate.now().minusDays(90), LocalDate.now().minusDays(1), true, true, LocalDateTime.now());
            projectRepository.save(project2);

            // 프로젝트 3: 여행 일정 공유 플랫폼 "TripMate" (모집 중)
            Project project3 = projectRepository.save(new Project(
                    "여행 일정 공유 플랫폼 [TripMate]",
                    "전 세계 여행자들과 나만의 특별한 일정을 공유하고 동행을 구하는 소셜 플랫폼입니다.",
                    "복잡한 여행 계획을 세우는 것이 힘든 사람들을 위해, 검증된 여행자들의 일정을 가져와 내 입맛대로 수정하고 동행을 구할 수 있는 서비스입니다. 지도 API를 활용한 실시간 경로 계산 기능이 핵심입니다.",
                    "사이드 프로젝트",
                    "IOS:1, 안드로이드:2, 프론트엔드:1, 백엔드:2",
                    "성실한리트리버",
                    "Backend Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=member",
                    "https://picsum.photos/seed/travel/1200/600",
                    1,
                    6,
                    LocalDate.now().plusDays(20),
                    LocalDate.now().minusDays(5),
                    List.of("Flutter", "Firebase")
            ));

            // 프로젝트 4: 블록체인 기반 배지 시스템 (소규모 모집 중)
            Project project4 = projectRepository.save(new Project(
                    "블록체인 기반 교육 이력 배지 시스템",
                    "자신의 학습 이력을 블록체인 배지로 인증하고 커리어를 관리하는 투명한 시스템입니다.",
                    "수료증이나 증명서를 위조 없이 안전하게 보관하고, 기업이 신뢰할 수 있는 데이터를 제공하는 플랫폼입니다. NFT 기술을 활용하여 개인의 성취를 시각화합니다.",
                    "기업 연계",
                    "프론트엔드:1, 백엔드:1, 데이터/AI:1, 기타:1",
                    "코딩하는비글",
                    "Fullstack Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=dev",
                    "https://picsum.photos/seed/blockchain/1200/600",
                    3,
                    4,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().minusDays(1),
                    List.of("Go", "React")
            ));

            // 참여 데이터
            projectMemberRepository.save(ProjectMember.builder().user(leader).project(project1).role("LEADER").build());
            projectMemberRepository.save(ProjectMember.builder().user(member).project(project1).role("MEMBER").build());
            projectMemberRepository.save(ProjectMember.builder().user(leader).project(project2).role("LEADER").build());
            projectMemberRepository.save(ProjectMember.builder().user(dev).project(project2).role("MEMBER").build());
            projectMemberRepository.save(ProjectMember.builder().user(member).project(project3).role("LEADER").build());
            projectMemberRepository.save(ProjectMember.builder().user(dev).project(project4).role("LEADER").build());

            // 지원 데이터
            applicationRepository.save(Application.builder()
                    .user(guest)
                    .project(project1)
                    .applicantName(guest.getNickname())
                    .position("디자이너")
                    .status(ApplicationStatus.PENDING)
                    .message("사용자 경험 설계와 화면 플로우 정리에 기여하고 싶습니다!")
                    .build());

            applicationRepository.save(Application.builder()
                    .user(leader)
                    .project(project3)
                    .applicantName(leader.getNickname())
                    .position("프론트엔드")
                    .status(ApplicationStatus.PENDING)
                    .message("여행 일정 공유 화면을 직관적으로 구현해보고 싶습니다!")
                    .build());

            // 찜 및 조회 기록
            bookmarkRepository.save(Bookmark.builder().user(guest).project(project1).build());
            bookmarkRepository.save(Bookmark.builder().user(guest).project(project3).build());
            bookmarkRepository.save(Bookmark.builder().user(leader).project(project2).build());
            projectReadRepository.save(ProjectRead.builder().user(guest).project(project1).build());
            projectReadRepository.save(ProjectRead.builder().user(guest).project(project2).build());
            projectReadRepository.save(ProjectRead.builder().user(leader).project(project1).build());
            projectReadRepository.save(ProjectRead.builder().user(leader).project(project2).build());

            // 리뷰 (성과 탭 확인용)
            reviewRepository.save(Review.builder()
                    .project(project2)
                    .reviewer(dev)
                    .reviewee(leader)
                    .content("리더님의 명확한 디렉션 덕분에 일정 내에 멋진 결과물을 낼 수 있었습니다!")
                    .score(5.0)
                    .build());

            reviewRepository.save(Review.builder()
                    .project(project2)
                    .reviewer(leader)
                    .reviewee(null)
                    .content("모든 팀원이 완벽하게 협업하여 앱스토어 1위를 달성한 기념비적인 프로젝트입니다.")
                    .score(5.0)
                    .build());

            // 댓글 (커뮤니케이션 탭 확인용)
            Comment c1 = commentRepository.save(Comment.builder()
                    .projectId(project1.getId())
                    .authorNickname("열정고양이")
                    .authorUserId(guest.getId())
                    .authorRole("GUEST")
                    .content("프론트엔드 포지션 지원할 때 React Native 경험이 꼭 필수인가요?")
                    .build());

            commentRepository.save(Comment.builder()
                    .projectId(project1.getId())
                    .authorNickname("초코푸들")
                    .authorUserId(leader.getId())
                    .authorRole("LEADER")
                    .content("필수는 아니지만, 관련 지식이 있으시면 훨씬 수월하게 적응하실 수 있습니다!")
                    .parent(c1)
                    .build());
        }

        ensureMinimumSeedProjects(leader, member, guest, dev);
    }

    private User ensureSeedUser(String email, String nickname, String jobTitle, List<String> techStacks, boolean isPublic, String role) {
        return userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .nickname(nickname)
                        .jobTitle(jobTitle)
                        .techStacks(techStacks)
                        .isPublic(isPublic)
                        .role(role)
                        .build()));
    }

    private void ensureMinimumSeedProjects(User leader, User member, User guest, User dev) {
        List<Project> existingProjects = new ArrayList<>(projectRepository.findAll());
        long projectCount = existingProjects.size();

        for (SeedProject seed : seedProjects()) {
            Project project = existingProjects.stream()
                    .filter(existing -> seed.title().equals(existing.getTitle()))
                    .findFirst()
                    .orElse(null);

            if (project == null && projectCount < 10) {
                User seedLeader = seed.leader().user(leader, member, guest, dev);
                project = projectRepository.save(new Project(
                        seed.title(),
                        seed.summary(),
                        seed.description(),
                        seed.projectType(),
                        seed.position(),
                        seedLeader.getNickname(),
                        seed.leaderRole(),
                        seed.leaderAvatarUrl(),
                        seed.thumbnailUrl(),
                        seed.currentRecruitment(),
                        seed.totalRecruitment(),
                        LocalDate.now().plusDays(seed.deadlineOffsetDays()),
                        LocalDate.now().minusDays(seed.createdOffsetDays()),
                        seed.techStacks()
                ));
                if (seed.completed()) {
                    project.update(project.getTitle(), project.getDescription(), project.getProjectType(), "ONLINE",
                            seed.totalRecruitment(), LocalDate.now().minusDays(100), LocalDate.now().minusDays(1),
                            LocalDate.now().minusDays(90), LocalDate.now().minusDays(1), true, true, LocalDateTime.now());
                    projectRepository.save(project);
                }
                existingProjects.add(project);
                projectCount++;
            }

            if (project != null) {
                ensureProjectMember(project, seed.leader().user(leader, member, guest, dev), "LEADER");
                for (SeedUserKey memberKey : seed.members()) {
                    ensureProjectMember(project, memberKey.user(leader, member, guest, dev), "MEMBER");
                }
                ensureSeedActivity(project, seed, leader, member, guest, dev);
            }
        }
    }

    private void ensureProjectMember(Project project, User user, String role) {
        if (user == null || projectMemberRepository.existsByProjectAndUser(project, user)) {
            return;
        }
        projectMemberRepository.save(ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .build());
    }

    private void ensureSeedActivity(Project project, SeedProject seed, User leader, User member, User guest, User dev) {
        for (SeedUserKey userKey : seed.bookmarks()) {
            ensureBookmark(project, userKey.user(leader, member, guest, dev));
        }
        for (SeedUserKey userKey : seed.reads()) {
            ensureRead(project, userKey.user(leader, member, guest, dev));
        }
        if (seed.applicant() != null && !applicationRepository.existsByProjectAndUser(project, seed.applicant().user(leader, member, guest, dev))) {
            User applicant = seed.applicant().user(leader, member, guest, dev);
            applicationRepository.save(Application.builder()
                    .user(applicant)
                    .project(project)
                    .applicantName(applicant.getNickname())
                    .position(seed.applicationPosition())
                    .status(ApplicationStatus.PENDING)
                    .message(seed.applicationMessage())
                    .build());
        }
    }

    private void ensureBookmark(Project project, User user) {
        if (user == null || bookmarkRepository.findByProjectAndUser(project, user).isPresent()) {
            return;
        }
        bookmarkRepository.save(Bookmark.builder().user(user).project(project).build());
    }

    private void ensureRead(Project project, User user) {
        if (user == null || projectReadRepository.findByProjectAndUser(project, user).isPresent()) {
            return;
        }
        projectReadRepository.save(ProjectRead.builder().user(user).project(project).build());
    }

    private List<SeedProject> seedProjects() {
        return List.of(
                new SeedProject(
                        "AI 기반 헬스케어 모바일 앱 개발",
                        "사용자의 건강 데이터를 분석하여 맞춤형 식단과 운동을 추천하는 AI 플랫폼입니다.",
                        "사용자의 건강 데이터를 분석하여 맞춤형 운동 및 식단을 추천하는 AI 헬스케어 앱 개발입니다. 운동 기록, 식단 관리, 수면 패턴 분석 등의 기능을 통해 사용자에게 건강한 라이프스타일을 지원하는 것이 목표입니다.",
                        "사이드 프로젝트",
                        "프론트엔드:2, 백엔드:1, 디자이너:1, 데이터/AI:1",
                        SeedUserKey.LEADER,
                        "Frontend Leader",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                        "https://picsum.photos/seed/healthcare/1200/600",
                        2,
                        5,
                        15,
                        2,
                        List.of("ReactNative", "Python"),
                        false,
                        List.of(SeedUserKey.MEMBER),
                        SeedUserKey.GUEST,
                        "디자이너",
                        "사용자 경험 설계와 화면 플로우 정리에 기여하고 싶습니다!",
                        List.of(SeedUserKey.GUEST),
                        List.of(SeedUserKey.GUEST, SeedUserKey.LEADER)
                ),
                new SeedProject(
                        "반려견 케어 서비스 [멍멍 비서]",
                        "바쁜 주인들을 위해 반려견의 일정을 관리하고 산책 메이트를 매칭해주는 서비스입니다.",
                        "이미 성공적으로 런칭하여 1,000명의 유저를 확보한 프로젝트입니다. 반려견의 사료 급여 정보, 산책 시간, 예방 접종 일정을 관리하고 근처의 산책 친구를 찾아주는 소셜 기능을 포함하고 있습니다.",
                        "스타트업",
                        "매니저(PM):1, 프론트엔드:2, 백엔드:2",
                        SeedUserKey.LEADER,
                        "Project Manager",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                        "https://picsum.photos/seed/dog/1200/600",
                        5,
                        5,
                        -1,
                        100,
                        List.of("Nextjs", "Spring"),
                        true,
                        List.of(SeedUserKey.DEV),
                        null,
                        null,
                        null,
                        List.of(SeedUserKey.LEADER),
                        List.of(SeedUserKey.GUEST, SeedUserKey.LEADER)
                ),
                new SeedProject(
                        "여행 일정 공유 플랫폼 [TripMate]",
                        "전 세계 여행자들과 나만의 특별한 일정을 공유하고 동행을 구하는 소셜 플랫폼입니다.",
                        "복잡한 여행 계획을 세우는 것이 힘든 사람들을 위해, 검증된 여행자들의 일정을 가져와 내 입맛대로 수정하고 동행을 구할 수 있는 서비스입니다. 지도 API를 활용한 실시간 경로 계산 기능이 핵심입니다.",
                        "사이드 프로젝트",
                        "IOS:1, 안드로이드:2, 프론트엔드:1, 백엔드:2",
                        SeedUserKey.MEMBER,
                        "Backend Developer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=member",
                        "https://picsum.photos/seed/travel/1200/600",
                        1,
                        6,
                        20,
                        5,
                        List.of("Flutter", "Firebase"),
                        false,
                        List.of(),
                        SeedUserKey.LEADER,
                        "프론트엔드",
                        "여행 일정 공유 화면을 직관적으로 구현해보고 싶습니다!",
                        List.of(SeedUserKey.GUEST),
                        List.of()
                ),
                new SeedProject(
                        "블록체인 기반 교육 이력 배지 시스템",
                        "자신의 학습 이력을 블록체인 배지로 인증하고 커리어를 관리하는 투명한 시스템입니다.",
                        "수료증이나 증명서를 위조 없이 안전하게 보관하고, 기업이 신뢰할 수 있는 데이터를 제공하는 플랫폼입니다. NFT 기술을 활용하여 개인의 성취를 시각화합니다.",
                        "기업 연계",
                        "프론트엔드:1, 백엔드:1, 데이터/AI:1, 기타:1",
                        SeedUserKey.DEV,
                        "Fullstack Developer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=dev",
                        "https://picsum.photos/seed/blockchain/1200/600",
                        3,
                        4,
                        5,
                        1,
                        List.of("Go", "React"),
                        false,
                        List.of(SeedUserKey.LEADER),
                        null,
                        null,
                        null,
                        List.of(SeedUserKey.MEMBER),
                        List.of(SeedUserKey.MEMBER)
                ),
                new SeedProject(
                        "온라인 스터디 매칭 플랫폼 [StudyMate]",
                        "관심 주제와 학습 시간을 기반으로 스터디 팀원을 추천하는 협업 플랫폼입니다.",
                        "부트캠프 수강생과 취업 준비생이 스터디 목표, 가능 시간, 선호 방식에 맞춰 팀을 만들고 진행률을 기록하는 서비스입니다. 출석 체크, 회고 노트, 과제 링크 공유 기능을 포함합니다.",
                        "사이드 프로젝트",
                        "기획자:1, 프론트엔드:2, 백엔드:1, 디자이너:1",
                        SeedUserKey.GUEST,
                        "Product Designer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=study",
                        "https://picsum.photos/seed/studymate/1200/600",
                        2,
                        5,
                        12,
                        3,
                        List.of("React", "Spring", "Figma"),
                        false,
                        List.of(SeedUserKey.LEADER, SeedUserKey.MEMBER),
                        SeedUserKey.DEV,
                        "백엔드",
                        "스터디 매칭 조건과 알림 API 설계에 참여하고 싶습니다.",
                        List.of(SeedUserKey.LEADER, SeedUserKey.DEV),
                        List.of(SeedUserKey.LEADER)
                ),
                new SeedProject(
                        "소상공인 예약/재고 관리 SaaS",
                        "동네 매장의 예약, 재고, 고객 알림을 한 화면에서 관리하는 경량 SaaS입니다.",
                        "소규모 공방과 1인 매장을 대상으로 예약 캘린더, 재고 알림, 단골 고객 쿠폰 발송을 제공하는 서비스입니다. 운영자가 모바일에서도 빠르게 확인할 수 있는 관리 화면을 목표로 합니다.",
                        "스타트업",
                        "매니저(PM):1, 백엔드:2, 프론트엔드:1, QA:1",
                        SeedUserKey.MEMBER,
                        "Backend Developer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=shop",
                        "https://picsum.photos/seed/localshop/1200/600",
                        1,
                        5,
                        18,
                        4,
                        List.of("Java", "Spring", "MySQL", "AWS"),
                        false,
                        List.of(SeedUserKey.DEV),
                        SeedUserKey.LEADER,
                        "프론트엔드",
                        "관리자 화면과 예약 캘린더 UI를 맡아보고 싶습니다.",
                        List.of(SeedUserKey.GUEST),
                        List.of(SeedUserKey.GUEST, SeedUserKey.DEV)
                ),
                new SeedProject(
                        "실시간 게임 파티 매칭 앱",
                        "플레이 스타일과 시간대를 기반으로 게임 파티원을 매칭하는 모바일 앱입니다.",
                        "협동 게임을 즐기는 사용자가 음성 채팅 선호도, 플레이 시간, 포지션을 기준으로 파티원을 찾을 수 있게 돕습니다. 실시간 대기방과 신고/평판 기능을 함께 설계합니다.",
                        "사이드 프로젝트",
                        "게임:2, 서버:1, 디자이너:1",
                        SeedUserKey.DEV,
                        "Fullstack Developer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=game",
                        "https://picsum.photos/seed/gameparty/1200/600",
                        0,
                        4,
                        25,
                        6,
                        List.of("Unity", "Firebase", "C"),
                        false,
                        List.of(SeedUserKey.GUEST),
                        SeedUserKey.MEMBER,
                        "서버",
                        "매칭 큐와 파티 상태 동기화 API를 구현해보고 싶습니다.",
                        List.of(SeedUserKey.MEMBER),
                        List.of(SeedUserKey.MEMBER)
                ),
                new SeedProject(
                        "탄소 발자국 가계부 [GreenLedger]",
                        "소비 내역을 기반으로 개인 탄소 배출량을 추정하고 절감 챌린지를 제안하는 앱입니다.",
                        "카테고리별 소비 데이터를 분석해 월간 탄소 발자국을 시각화하고, 대중교통 이용이나 중고 거래 같은 실천 챌린지를 추천합니다.",
                        "기업 연계",
                        "안드로이드:1, IOS:1, 백엔드:1, 데이터/AI:1",
                        SeedUserKey.LEADER,
                        "Frontend Leader",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=green",
                        "https://picsum.photos/seed/greenledger/1200/600",
                        2,
                        4,
                        9,
                        8,
                        List.of("Kotlin", "Swift", "Python", "Django"),
                        false,
                        List.of(SeedUserKey.MEMBER),
                        SeedUserKey.GUEST,
                        "데이터/AI",
                        "소비 카테고리와 탄소 계수 매핑 모델을 정리해보고 싶습니다.",
                        List.of(SeedUserKey.DEV),
                        List.of(SeedUserKey.DEV, SeedUserKey.GUEST)
                ),
                new SeedProject(
                        "클라우드 비용 모니터링 대시보드",
                        "팀별 클라우드 사용량과 비용 이상치를 한눈에 확인하는 운영 대시보드입니다.",
                        "AWS 리소스 사용량을 수집해 팀/서비스 단위 비용을 시각화하고, 예산 초과 가능성이 높을 때 Slack 알림을 보내는 도구입니다.",
                        "오픈소스",
                        "데브옵스:1, 백엔드:1, 프론트엔드:1, DBA:1",
                        SeedUserKey.MEMBER,
                        "Backend Developer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=cloud",
                        "https://picsum.photos/seed/cloudcost/1200/600",
                        2,
                        4,
                        30,
                        10,
                        List.of("AWS", "Docker", "Kubernetes", "Go"),
                        false,
                        List.of(SeedUserKey.DEV),
                        SeedUserKey.LEADER,
                        "프론트엔드",
                        "비용 차트와 필터 UI를 구현하는 데 기여하고 싶습니다.",
                        List.of(SeedUserKey.LEADER, SeedUserKey.GUEST),
                        List.of(SeedUserKey.LEADER)
                ),
                new SeedProject(
                        "크리에이터 포트폴리오 빌더",
                        "디자이너와 개발자가 작업물을 빠르게 공개 페이지로 구성하는 포트폴리오 빌더입니다.",
                        "템플릿을 선택하고 프로젝트 카드, 외부 링크, 팀 작업 이력을 조합해 개인 포트폴리오를 만들 수 있는 웹 서비스입니다. 공유 링크와 조회 분석 기능을 포함합니다.",
                        "사이드 프로젝트",
                        "프론트엔드:2, 디자이너:1, 마케터:1, 사업:1",
                        SeedUserKey.GUEST,
                        "Product Designer",
                        "https://api.dicebear.com/7.x/adventurer/svg?seed=portfolio",
                        "https://picsum.photos/seed/portfolio/1200/600",
                        3,
                        5,
                        14,
                        7,
                        List.of("Nextjs", "TypeScript", "Figma", "Jest"),
                        false,
                        List.of(SeedUserKey.LEADER, SeedUserKey.DEV),
                        SeedUserKey.MEMBER,
                        "마케터",
                        "런칭 페이지 카피와 사용자 인터뷰 설계를 도와보고 싶습니다.",
                        List.of(SeedUserKey.MEMBER),
                        List.of(SeedUserKey.MEMBER, SeedUserKey.DEV)
                )
        );
    }

    private enum SeedUserKey {
        LEADER,
        MEMBER,
        GUEST,
        DEV;

        private User user(User leader, User member, User guest, User dev) {
            return switch (this) {
                case LEADER -> leader;
                case MEMBER -> member;
                case GUEST -> guest;
                case DEV -> dev;
            };
        }
    }

    private record SeedProject(
            String title,
            String summary,
            String description,
            String projectType,
            String position,
            SeedUserKey leader,
            String leaderRole,
            String leaderAvatarUrl,
            String thumbnailUrl,
            Integer currentRecruitment,
            Integer totalRecruitment,
            int deadlineOffsetDays,
            int createdOffsetDays,
            List<String> techStacks,
            boolean completed,
            List<SeedUserKey> members,
            SeedUserKey applicant,
            String applicationPosition,
            String applicationMessage,
            List<SeedUserKey> bookmarks,
            List<SeedUserKey> reads
    ) {
    }
}
