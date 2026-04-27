package com.example.meetball.domain.techstack.repository;

import com.example.meetball.domain.techstack.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TechStackRepository extends JpaRepository<TechStack, Long> {

    Optional<TechStack> findByName(String name);

    List<TechStack> findAllByOrderByIdAsc();
}
