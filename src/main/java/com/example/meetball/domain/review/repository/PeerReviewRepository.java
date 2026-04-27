package com.example.meetball.domain.review.repository;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.review.entity.PeerReview;
import com.example.meetball.domain.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PeerReviewRepository extends JpaRepository<PeerReview, Long> {
    // 특정 유저에게 달린 피어 리뷰 조회 (마이페이지용)
    List<PeerReview> findByRevieweeOrderByCreatedAtDesc(Profile reviewee);

    // 중복 리뷰 방지용: 특정 프로젝트에서 작성자가 특정 대상(또는 프로젝트 전체)에게 남긴 리뷰가 있는지 확인
    boolean existsByProjectAndReviewerAndReviewee(Project project, Profile reviewer, Profile reviewee);
    List<PeerReview> findByProjectAndReviewer(Project project, Profile reviewer);
    long countByProjectAndReviewer(Project project, Profile reviewer);

    void deleteByProject(Project project);
}
