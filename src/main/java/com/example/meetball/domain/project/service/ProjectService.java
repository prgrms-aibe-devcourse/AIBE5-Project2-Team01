package com.example.meetball.domain.project.service;

import com.example.meetball.domain.project.dto.ParticipatedProjectResponse;
import com.example.meetball.domain.project.repository.ProjectMemberRepository;
import com.example.meetball.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectMemberRepository projectMemberRepository;

    @Transactional(readOnly = true)
    public List<ParticipatedProjectResponse> getParticipatedProjects(User user) {
        return projectMemberRepository.findByUser(user).stream()
                .map(pm -> ParticipatedProjectResponse.of(pm.getProject(), pm.getRole()))
                .collect(Collectors.toList());
    }
}
