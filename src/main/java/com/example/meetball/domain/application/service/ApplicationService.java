package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.ApplicationRequestDto;
import com.example.meetball.domain.application.dto.ApplicationResponseDto;
import com.example.meetball.domain.application.dto.ApplicationStatusUpdateRequestDto;
import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.application.entity.ApplicationStatus;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // =====================================================
    // 지원자용 기능 (우리 코드 방식 - userId 기반)
    // =====================================================

    /**
     * 프로젝트에 지원하기 (지원자가 사용)
     */
    @Transactional
    public ApplicationResponseDto createApplication(Long projectId, ApplicationRequestDto request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + request.getUserId()));

        // 중복 지원 방지
        if (applicationRepository.existsByProjectAndUser(project, user)) {
            throw new IllegalArgumentException("이미 지원한 프로젝트입니다.");
        }

        Application application = Application.builder()
                .user(user)
                .project(project)
                .position(request.getPosition())
                .message(request.getMessage())
                .status(ApplicationStatus.PENDING)
                .build();

        return new ApplicationResponseDto(applicationRepository.save(application));
    }

    /**
     * 내가 지원한 목록 조회 (마이페이지용)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyApplications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));
        return applicationRepository.findByUser(user).stream()
                .map(ApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    // =====================================================
    // 팀장용 기능 (mergefile 방식 - projectId 기반)
    // =====================================================

    /**
     * 특정 프로젝트의 지원자 목록 조회 (팀장이 사용)
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));
        return applicationRepository.findByProject(project).stream()
                .map(ApplicationResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * 지원 상태 변경 (팀장이 사용: PENDING → ACCEPTED / REJECTED)
     */
    @Transactional
    public ApplicationResponseDto updateApplicationStatus(Long applicationId, ApplicationStatusUpdateRequestDto request) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("지원 내역을 찾을 수 없습니다: " + applicationId));

        ApplicationStatus nextStatus;
        try {
            nextStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("올바르지 않은 상태값입니다: " + request.getStatus());
        }

        application.updateStatus(nextStatus, LocalDateTime.now());
        return new ApplicationResponseDto(applicationRepository.save(application));
    }
}
