package com.example.meetball.domain.application.service;

import com.example.meetball.domain.application.dto.AppliedProjectResponse;
import com.example.meetball.domain.application.repository.ApplicationRepository;
import com.example.meetball.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Transactional(readOnly = true)
    public List<AppliedProjectResponse> getAppliedProjects(User user) {
        return applicationRepository.findByUser(user).stream()
                .map(AppliedProjectResponse::from)
                .collect(Collectors.toList());
    }
}
