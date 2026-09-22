package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataLevel3NodeRepository extends JpaRepository<Level3NodeJpaEntity, UUID> {

    List<Level3NodeJpaEntity> findByLevel2NodeIdOrderByOrderAsc(UUID level2Id);

    Optional<Level3NodeJpaEntity> findByIdAndLevel2NodeId(UUID id, UUID level2Id);

    @Query("""
            SELECT l3 FROM Level3NodeJpaEntity l3
            JOIN FETCH l3.level2Node l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.process proc
            WHERE l3.id = :level3Id
            """)
    Optional<Level3NodeJpaEntity> findWithProcessById(@Param("level3Id") UUID level3Id);
}
