package com.example.meetball.domain.catalog.repository;

import com.example.meetball.domain.catalog.entity.CatalogTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CatalogTechStackRepository extends JpaRepository<CatalogTechStack, Long> {

    Optional<CatalogTechStack> findByName(String name);

    List<CatalogTechStack> findAllByActiveTrueOrderBySortOrderAscIdAsc();
}
