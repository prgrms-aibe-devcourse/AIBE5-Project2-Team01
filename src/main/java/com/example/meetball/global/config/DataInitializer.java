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
                    .email("leader@test.com")
                    .nickname("팀장님")
                    .jobTitle("프론트엔드 개발자")
                    .techStack("React, TypeScript")
                    .isPublic(true)
                    .role("LEADER")
                    .build());

            userRepository.save(User.builder()
                    .email("member@test.com")
                    .nickname("성실팀원")
                    .jobTitle("백엔드 개발자")
                    .techStack("Java, Spring Boot")
                    .isPublic(true)
                    .role("MEMBER")
                    .build());

            userRepository.save(User.builder()
                    .email("guest@test.com")
                    .nickname("익명게스트")
                    .jobTitle("취업준비생")
                    .techStack("Python")
                    .isPublic(false)
                    .role("GUEST")
                    .build());
        }

        // 0-1. 테스트용 프로젝트 및 참여 멤버 데이터 생성
        if (projectRepository.count() == 0) {
            // 프로젝트 1: AI 헬스케어 (마감된 프로젝트)
            Project project1 = projectRepository.save(new Project(
                    "AI 헬스케어 모바일 앱",
                    "개인 건강 관리를 위한 AI 플랫폼",
                    "사용자 건강 데이터를 AI로 분석하여 맞춤 운동과 식단을 추천하는 서비스",
                    "사이드 프로젝트",
                    "백엔드",
                    "팀장님",
                    "Frontend Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=leader",
                    "https://picsum.photos/seed/health/800/400",
                    3,
                    5,
                    LocalDate.now().minusDays(30), // 30일 전 마감
                    LocalDate.now().minusDays(60),
                    "Java, Spring, Flutter"
            ));

            // 프로젝트 2: 블록체인 투표 (모집 중)
            Project project2 = projectRepository.save(new Project(
                    "블록체인 기반 투표 시스템",
                    "투명한 투표를 위한 블록체인 플랫폼",
                    "블록체인 기술로 투명하고 안전한 온라인 투표 시스템을 만들어봅니다.",
                    "스타트업",
                    "프론트엔드",
                    "성실팀원",
                    "Backend Developer",
                    "https://api.dicebear.com/7.x/adventurer/svg?seed=member",
                    "https://picsum.photos/seed/vote/800/400",
                    2,
                    4,
                    LocalDate.now().plusDays(7), // D-7
                    LocalDate.now().minusDays(5),
                    "React, Solidity, Go"
            ));

            // 참여 정보 연결 (1번 유저: 팀장님, 2번 유저: 성실팀원)
            User leader = userRepository.findByEmail("leader@test.com").get();
            User member = userRepository.findByEmail("member@test.com").get();

            // 유저 1은 프로젝트 1의 팀장
            projectMemberRepository.save(ProjectMember.builder().user(leader).project(project1).role("LEADER").build());
            // 유저 2는 프로젝트 1의 팀원
            projectMemberRepository.save(ProjectMember.builder().user(member).project(project1).role("MEMBER").build());
            // 유저 2는 프로젝트 2의 팀장
            projectMemberRepository.save(ProjectMember.builder().user(member).project(project2).role("LEADER").build());

            // --- 추가: 지원 정보 샘플 ---
            User guest = userRepository.findByEmail("guest@test.com").get();
            applicationRepository.save(Application.builder()
                    .user(guest)
                    .project(project1)
                    .position("백엔드 개발자")
                    .status(ApplicationStatus.PENDING)
                    .message("열심히 도와드리고 싶습니다!")
                    .build());

            // --- 팀장님(ID: 1)도 프로젝트 2에 지원한 상황 추가 ---
            applicationRepository.save(Application.builder()
                    .user(leader)
                    .project(project2)
                    .position("프론트엔드 개발자")
                    .status(ApplicationStatus.PENDING)
                    .message("디자인 감각이 뛰어난 프론트엔드입니다!")
                    .build());

            // --- 추가: 조회 기록 샘플 ---
            projectReadRepository.save(ProjectRead.builder().user(leader).project(project1).build());
            projectReadRepository.save(ProjectRead.builder().user(leader).project(project2).build());

            // --- 추가: 찜 기록 샘플 ---
            bookmarkRepository.save(Bookmark.builder().user(leader).project(project2).build());

            // --- 추가: 리뷰 샘플 (피어 리뷰) ---
            User member1 = userRepository.save(User.builder()
                    .nickname("열정개발자")
                    .email("dev@test.com")
                    .jobTitle("백엔드 개발자")
                    .techStack("Java, Spring Boot")
                    .role("MEMBER")
                    .isPublic(true)
                    .build());

            // 열정개발자가 팀장님에게 남긴 피어 리뷰
            reviewRepository.save(Review.builder()
                    .project(project1)
                    .reviewer(member1)
                    .reviewee(leader)
                    .content("팀장님 리더십 덕분에 프로젝트 마무리 잘 할 수 있었습니다!")
                    .score(5.0)
                    .build());

            // 팀장님이 프로젝트에 대해 남긴 총평 리뷰 (Project Review)
            reviewRepository.save(Review.builder()
                    .project(project1)
                    .reviewer(leader)
                    .reviewee(null) // 특정인 대상이 아닌 프로젝트 대상
                    .content("좋은 협업 경험이었습니다.")
                    .score(4.5)
                    .build());
        }

        // 1. 댓글 더미 데이터 생성
        if (commentRepository.count() == 0) {
            // 1. 팀장 댓글
            Comment leaderComment = Comment.builder()
                    .projectId(1L)
                    .authorNickname("팀장님")
                    .authorRole("LEADER")
                    .content("안녕하세요! 이번 프로젝트 팀장입니다. 궁금한 점은 언제든 댓글로 남겨주세요!")
                    .build();
            commentRepository.save(leaderComment);

            // 2. 팀원 댓글 (답글 형식)
            Comment memberReply = Comment.builder()
                    .projectId(1L)
                    .authorNickname("성실팀원")
                    .authorRole("MEMBER")
                    .content("팀장님 반갑습니다! 열심히 참여하겠습니다.")
                    .parent(leaderComment)
                    .build();
            commentRepository.save(memberReply);

            // 3. 일반 사용자(게스트) 댓글
            Comment guestComment = Comment.builder()
                    .projectId(1L)
                    .authorNickname("익명게스트")
                    .authorRole("GUEST")
                    .content("이 프로젝트 기획이 정말 좋네요! 지원해보고 싶습니다.")
                    .build();
            commentRepository.save(guestComment);
        }
    }
}
