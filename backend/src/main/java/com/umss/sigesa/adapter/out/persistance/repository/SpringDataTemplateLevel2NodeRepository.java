package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel2NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataTemplateLevel2NodeRepository extends JpaRepository<TemplateLevel2NodeJpaEntity, UUID> {

    List<TemplateLevel2NodeJpaEntity> findByLevel1NodeIdOrderByOrderAsc(UUID templateLevel1Id);

    Optional<TemplateLevel2NodeJpaEntity> findByIdAndLevel1NodeId(UUID id, UUID templateLevel1Id);

    @Query("""
            SELECT l2 FROM TemplateLevel2NodeJpaEntity l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.template t
            WHERE l2.id = :level2Id
            """)
    Optional<TemplateLevel2NodeJpaEntity> findWithTemplateById(@Param("level2Id") UUID level2Id);
}
