package com.example.meetball.domain.application.repository;

import com.example.meetball.domain.application.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByProjectId(Long projectId);
    boolean existsByProjectIdAndApplicantName(Long projectId, String applicantName);
}
