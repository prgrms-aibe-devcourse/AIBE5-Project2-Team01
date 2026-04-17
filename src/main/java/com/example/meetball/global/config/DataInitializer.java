package com.example.meetball.global.config;

import com.example.meetball.domain.comment.entity.Comment;
import com.example.meetball.domain.comment.repository.CommentRepository;
import com.example.meetball.domain.review.entity.Review;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CommentRepository commentRepository;
    private final ReviewRepository reviewRepository;
    private final ProjectRepository projectRepository;

    @Override
    public void run(String... args) throws Exception {
        // 앱 시작 시 프로젝트 테스트용 데이터가 하나도 없을 때만 샘플을 생성합니다.
        if (projectRepository.count() == 0) {
            Project p1 = new Project(
                    "백엔드 스프링부트 스터디 모집",
                    "스프링부트 딥다이브 스터디입니다. 주 1회 온라인 모임.",
                    "STUDY",
                    "ONLINE",
                    4,
                    LocalDate.now().minusDays(5),
                    LocalDate.now().plusDays(10),
                    LocalDate.now().plusDays(15),
                    LocalDate.now().plusMonths(3),
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            Project p2 = new Project(
                    "사이드 프로젝트 앱 개발",
                    "안드로이드/iOS 앱 개발 사이드 프로젝트 팀원 구합니다.",
                    "SIDE",
                    "OFFLINE",
                    5,
                    LocalDate.now().minusDays(10),
                    LocalDate.now().minusDays(1),
                    LocalDate.now().plusDays(5),
                    LocalDate.now().plusMonths(4),
                    true,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            Project p3 = new Project(
                    "알고리즘 코딩테스트 스터디",
                    "코테 대비반 모집합니다. 매주 프로그래머스 레벨 2~3 문제 풀이",
                    "STUDY",
                    "ONLINE",
                    3,
                    LocalDate.now(),
                    LocalDate.now().plusDays(7),
                    LocalDate.now().plusDays(14),
                    LocalDate.now().plusMonths(2),
                    false,
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
            projectRepository.save(p1);
            projectRepository.save(p2);
            projectRepository.save(p3);
        }

        // 앱 시작 시 테스트용 데이터가 하나도 없을 때만 샘플을 생성합니다.
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

        // 2. 리뷰 더미 데이터 생성
        if (reviewRepository.count() == 0) {
            // 프로젝트(ID 1)에 대한 팀원(다수)의 리뷰 삽입 (4.8 평점을 유사하게 맞추기 위해 5점 8개, 4점 2개 생성)
            for (int i = 0; i < 8; i++) {
                reviewRepository.save(Review.builder()
                        .projectId(1L)
                        .reviewerNickname("평가자" + i)
                        .targetUserNickname("팀전체")
                        .score(5)
                        .build());
            }
            for (int i = 0; i < 2; i++) {
                reviewRepository.save(Review.builder()
                        .projectId(1L)
                        .reviewerNickname("조금깐깐한평가자" + i)
                        .targetUserNickname("팀전체")
                        .score(4)
                        .build());
            }
        }
    }
}
