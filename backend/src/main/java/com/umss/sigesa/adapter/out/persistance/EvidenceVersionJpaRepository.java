package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceVersionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EvidenceVersionJpaRepository extends JpaRepository<EvidenceVersionEntity, UUID> {

    List<EvidenceVersionEntity> findByEvidenceIdOrderByVersionNumberAsc(UUID evidenceId);
}
