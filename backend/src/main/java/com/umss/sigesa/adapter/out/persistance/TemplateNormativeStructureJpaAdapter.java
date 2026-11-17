package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.TemplateIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.NormativeHierarchyPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel2NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel3NodeRepository;
import com.umss.sigesa.application.port.out.TemplateNormativeStructurePort;
import com.umss.sigesa.domain.exception.TemplateNotFoundException;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateLevel2Node;
import com.umss.sigesa.domain.model.TemplateLevel3Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TemplateNormativeStructureJpaAdapter implements TemplateNormativeStructurePort {

    private final SpringDataTemplateLevel1NodeRepository level1Repository;
    private final SpringDataTemplateLevel2NodeRepository level2Repository;
    private final SpringDataTemplateLevel3NodeRepository level3Repository;
    private final SpringDataTemplateIndicatorRepository indicatorRepository;
    private final NormativeHierarchyPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public TemplateLevel1Node saveLevel1(UUID templateId, TemplateLevel1Node node) {
        LocalDateTime now = LocalDateTime.now();
        TemplateLevel1NodeJpaEntity entity = TemplateLevel1NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .template(entityManager.getReference(TemplateJpaEntity.class, templateId))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toTemplateLevel1Domain(level1Repository.save(entity));
    }

    @Override
    @Transactional
    public TemplateLevel1Node updateLevel1(UUID templateId, UUID level1Id, TemplateLevel1Node node) {
        TemplateLevel1NodeJpaEntity entity = level1Repository.findByIdAndTemplateId(level1Id, templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 1 no encontrado: " + level1Id));
        if (node.getName() != null) {
            entity.setName(node.getName());
        }
        if (node.getOrder() != null) {
            entity.setOrder(node.getOrder());
        }
        if (node.getDescription() != null) {
            entity.setDescription(node.getDescription());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toTemplateLevel1Domain(level1Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel1(UUID templateId, UUID level1Id) {
        TemplateLevel1NodeJpaEntity entity = level1Repository.findByIdAndTemplateId(level1Id, templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 1 no encontrado: " + level1Id));
        level1Repository.delete(entity);
    }

    @Override
    @Transactional
    public TemplateLevel2Node saveLevel2(UUID level1Id, TemplateLevel2Node node) {
        LocalDateTime now = LocalDateTime.now();
        TemplateLevel2NodeJpaEntity entity = TemplateLevel2NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .level1Node(entityManager.getReference(TemplateLevel1NodeJpaEntity.class, level1Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toTemplateLevel2Domain(level2Repository.save(entity));
    }

    @Override
    @Transactional
    public TemplateLevel2Node updateLevel2(UUID level1Id, UUID level2Id, TemplateLevel2Node node) {
        TemplateLevel2NodeJpaEntity entity = level2Repository.findByIdAndLevel1NodeId(level2Id, level1Id)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 2 no encontrado: " + level2Id));
        if (node.getName() != null) {
            entity.setName(node.getName());
        }
        if (node.getOrder() != null) {
            entity.setOrder(node.getOrder());
        }
        if (node.getDescription() != null) {
            entity.setDescription(node.getDescription());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toTemplateLevel2Domain(level2Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel2(UUID level1Id, UUID level2Id) {
        TemplateLevel2NodeJpaEntity entity = level2Repository.findByIdAndLevel1NodeId(level2Id, level1Id)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 2 no encontrado: " + level2Id));
        level2Repository.delete(entity);
    }

    @Override
    @Transactional
    public TemplateLevel3Node saveLevel3(UUID level2Id, TemplateLevel3Node node) {
        LocalDateTime now = LocalDateTime.now();
        TemplateLevel3NodeJpaEntity entity = TemplateLevel3NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .level2Node(entityManager.getReference(TemplateLevel2NodeJpaEntity.class, level2Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toTemplateLevel3Domain(level3Repository.save(entity));
    }

    @Override
    @Transactional
    public TemplateLevel3Node updateLevel3(UUID level2Id, UUID level3Id, TemplateLevel3Node node) {
        TemplateLevel3NodeJpaEntity entity = level3Repository.findByIdAndLevel2NodeId(level3Id, level2Id)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 3 no encontrado: " + level3Id));
        if (node.getName() != null) {
            entity.setName(node.getName());
        }
        if (node.getOrder() != null) {
            entity.setOrder(node.getOrder());
        }
        if (node.getDescription() != null) {
            entity.setDescription(node.getDescription());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toTemplateLevel3Domain(level3Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel3(UUID level2Id, UUID level3Id) {
        TemplateLevel3NodeJpaEntity entity = level3Repository.findByIdAndLevel2NodeId(level3Id, level2Id)
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 3 no encontrado: " + level3Id));
        level3Repository.delete(entity);
    }

    @Override
    @Transactional
    public TemplateNormativeIndicator saveIndicator(UUID level3Id, TemplateNormativeIndicator indicator) {
        LocalDateTime now = LocalDateTime.now();
        TemplateIndicatorJpaEntity entity = TemplateIndicatorJpaEntity.builder()
                .code(indicator.getCode())
                .description(indicator.getDescription())
                .weight(indicator.getWeight() != null ? indicator.getWeight() : BigDecimal.ONE)
                .order(indicator.getOrder())
                .referenceUrl(indicator.getReferenceUrl())
                .level3Node(entityManager.getReference(TemplateLevel3NodeJpaEntity.class, level3Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toTemplateIndicatorDomain(indicatorRepository.save(entity));
    }

    @Override
    @Transactional
    public TemplateNormativeIndicator updateIndicator(UUID level3Id, UUID indicatorId,
                                                      TemplateNormativeIndicator indicator) {
        TemplateIndicatorJpaEntity entity = indicatorRepository.findByIdAndLevel3NodeId(indicatorId, level3Id)
                .orElseThrow(() -> new TemplateNotFoundException("Indicador no encontrado: " + indicatorId));
        if (indicator.getCode() != null) {
            entity.setCode(indicator.getCode());
        }
        if (indicator.getDescription() != null) {
            entity.setDescription(indicator.getDescription());
        }
        if (indicator.getWeight() != null) {
            entity.setWeight(indicator.getWeight());
        }
        if (indicator.getOrder() != null) {
            entity.setOrder(indicator.getOrder());
        }
        if (indicator.getReferenceUrl() != null) {
            entity.setReferenceUrl(indicator.getReferenceUrl());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return mapper.toTemplateIndicatorDomain(indicatorRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteIndicator(UUID level3Id, UUID indicatorId) {
        TemplateIndicatorJpaEntity entity = indicatorRepository.findByIdAndLevel3NodeId(indicatorId, level3Id)
                .orElseThrow(() -> new TemplateNotFoundException("Indicador no encontrado: " + indicatorId));
        indicatorRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateNormativeIndicator> findIndicatorsUnderLevel1(UUID level1Id) {
        return indicatorRepository.findAllByLevel1Id(level1Id).stream()
                .map(mapper::toTemplateIndicatorDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findTemplateIdByLevel1(UUID level1Id) {
        return level1Repository.findWithTemplateById(level1Id)
                .map(entity -> entity.getTemplate().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 1 no encontrado: " + level1Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findTemplateIdByLevel2(UUID level2Id) {
        return level2Repository.findWithTemplateById(level2Id)
                .map(entity -> entity.getLevel1Node().getTemplate().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findTemplateIdByLevel3(UUID level3Id) {
        return level3Repository.findWithTemplateById(level3Id)
                .map(entity -> entity.getLevel2Node().getLevel1Node().getTemplate().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findTemplateIdByIndicator(UUID indicatorId) {
        return indicatorRepository.findWithTemplateById(indicatorId)
                .map(entity -> entity.getLevel3Node().getLevel2Node().getLevel1Node().getTemplate().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Indicador no encontrado: " + indicatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel1IdByLevel2(UUID level2Id) {
        return level2Repository.findWithTemplateById(level2Id)
                .map(entity -> entity.getLevel1Node().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel2IdByLevel3(UUID level3Id) {
        return level3Repository.findWithTemplateById(level3Id)
                .map(entity -> entity.getLevel2Node().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel3IdByIndicator(UUID indicatorId) {
        return indicatorRepository.findById(indicatorId)
                .map(entity -> entity.getLevel3Node().getId())
                .orElseThrow(() -> new TemplateNotFoundException("Indicador no encontrado: " + indicatorId));
    }
}
