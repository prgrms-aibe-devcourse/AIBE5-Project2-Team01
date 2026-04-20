package com.example.meetball.domain.application.repository;

import com.example.meetball.domain.application.entity.Application;
import com.example.meetball.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByUser(User user);
}
