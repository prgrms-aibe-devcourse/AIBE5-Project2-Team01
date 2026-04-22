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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
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

        // 0. 테스트용 유저 데이터 생성
        if (userRepository.count() == 0) {
            userRepository.save(User.builder()
                    .email("leader@meetball.com")
                    .nickname("초코푸들")
                    .jobTitle("프론트엔드 리더")
                    .techStack("React, TypeScript, TailwindCSS")
                    .isPublic(true)
                    .role("LEADER")
                    .build());

            userRepository.save(User.builder()
                    .email("member@meetball.com")
                    .nickname("성실한리트리버")
                    .jobTitle("백엔드 개발자")
                    .techStack("Java, Spring Boot, MySQL")
                    .isPublic(true)
                    .role("MEMBER")
                    .build());

            userRepository.save(User.builder()
                    .email("guest@meetball.com")
                    .nickname("열정고양이")
                    .jobTitle("UI/UX 디자이너")
                    .techStack("Figma, Adobe XD")
                    .isPublic(false)
                    .role("GUEST")
                    .build());
            
            userRepository.save(User.builder()
                    .email("dev@meetball.com")
                    .nickname("코딩하는비글")
                    .jobTitle("풀스택 개발자")
                    .techStack("Next.js, Node.js")
                    .isPublic(true)
                    .role("MEMBER")
                    .build());
        }

        // 0-1. 테스트용 프로젝트 및 참여 멤버 데이터 생성
        if (projectRepository.count() == 0) {
            // 프로젝트 1: AI 헬스케어 (모집 중 - 메인 프로젝트)
            Project project1 = projectRepository.save(new Project(
                    "AI 기반 헬스케어 모바일 앱 개발",
                    "사용자의 건강 데이터를 분석하여 맞춤형 식단과 운동을 추천하는 AI 플랫폼입니다.",
                    "사용자의 건강 데이터를 분석하여 맞춤형 운동 및 식단을 추천하는 AI 헬스케어 앱 개발입니다. 운동 기록, 식단 관리, 수면 패턴 분석 등의 기능을 통해 사용자에게 건강한 라이프스타일을 지원하는 것이 목표입니다.\n\n[주요 기능]\n- 운동 기록 및 분석\n- AI 기반 맞춤 운동 추천\n- 식단 관리 및 영양 분석\n- 수면 패턴 트래킹\n- 건강 목표 설정 및 달성 현황",
                    "사이드 프로젝트",
                    "프론트엔드, 백엔드, AI/ML",
                    "초코푸들",
                    "Frontend Leader",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                    "https://picsum.photos/seed/healthcare/1200/600",
                    2,
                    5,
                    LocalDate.now().plusDays(15),
                    LocalDate.now().minusDays(2),
                    "React Native, Python, TensorFlow, Flask"
            ));

            // 프로젝트 2: 반려견 케어 서비스 "멍멍 비서" (완료된 프로젝트 - 성과 확인용)
            Project project2 = projectRepository.save(new Project(
                    "반려견 케어 서비스 [멍멍 비서]",
                    "바쁜 주인들을 위해 반려견의 일정을 관리하고 산책 메이트를 매칭해주는 서비스입니다.",
                    "이미 성공적으로 런칭하여 1,000명의 유저를 확보한 프로젝트입니다. 반려견의 사료 급여 정보, 산책 시간, 예방 접종 일정을 관리하고 근처의 산책 친구를 찾아주는 소셜 기능을 포함하고 있습니다.",
                    "스타트업",
                    "전 분야",
                    "초코푸들",
                    "Project Manager",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=poodle",
                    "https://picsum.photos/seed/dog/1200/600",
                    5,
                    5,
                    LocalDate.now().minusDays(1),
                    LocalDate.now().minusDays(100),
                    "Next.js, Spring Boot, PostgreSQL, Redis"
            ));
            project2.update(project2.getTitle(), project2.getDescription(), project2.getProjectType(), "ONLINE", 5, LocalDate.now().minusDays(100), LocalDate.now().minusDays(1), LocalDate.now().minusDays(90), LocalDate.now().minusDays(1), true, LocalDateTime.now());

            // 프로젝트 3: 여행 일정 공유 플랫폼 "TripMate" (모집 중)
            Project project3 = projectRepository.save(new Project(
                    "여행 일정 공유 플랫폼 [TripMate]",
                    "전 세계 여행자들과 나만의 특별한 일정을 공유하고 동행을 구하는 소셜 플랫폼입니다.",
                    "복잡한 여행 계획을 세우는 것이 힘든 사람들을 위해, 검증된 여행자들의 일정을 가져와 내 입맛대로 수정하고 동행을 구할 수 있는 서비스입니다. 지도 API를 활용한 실시간 경로 계산 기능이 핵심입니다.",
                    "사이드 프로젝트",
                    "모바일 앱",
                    "성실한리트리버",
                    "Backend Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=member",
                    "https://picsum.photos/seed/travel/1200/600",
                    1,
                    6,
                    LocalDate.now().plusDays(20),
                    LocalDate.now().minusDays(5),
                    "Flutter, Firebase, Mapbox"
            ));

            // 프로젝트 4: 블록체인 기반 배지 시스템 (소규모 모집 중)
            Project project4 = projectRepository.save(new Project(
                    "블록체인 기반 교육 이력 배지 시스템",
                    "자신의 학습 이력을 블록체인 배지로 인증하고 커리어를 관리하는 투명한 시스템입니다.",
                    "수료증이나 증명서를 위조 없이 안전하게 보관하고, 기업이 신뢰할 수 있는 데이터를 제공하는 플랫폼입니다. NFT 기술을 활용하여 개인의 성취를 시각화합니다.",
                    "기업 연계",
                    "블록체인, 웹",
                    "코딩하는비글",
                    "Fullstack Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=dev",
                    "https://picsum.photos/seed/blockchain/1200/600",
                    3,
                    4,
                    LocalDate.now().plusDays(5),
                    LocalDate.now().minusDays(1),
                    "Solidity, Go, React, Ethers.js"
            ));

            // 사용자 연결
            User leader = userRepository.findByEmail("leader@meetball.com").get();
            User member = userRepository.findByEmail("member@meetball.com").get();
            User dev = userRepository.findByEmail("dev@meetball.com").get();
            User guest = userRepository.findByEmail("guest@meetball.com").get();

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
                    .position("UI/UX 디자이너")
                    .status(ApplicationStatus.PENDING)
                    .message("사용자 경험 설계와 화면 플로우 정리에 기여하고 싶습니다!")
                    .build());

            applicationRepository.save(Application.builder()
                    .user(leader)
                    .project(project3)
                    .applicantName(leader.getNickname())
                    .position("프론트엔드 개발자")
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
                    .authorRole("GUEST")
                    .content("프론트엔드 포지션 지원할 때 React Native 경험이 꼭 필수인가요?")
                    .build());

            commentRepository.save(Comment.builder()
                    .projectId(project1.getId())
                    .authorNickname("초코푸들")
                    .authorRole("LEADER")
                    .content("필수는 아니지만, 관련 지식이 있으시면 훨씬 수월하게 적응하실 수 있습니다!")
                    .parent(c1)
                    .build());
        }
    }
}
