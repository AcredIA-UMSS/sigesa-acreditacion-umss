package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataLevel2NodeRepository extends JpaRepository<Level2NodeJpaEntity, UUID> {

    List<Level2NodeJpaEntity> findByLevel1NodeIdOrderByOrderAsc(UUID level1Id);

    @Query("""
            SELECT l2 FROM Level2NodeJpaEntity l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.process proc
            WHERE l2.id = :level2Id
            """)
    Optional<Level2NodeJpaEntity> findWithProcessById(@Param("level2Id") UUID level2Id);

    Optional<Level2NodeJpaEntity> findByIdAndLevel1NodeId(UUID id, UUID level1Id);
}
