package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.PeerVisitJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataPeerVisitRepository extends JpaRepository<PeerVisitJpaEntity, UUID> {

    @Query("""
            SELECT v FROM PeerVisitJpaEntity v
            LEFT JOIN FETCH v.items
            WHERE v.processId = :processId
            """)
    Optional<PeerVisitJpaEntity> findByProcessIdWithItems(@Param("processId") UUID processId);
}
