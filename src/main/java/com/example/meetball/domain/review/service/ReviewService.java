package com.example.meetball.domain.review.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectParticipantRepository;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.review.dto.ReviewRequestDto;
import com.example.meetball.domain.review.dto.ReviewSummaryDto;
import com.example.meetball.domain.review.dto.ReviewTargetResponse;
import com.example.meetball.domain.review.dto.PeerReviewResponse;
import com.example.meetball.domain.review.dto.ProjectReviewResponse;
import com.example.meetball.domain.review.entity.ProjectReview;
import com.example.meetball.domain.review.entity.PeerReview;
import com.example.meetball.domain.review.repository.ProjectReviewRepository;
import com.example.meetball.domain.review.repository.PeerReviewRepository;
import com.example.meetball.domain.profile.entity.Profile;
import com.example.meetball.domain.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PeerReviewRepository peerReviewRepository;
    private final ProjectReviewRepository projectReviewRepository;
    private final ProjectRepository projectRepository;
    private final ProfileRepository profileRepository;
    private final ProjectParticipantRepository projectParticipantRepository;

    @Transactional(readOnly = true)
    public ReviewSummaryDto getProjectReviewSummary(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        List<ProjectReview> reviews = projectReviewRepository.findByProject(project);

        if (reviews.isEmpty()) {
            return new ReviewSummaryDto(0.0, 0, new HashMap<>());
        }

        double totalScore = 0;
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) counts.put(i, 0);

        for (ProjectReview r : reviews) {
            totalScore += r.getScore();
            int roundedScore = (int) Math.round(r.getScore());
            if (roundedScore < 1) roundedScore = 1;
            counts.put(roundedScore, counts.get(roundedScore) + 1);
        }

        double average = Math.round((totalScore / reviews.size()) * 10.0) / 10.0;

        Map<Integer, Integer> percentages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            int count = counts.get(i);
            int percentage = (int) Math.round((double) count / reviews.size() * 100);
            percentages.put(i, percentage);
        }

        return new ReviewSummaryDto(average, reviews.size(), percentages);
    }

    @Transactional(readOnly = true)
    public List<ProjectReviewResponse> getProjectReviews(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        return projectReviewRepository.findByProject(project).stream()
                .map(ProjectReviewResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PeerReviewResponse> getReceivedPeerReviews(Profile reviewee) {
        return peerReviewRepository.findByRevieweeOrderByCreatedAtDesc(reviewee).stream()
                .map(PeerReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 유저의 밋볼 지수(매너 온도)를 계산합니다.
     * 기본 점수 36.5점에서 시작하며, 받은 피어 리뷰의 별점에 따라 가감됩니다.
     * 공식: 36.5 + (각 리뷰 별점 - 3.0) * 0.1
     */
    @Transactional(readOnly = true)
    public double calculateMeetBallIndex(Profile profile) {
        List<PeerReview> reviews = peerReviewRepository.findByRevieweeOrderByCreatedAtDesc(profile);
        double baseIndex = 36.5;

        if (reviews.isEmpty()) {
            return baseIndex;
        }

        double totalAdjustment = reviews.stream()
                .mapToDouble(review -> (review.getScore() - 3.0) * 0.1)
                .sum();

        double finalIndex = baseIndex + totalAdjustment;
        
        // 소수점 첫째 자리까지 반올림
        return Math.round(finalIndex * 10.0) / 10.0;
    }

    @Transactional
    public void addReview(Long projectId, Long reviewerId, ReviewRequestDto requestDto) {
        Long revieweeId = null;
        if (requestDto.getTargetProfileNickname() != null && !requestDto.getTargetProfileNickname().isBlank()) {
            revieweeId = profileRepository.findByNickname(requestDto.getTargetProfileNickname())
                    .orElseThrow(() -> new IllegalArgumentException("리뷰 대상자를 찾을 수 없습니다: " + requestDto.getTargetProfileNickname()))
                    .getId();
        }

        addReview(projectId, reviewerId, revieweeId, requestDto.getContent(), requestDto.getScore());
    }

    @Transactional(readOnly = true)
    public List<ReviewTargetResponse> getTeammatesForReview(Long projectId, Long currentProfileId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        Profile currentProfile = profileRepository.findById(currentProfileId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!project.isCompleted()) {
            throw new IllegalArgumentException("완료된 프로젝트에서만 팀원 리뷰를 작성할 수 있습니다.");
        }

        if (!projectParticipantRepository.existsByProjectAndProfile(project, currentProfile)) {
            throw new IllegalArgumentException("이 프로젝트의 참여 멤버만 팀원 목록을 조회할 수 있습니다.");
        }

        List<Profile> alreadyReviewed = peerReviewRepository.findByProjectAndReviewer(project, currentProfile).stream()
                .map(PeerReview::getReviewee)
                .toList();

        return projectParticipantRepository.findByProject(project).stream()
                .map(pm -> pm.getProfile())
                .filter(memberProfile -> memberProfile != null && !memberProfile.getId().equals(currentProfile.getId()))
                .filter(memberProfile -> !alreadyReviewed.contains(memberProfile))
                .map(memberProfile -> ReviewTargetResponse.builder()
                        .profileId(memberProfile.getId())
                        .nickname(memberProfile.getNickname())
                        .position(memberProfile.getPosition())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void addReview(Long projectId, Long reviewerId, Long revieweeId, String content, double score) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        Profile reviewer = profileRepository.findById(reviewerId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        if (!project.isCompleted()) {
            throw new IllegalArgumentException("완료된 프로젝트에만 리뷰를 작성할 수 있습니다.");
        }
        
        // 추가된 권한 체크: 작성자가 프로젝트 멤버인지 확인
        if (!projectParticipantRepository.existsByProjectAndProfile(project, reviewer)) {
            throw new IllegalArgumentException("이 프로젝트의 참여 멤버만 리뷰를 작성할 수 있습니다.");
        }

        Profile reviewee = revieweeId != null ? profileRepository.findById(revieweeId).orElse(null) : null;

        if (reviewee != null && reviewer.getId().equals(reviewee.getId())) {
            throw new IllegalArgumentException("자신에 대한 리뷰는 작성할 수 없습니다.");
        }

        // 피어 리뷰인 경우 대상자도 멤버인지 확인
        if (reviewee != null && !projectParticipantRepository.existsByProjectAndProfile(project, reviewee)) {
            throw new IllegalArgumentException("리뷰 대상자가 프로젝트 참여 멤버가 아닙니다.");
        }

        if (score < 0.5 || score > 5.0) {
            throw new IllegalArgumentException("별점은 0.5점에서 5.0점 사이여야 합니다.");
        }

        if (reviewee == null) {
            if (projectReviewRepository.existsByProjectAndReviewer(project, reviewer)) {
                throw new IllegalArgumentException("이미 해당 프로젝트에 대해 리뷰를 제출하셨습니다.");
            }
            projectReviewRepository.save(new ProjectReview(project, reviewer, content, score));
            return;
        }
        
        // 중복 방지 추가
        if (peerReviewRepository.existsByProjectAndReviewerAndReviewee(project, reviewer, reviewee)) {
            throw new IllegalArgumentException("이미 해당 대상에 대해 리뷰를 제출하셨습니다.");
        }

        PeerReview review = PeerReview.builder()
                .project(project)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .content(content)
                .score(score)
                .build();

        peerReviewRepository.save(review);
    }
}
