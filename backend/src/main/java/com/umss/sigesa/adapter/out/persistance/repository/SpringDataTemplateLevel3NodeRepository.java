package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel3NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTemplateLevel3NodeRepository extends JpaRepository<TemplateLevel3NodeJpaEntity, UUID> {

    List<TemplateLevel3NodeJpaEntity> findByLevel2NodeIdOrderByOrderAsc(UUID templateLevel2Id);

    Optional<TemplateLevel3NodeJpaEntity> findByIdAndLevel2NodeId(UUID id, UUID templateLevel2Id);

    @Query("""
            SELECT l3 FROM TemplateLevel3NodeJpaEntity l3
            JOIN FETCH l3.level2Node l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.template t
            WHERE l3.id = :level3Id
            """)
    Optional<TemplateLevel3NodeJpaEntity> findWithTemplateById(@Param("level3Id") UUID level3Id);
}
