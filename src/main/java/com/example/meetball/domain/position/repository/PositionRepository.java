package com.example.meetball.domain.position.repository;

import com.example.meetball.domain.position.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PositionRepository extends JpaRepository<Position, Long> {

    Optional<Position> findByName(String name);

    List<Position> findAllByOrderByIdAsc();
}
