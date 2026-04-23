package com.example.meetball.domain.review.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.review.entity.Review;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 프로젝트 자체에 대한 리뷰만 조회 (대상자가 없는 경우)
    List<Review> findByProjectAndRevieweeIsNull(Project project);

    // 특정 유저에게 달린 피어 리뷰 조회 (마이페이지용)
    List<Review> findByRevieweeOrderByCreatedAtDesc(User reviewee);

    // 중복 리뷰 방지용: 특정 프로젝트에서 작성자가 특정 대상(또는 프로젝트 전체)에게 남긴 리뷰가 있는지 확인
    boolean existsByProjectAndReviewerAndReviewee(Project project, User reviewer, User reviewee);

    // 추가된 메서드: 작성자가 해당 프로젝트에 대해 어떤 리뷰라도 남긴 적이 있는지 확인
    boolean existsByProjectAndReviewer(Project project, User reviewer);

    void deleteByProject(Project project);
}
