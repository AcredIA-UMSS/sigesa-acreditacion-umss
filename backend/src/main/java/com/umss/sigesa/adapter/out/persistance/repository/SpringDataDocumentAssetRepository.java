package com.umss.sigesa.adapter.out.persistance.repository;

import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SpringDataDocumentAssetRepository extends JpaRepository<DocumentAssetEntity, UUID> {

    Optional<DocumentAssetEntity> findByProcessIdAndContentHash(UUID processId, String contentHash);

    List<DocumentAssetEntity> findByProcessIdOrderByCreatedAtDesc(UUID processId);

    boolean existsByStorageKey(String storageKey);
}
