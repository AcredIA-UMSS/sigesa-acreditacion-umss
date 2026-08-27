package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EvidenceVersionJpaRepository extends JpaRepository<EvidenceVersionEntity, UUID> {

    List<EvidenceVersionEntity> findByEvidenceIdOrderByVersionNumberDesc(UUID evidenceId);

    @Query("SELECT COALESCE(MAX(v.versionNumber), 0) FROM EvidenceVersionEntity v WHERE v.evidenceId = :evidenceId")
    int findMaxVersionNumber(@Param("evidenceId") UUID evidenceId);
}
