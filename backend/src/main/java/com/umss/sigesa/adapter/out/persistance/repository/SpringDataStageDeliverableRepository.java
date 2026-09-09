package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.StageDeliverableJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataStageDeliverableRepository extends JpaRepository<StageDeliverableJpaEntity, UUID> {

    @Query("""
            SELECT d FROM StageDeliverableJpaEntity d
            JOIN FETCH d.stage
            WHERE d.id = :id
            """)
    Optional<StageDeliverableJpaEntity> findWithStageById(@Param("id") UUID id);
}
