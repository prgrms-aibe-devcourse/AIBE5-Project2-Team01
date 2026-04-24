package com.example.meetball.domain.viewhistory.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.viewhistory.dto.ViewHistoryProjectResponse;
import com.example.meetball.domain.viewhistory.entity.ViewHistory;
import com.example.meetball.domain.viewhistory.repository.ViewHistoryRepository;
import com.example.meetball.domain.profile.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ViewHistoryService {

    private final ViewHistoryRepository viewHistoryRepository;

    @Transactional
    public void recordRead(Project project, Profile profile) {
        viewHistoryRepository.findByProjectAndProfile(project, profile)
                .ifPresentOrElse(
                        read -> {
                            // 이미 기록이 있으면 JPA Auditing에 의해 readAt이 자동 갱신되도록 dirty checking 유도
                            // @LastModifiedDate가 있으므로 save만 호출해도 갱신됨
                            viewHistoryRepository.save(read);
                        },
                        () -> {
                            ViewHistory newRead = ViewHistory.builder()
                                    .project(project)
                                    .profile(profile)
                                    .build();
                            viewHistoryRepository.save(newRead);
                        }
                );
    }

    @Transactional(readOnly = true)
    public List<ViewHistoryProjectResponse> getReadHistory(Profile profile) {
        return viewHistoryRepository.findByProfileOrderByReadAtDesc(profile).stream()
                .map(ViewHistoryProjectResponse::from)
                .collect(Collectors.toList());
    }
}
