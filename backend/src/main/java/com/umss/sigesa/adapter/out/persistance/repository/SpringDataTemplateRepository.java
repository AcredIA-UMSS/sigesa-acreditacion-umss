package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTemplateRepository extends JpaRepository<TemplateJpaEntity, UUID> {

    @EntityGraph(attributePaths = {"phases"})
    Optional<TemplateJpaEntity> findWithPhasesById(UUID id);

    @EntityGraph(attributePaths = {"phases"})
    @Override
    List<TemplateJpaEntity> findAll();

    @EntityGraph(attributePaths = {"phases"})
    List<TemplateJpaEntity> findByStatus(String status);

    @EntityGraph(attributePaths = {"phases"})
    List<TemplateJpaEntity> findByStatusAndType(String status, String type);
}
