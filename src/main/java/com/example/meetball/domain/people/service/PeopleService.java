package com.example.meetball.domain.people.service;

import com.example.meetball.domain.people.dto.PeopleProfileResponse;
import com.example.meetball.domain.people.dto.PeopleProjectResponse;
import com.example.meetball.domain.project.service.ProjectService;
import com.example.meetball.domain.review.service.ReviewService;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PeopleService {

    private final UserService userService;
    private final ProjectService projectService;
    private final ReviewService reviewService;

    @Transactional(readOnly = true)
    public PeopleProfileResponse getProfile(Long profileUserId, Long viewerId) {
        requireMemberViewer(viewerId);
        User profileUser = userService.getUserById(profileUserId);
        double meetBallIndex = reviewService.calculateMeetBallIndex(profileUser);
        return PeopleProfileResponse.from(profileUser, meetBallIndex);
    }

    @Transactional(readOnly = true)
    public List<PeopleProjectResponse> getProjects(Long profileUserId, Long viewerId) {
        requireMemberViewer(viewerId);
        User profileUser = userService.getUserById(profileUserId);
        return projectService.getParticipatedProjects(profileUser).stream()
                .map(PeopleProjectResponse::from)
                .toList();
    }

    private void requireMemberViewer(Long viewerId) {
        if (viewerId == null) {
            throw new IllegalArgumentException("로그인이 필요합니다.");
        }
        userService.getUserById(viewerId);
    }
}
