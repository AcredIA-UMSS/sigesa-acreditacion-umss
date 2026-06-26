package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.EvidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EvidenceJpaRepository extends JpaRepository<EvidenceEntity, UUID> {

    boolean existsByIndicatorId(UUID indicatorId);
}
