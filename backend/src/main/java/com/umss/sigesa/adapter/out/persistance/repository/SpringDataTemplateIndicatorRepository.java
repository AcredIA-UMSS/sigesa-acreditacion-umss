package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateIndicatorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTemplateIndicatorRepository extends JpaRepository<TemplateIndicatorJpaEntity, UUID> {

    List<TemplateIndicatorJpaEntity> findByLevel3NodeIdOrderByOrderAsc(UUID templateLevel3Id);

    Optional<TemplateIndicatorJpaEntity> findByIdAndLevel3NodeId(UUID id, UUID templateLevel3Id);

    @Query("""
            SELECT ti FROM TemplateIndicatorJpaEntity ti
            JOIN FETCH ti.level3Node l3
            JOIN FETCH l3.level2Node l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.template t
            WHERE ti.id = :indicatorId
            """)
    Optional<TemplateIndicatorJpaEntity> findWithTemplateById(@Param("indicatorId") UUID indicatorId);

    @Query("""
            SELECT ti FROM TemplateIndicatorJpaEntity ti
            JOIN FETCH ti.level3Node l3
            JOIN l3.level2Node l2
            JOIN l2.level1Node l1
            WHERE l1.id = :level1Id
            """)
    List<TemplateIndicatorJpaEntity> findAllByLevel1Id(@Param("level1Id") UUID level1Id);

    @Query("""
            SELECT COUNT(ti) FROM TemplateIndicatorJpaEntity ti
            JOIN ti.level3Node l3
            JOIN l3.level2Node l2
            JOIN l2.level1Node l1
            WHERE l1.template.id = :templateId
            """)
    long countByTemplateId(@Param("templateId") UUID templateId);
}
