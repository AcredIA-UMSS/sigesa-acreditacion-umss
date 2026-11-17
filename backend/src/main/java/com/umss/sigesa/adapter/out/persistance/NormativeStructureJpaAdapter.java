package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level2NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.Level3NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.NormativeHierarchyPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel2NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel3NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.application.port.out.NormativeStructurePort;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.Level2Node;
import com.umss.sigesa.domain.model.Level3Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
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
public class NormativeStructureJpaAdapter implements NormativeStructurePort {

    private final SpringDataLevel1NodeRepository level1Repository;
    private final SpringDataLevel2NodeRepository level2Repository;
    private final SpringDataLevel3NodeRepository level3Repository;
    private final SpringDataNormativeIndicatorRepository indicatorRepository;
    private final NormativeHierarchyPersistenceMapper mapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Level1Node saveLevel1(UUID processId, Level1Node node) {
        LocalDateTime now = LocalDateTime.now();
        Level1NodeJpaEntity entity = Level1NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .status(node.getStatus() != null ? node.getStatus().name() : PhaseState.ABIERTA.name())
                .process(entityManager.getReference(AccreditationProcessJpaEntity.class, processId))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toLevel1Domain(level1Repository.save(entity));
    }

    @Override
    @Transactional
    public Level1Node updateLevel1(UUID processId, UUID level1Id, Level1Node node) {
        Level1NodeJpaEntity entity = level1Repository.findByIdAndProcessId(level1Id, processId)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 1 no encontrado: " + level1Id));
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
        return mapper.toLevel1Domain(level1Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel1(UUID processId, UUID level1Id) {
        Level1NodeJpaEntity entity = level1Repository.findByIdAndProcessId(level1Id, processId)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 1 no encontrado: " + level1Id));
        level1Repository.delete(entity);
    }

    @Override
    @Transactional
    public Level2Node saveLevel2(UUID level1Id, Level2Node node) {
        LocalDateTime now = LocalDateTime.now();
        Level2NodeJpaEntity entity = Level2NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .level1Node(entityManager.getReference(Level1NodeJpaEntity.class, level1Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toLevel2Domain(level2Repository.save(entity));
    }

    @Override
    @Transactional
    public Level2Node updateLevel2(UUID level1Id, UUID level2Id, Level2Node node) {
        Level2NodeJpaEntity entity = level2Repository.findByIdAndLevel1NodeId(level2Id, level1Id)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 2 no encontrado: " + level2Id));
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
        return mapper.toLevel2Domain(level2Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel2(UUID level1Id, UUID level2Id) {
        Level2NodeJpaEntity entity = level2Repository.findByIdAndLevel1NodeId(level2Id, level1Id)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 2 no encontrado: " + level2Id));
        level2Repository.delete(entity);
    }

    @Override
    @Transactional
    public Level3Node saveLevel3(UUID level2Id, Level3Node node) {
        LocalDateTime now = LocalDateTime.now();
        Level3NodeJpaEntity entity = Level3NodeJpaEntity.builder()
                .name(node.getName())
                .order(node.getOrder())
                .description(node.getDescription())
                .level2Node(entityManager.getReference(Level2NodeJpaEntity.class, level2Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toLevel3Domain(level3Repository.save(entity));
    }

    @Override
    @Transactional
    public Level3Node updateLevel3(UUID level2Id, UUID level3Id, Level3Node node) {
        Level3NodeJpaEntity entity = level3Repository.findByIdAndLevel2NodeId(level3Id, level2Id)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 3 no encontrado: " + level3Id));
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
        return mapper.toLevel3Domain(level3Repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteLevel3(UUID level2Id, UUID level3Id) {
        Level3NodeJpaEntity entity = level3Repository.findByIdAndLevel2NodeId(level3Id, level2Id)
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 3 no encontrado: " + level3Id));
        level3Repository.delete(entity);
    }

    @Override
    @Transactional
    public NormativeIndicator saveIndicator(UUID level3Id, NormativeIndicator indicator) {
        LocalDateTime now = LocalDateTime.now();
        NormativeIndicatorJpaEntity entity = NormativeIndicatorJpaEntity.builder()
                .code(indicator.getCode())
                .description(indicator.getDescription())
                .weight(indicator.getWeight() != null ? indicator.getWeight() : BigDecimal.ONE)
                .order(indicator.getOrder())
                .referenceUrl(indicator.getReferenceUrl())
                .status(indicator.getStatus() != null ? indicator.getStatus().name() : IndicatorState.PENDIENTE.name())
                .level3Node(entityManager.getReference(Level3NodeJpaEntity.class, level3Id))
                .createdAt(now)
                .updatedAt(now)
                .build();
        return mapper.toIndicatorDomain(indicatorRepository.save(entity));
    }

    @Override
    @Transactional
    public NormativeIndicator updateIndicator(UUID level3Id, UUID indicatorId, NormativeIndicator indicator) {
        NormativeIndicatorJpaEntity entity = indicatorRepository.findByIdAndLevel3NodeId(indicatorId, level3Id)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
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
        return mapper.toIndicatorDomain(indicatorRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteIndicator(UUID level3Id, UUID indicatorId) {
        NormativeIndicatorJpaEntity entity = indicatorRepository.findByIdAndLevel3NodeId(indicatorId, level3Id)
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
        indicatorRepository.delete(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NormativeIndicator> findIndicatorsUnderLevel1(UUID level1Id) {
        return indicatorRepository.findAllByLevel1Id(level1Id).stream()
                .map(mapper::toIndicatorDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProcessIdByLevel1(UUID level1Id) {
        return level1Repository.findWithProcessById(level1Id)
                .map(entity -> entity.getProcess().getId())
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 1 no encontrado: " + level1Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProcessIdByLevel2(UUID level2Id) {
        return level2Repository.findWithProcessById(level2Id)
                .map(entity -> entity.getLevel1Node().getProcess().getId())
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProcessIdByLevel3(UUID level3Id) {
        return level3Repository.findWithProcessById(level3Id)
                .map(entity -> entity.getLevel2Node().getLevel1Node().getProcess().getId())
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findProcessIdByIndicator(UUID indicatorId) {
        return indicatorRepository.findWithProcessById(indicatorId)
                .map(entity -> entity.getLevel3Node().getLevel2Node().getLevel1Node().getProcess().getId())
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel1IdByLevel2(UUID level2Id) {
        return level2Repository.findWithProcessById(level2Id)
                .map(entity -> entity.getLevel1Node().getId())
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 2 no encontrado: " + level2Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel2IdByLevel3(UUID level3Id) {
        return level3Repository.findWithProcessById(level3Id)
                .map(entity -> entity.getLevel2Node().getId())
                .orElseThrow(() -> new ProcessNotFoundException("Nivel 3 no encontrado: " + level3Id));
    }

    @Override
    @Transactional(readOnly = true)
    public UUID findLevel3IdByIndicator(UUID indicatorId) {
        return indicatorRepository.findById(indicatorId)
                .map(entity -> entity.getLevel3Node().getId())
                .orElseThrow(() -> new IndicatorNotFoundException(indicatorId));
    }
}
