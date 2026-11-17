package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.MethodologicalStageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataMethodologicalStageRepository extends JpaRepository<MethodologicalStageJpaEntity, UUID> {

    @Query("""
            SELECT DISTINCT s FROM MethodologicalStageJpaEntity s
            LEFT JOIN FETCH s.deliverables
            JOIN FETCH s.process
            WHERE s.process.id = :processId
            ORDER BY s.order ASC
            """)
    List<MethodologicalStageJpaEntity> findByProcessIdOrderByOrderAsc(@Param("processId") UUID processId);

    @Query("""
            SELECT DISTINCT s FROM MethodologicalStageJpaEntity s
            LEFT JOIN FETCH s.deliverables
            JOIN FETCH s.process
            WHERE s.id = :id AND s.process.id = :processId
            """)
    Optional<MethodologicalStageJpaEntity> findByIdAndProcessId(@Param("id") UUID id, @Param("processId") UUID processId);

    @Query("""
            SELECT DISTINCT s FROM MethodologicalStageJpaEntity s
            LEFT JOIN FETCH s.deliverables
            JOIN FETCH s.process
            WHERE s.process.id = :processId AND s.order = :order
            """)
    Optional<MethodologicalStageJpaEntity> findByProcessIdAndOrder(
            @Param("processId") UUID processId,
            @Param("order") Integer order);

    boolean existsByProcessId(UUID processId);
}
