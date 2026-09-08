package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataNormativeIndicatorRepository extends JpaRepository<NormativeIndicatorJpaEntity, UUID> {

    List<NormativeIndicatorJpaEntity> findByLevel3NodeIdOrderByOrderAsc(UUID level3Id);

    Optional<NormativeIndicatorJpaEntity> findByIdAndLevel3NodeId(UUID id, UUID level3Id);

    @Query("""
            SELECT i FROM NormativeIndicatorJpaEntity i
            JOIN FETCH i.level3Node l3
            JOIN FETCH l3.level2Node l2
            JOIN FETCH l2.level1Node l1
            JOIN FETCH l1.process proc
            WHERE i.id = :indicatorId
            """)
    Optional<NormativeIndicatorJpaEntity> findWithProcessById(@Param("indicatorId") UUID indicatorId);

    @Query("""
            SELECT COUNT(i) FROM NormativeIndicatorJpaEntity i
            JOIN i.level3Node l3
            JOIN l3.level2Node l2
            JOIN l2.level1Node l1
            WHERE l1.process.id = :processId
            """)
    long countByProcessId(@Param("processId") UUID processId);

    @Query("""
            SELECT i FROM NormativeIndicatorJpaEntity i
            JOIN FETCH i.level3Node l3
            JOIN l3.level2Node l2
            JOIN l2.level1Node l1
            WHERE l1.id = :level1Id
            """)
    List<NormativeIndicatorJpaEntity> findAllByLevel1Id(@Param("level1Id") UUID level1Id);
}
