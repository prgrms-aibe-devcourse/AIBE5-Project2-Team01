package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.entity.ProjectStatus;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.review.repository.ReviewRepository;
import com.example.meetball.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getParticipatedProjects(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .map(pm -> {
                    Project project = pm.getProject();
                    
                    // 1. dDay 계산
                    Long dDay = null;
                    if (project.getRecruitmentDeadline() != null) {
                        dDay = ChronoUnit.DAYS.between(LocalDate.now(), project.getRecruitmentDeadline());
                    }
                    
                    // 2. 리뷰 가능 여부 계산 (마감 상태 && 내가 작성한 리뷰가 없음)
                    boolean alreadyReviewed = reviewRepository.existsByProjectAndReviewer(project, user);
                    boolean canReview = (project.getStatus() == ProjectStatus.COMPLETED) && !alreadyReviewed;
                    
                    return ParticipatedProjectResponse.of(project, pm.getRole(), canReview, dDay);
                })
                .collect(Collectors.toList());
    }
}
