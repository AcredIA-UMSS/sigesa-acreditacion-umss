package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel1NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTemplateLevel1NodeRepository extends JpaRepository<TemplateLevel1NodeJpaEntity, UUID> {

    List<TemplateLevel1NodeJpaEntity> findByTemplateIdOrderByOrderAsc(UUID templateId);

    Optional<TemplateLevel1NodeJpaEntity> findByIdAndTemplateId(UUID id, UUID templateId);

    @Query("""
            SELECT l1 FROM TemplateLevel1NodeJpaEntity l1
            JOIN FETCH l1.template t
            WHERE l1.id = :level1Id
            """)
    Optional<TemplateLevel1NodeJpaEntity> findWithTemplateById(@Param("level1Id") UUID level1Id);

    boolean existsByTemplateId(UUID templateId);

    long countByTemplateId(UUID templateId);
}
