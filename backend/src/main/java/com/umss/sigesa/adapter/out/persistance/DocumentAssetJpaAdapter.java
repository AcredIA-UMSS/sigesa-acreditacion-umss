package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.DocumentAssetEntity;
import com.umss.sigesa.adapter.out.persistance.entity.IndicatorEvidenceLinkEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataDocumentAssetRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorEvidenceLinkRepository;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class DocumentAssetJpaAdapter implements DocumentAssetPort {

    private final SpringDataDocumentAssetRepository assetRepository;
    private final SpringDataIndicatorEvidenceLinkRepository linkRepository;

    public DocumentAssetJpaAdapter(
            SpringDataDocumentAssetRepository assetRepository,
            SpringDataIndicatorEvidenceLinkRepository linkRepository) {
        this.assetRepository = assetRepository;
        this.linkRepository = linkRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentAsset> findById(UUID id) {
        return assetRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentAsset> findByHashInProcess(UUID processId, String contentHash) {
        return assetRepository.findByProcessIdAndContentHash(processId, contentHash).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentAsset> findByProcessId(UUID processId) {
        return assetRepository.findByProcessIdOrderByCreatedAtDesc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DocumentAsset> findByEvidenceId(UUID evidenceId) {
        return linkRepository.findFirstByEvidenceId(evidenceId)
                .flatMap(link -> assetRepository.findById(link.getDocumentAssetId()))
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public DocumentAsset save(DocumentAsset asset) {
        DocumentAssetEntity entity = toEntity(asset);
        return toDomain(assetRepository.save(entity));
    }

    @Override
    @Transactional
    public IndicatorEvidenceLink saveLink(IndicatorEvidenceLink link) {
        IndicatorEvidenceLinkEntity entity = toLinkEntity(link);
        IndicatorEvidenceLinkEntity saved = linkRepository.save(entity);
        return toLinkDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> findLinkedIndicatorIds(UUID documentAssetId) {
        return linkRepository.findByDocumentAssetId(documentAssetId).stream()
                .map(IndicatorEvidenceLinkEntity::getIndicatorId)
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.umss.sigesa.domain.model.IndicatorEvidenceLink> findLinksByDocumentAssetId(UUID documentAssetId) {
        return linkRepository.findByDocumentAssetId(documentAssetId).stream()
                .map(DocumentAssetJpaAdapter::toLinkDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.umss.sigesa.domain.model.IndicatorEvidenceLink> findAllLinksByProcessId(UUID processId) {
        List<DocumentAssetEntity> assets = assetRepository.findByProcessIdOrderByCreatedAtDesc(processId);
        List<com.umss.sigesa.domain.model.IndicatorEvidenceLink> result = new ArrayList<>();
        for (DocumentAssetEntity asset : assets) {
            result.addAll(linkRepository.findByDocumentAssetId(asset.getId()).stream()
                    .map(DocumentAssetJpaAdapter::toLinkDomain)
                    .toList());
        }
        return result;
    }

    private DocumentAsset toDomain(DocumentAssetEntity entity) {
        return DocumentAsset.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .folioCode(entity.getFolioCode())
                .title(entity.getTitle())
                .storageKey(entity.getStorageKey())
                .contentHash(entity.getContentHash())
                .institutional(entity.isInstitutional())
                .uploadedBy(entity.getUploadedBy())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private static DocumentAssetEntity toEntity(DocumentAsset asset) {
        DocumentAssetEntity entity = new DocumentAssetEntity();
        entity.setId(asset.getId());
        entity.setProcessId(asset.getProcessId());
        entity.setFolioCode(asset.getFolioCode());
        entity.setTitle(asset.getTitle());
        entity.setStorageKey(asset.getStorageKey());
        entity.setContentHash(asset.getContentHash());
        entity.setInstitutional(asset.isInstitutional());
        entity.setUploadedBy(asset.getUploadedBy());
        entity.setCreatedAt(asset.getCreatedAt());
        return entity;
    }

    private static IndicatorEvidenceLinkEntity toLinkEntity(IndicatorEvidenceLink link) {
        IndicatorEvidenceLinkEntity entity = new IndicatorEvidenceLinkEntity();
        entity.setId(link.getId());
        entity.setIndicatorId(link.getIndicatorId());
        entity.setDocumentAssetId(link.getDocumentAssetId());
        entity.setEvidenceId(link.getEvidenceId());
        entity.setPrimaryReference(link.isPrimaryReference());
        entity.setCreatedAt(link.getCreatedAt());
        return entity;
    }

    private static IndicatorEvidenceLink toLinkDomain(IndicatorEvidenceLinkEntity entity) {
        return IndicatorEvidenceLink.builder()
                .id(entity.getId())
                .indicatorId(entity.getIndicatorId())
                .documentAssetId(entity.getDocumentAssetId())
                .evidenceId(entity.getEvidenceId())
                .primaryReference(entity.isPrimaryReference())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
