package com.example.meetball.domain.projectread.service;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.projectread.dto.ReadProjectResponse;
import com.example.meetball.domain.projectread.entity.ProjectRead;
import com.example.meetball.domain.projectread.repository.ProjectReadRepository;
import com.example.meetball.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectReadService {

    private final ProjectReadRepository projectReadRepository;

    @Transactional
    public void recordRead(Project project, User user) {
        projectReadRepository.findByProjectAndUser(project, user)
                .ifPresentOrElse(
                        read -> {
                            // 이미 기록이 있으면 JPA Auditing에 의해 readAt이 자동 갱신되도록 dirty checking 유도
                            // @LastModifiedDate가 있으므로 save만 호출해도 갱신됨
                            projectReadRepository.save(read);
                        },
                        () -> {
                            ProjectRead newRead = ProjectRead.builder()
                                    .project(project)
                                    .user(user)
                                    .build();
                            projectReadRepository.save(newRead);
                        }
                );
    }

    @Transactional(readOnly = true)
    public List<ReadProjectResponse> getReadHistory(User user) {
        return projectReadRepository.findByUserOrderByReadAtDesc(user).stream()
                .map(ReadProjectResponse::from)
                .collect(Collectors.toList());
    }
}
