package com.example.meetball.domain.catalog.repository;

import com.example.meetball.domain.catalog.entity.CatalogPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogPositionRepository extends JpaRepository<CatalogPosition, Long> {

    Optional<CatalogPosition> findByName(String name);

    List<CatalogPosition> findAllByActiveTrueOrderBySortOrderAscIdAsc();
}
