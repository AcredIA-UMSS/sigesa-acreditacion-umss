package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataLevel1NodeRepository extends JpaRepository<Level1NodeJpaEntity, UUID> {

    List<Level1NodeJpaEntity> findByProcessIdOrderByOrderAsc(UUID processId);

    Optional<Level1NodeJpaEntity> findByIdAndProcessId(UUID id, UUID processId);

    @Query("""
            SELECT l1 FROM Level1NodeJpaEntity l1
            JOIN FETCH l1.process proc
            WHERE l1.id = :level1Id
            """)
    Optional<Level1NodeJpaEntity> findWithProcessById(@Param("level1Id") UUID level1Id);

    boolean existsByProcessId(UUID processId);

    long countByProcessId(UUID processId);
}
