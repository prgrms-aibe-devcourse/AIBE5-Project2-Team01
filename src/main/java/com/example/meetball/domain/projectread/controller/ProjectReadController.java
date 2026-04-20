package com.example.meetball.domain.projectread.controller;

import com.example.meetball.domain.project.entity.Project;
import com.example.meetball.domain.project.repository.ProjectRepository;
import com.example.meetball.domain.projectread.service.ProjectReadService;
import com.example.meetball.domain.user.entity.User;
import com.example.meetball.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects/{projectId}/read")
@RequiredArgsConstructor
public class ProjectReadController {

    private final ProjectReadService projectReadService;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Void> recordRead(
            @PathVariable Long projectId,
            @RequestParam Long userId) {
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        projectReadService.recordRead(project, user);
        return ResponseEntity.ok().build();
    }
}
