package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataIndicatorEvidenceLinkRepository extends JpaRepository<IndicatorEvidenceLinkEntity, UUID> {

    List<IndicatorEvidenceLinkEntity> findByDocumentAssetId(UUID documentAssetId);

    Optional<IndicatorEvidenceLinkEntity> findFirstByEvidenceId(UUID evidenceId);

    Optional<IndicatorEvidenceLinkEntity> findFirstByIndicatorId(UUID indicatorId);
}
