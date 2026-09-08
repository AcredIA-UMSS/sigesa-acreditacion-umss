package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.Level1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.NormativeIndicatorJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.TemplateLevel1NodeJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.NormativeHierarchyPersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataLevel1NodeRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataNormativeIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateIndicatorRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataTemplateLevel1NodeRepository;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.Level1Node;
import com.umss.sigesa.domain.model.NormativeIndicator;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.TemplateLevel1Node;
import com.umss.sigesa.domain.model.TemplateNormativeIndicator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NormativeHierarchyJpaAdapter implements NormativeHierarchyQueryPort {

    private final SpringDataLevel1NodeRepository level1NodeRepository;
    private final SpringDataNormativeIndicatorRepository indicatorRepository;
    private final SpringDataTemplateLevel1NodeRepository templateLevel1NodeRepository;
    private final SpringDataTemplateIndicatorRepository templateIndicatorRepository;
    private final NormativeHierarchyPersistenceMapper mapper;

    @Override
    public boolean hasNormativeTreeForProcess(UUID processId) {
        return level1NodeRepository.existsByProcessId(processId);
    }

    @Override
    public boolean hasNormativeTreeForTemplate(UUID templateId) {
        return templateLevel1NodeRepository.existsByTemplateId(templateId);
    }

    @Override
    public long countLevel1NodesByProcessId(UUID processId) {
        return level1NodeRepository.countByProcessId(processId);
    }

    @Override
    public long countIndicatorsByProcessId(UUID processId) {
        return indicatorRepository.countByProcessId(processId);
    }

    @Override
    public long countLevel1NodesByTemplateId(UUID templateId) {
        return templateLevel1NodeRepository.countByTemplateId(templateId);
    }

    @Override
    public long countIndicatorsByTemplateId(UUID templateId) {
        return templateIndicatorRepository.countByTemplateId(templateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessNormativeTree> findProcessTree(UUID processId) {
        List<Level1NodeJpaEntity> level1Entities = level1NodeRepository.findByProcessIdOrderByOrderAsc(processId);
        if (level1Entities.isEmpty()) {
            return Optional.empty();
        }
        level1Entities.forEach(this::initializeProcessSubtree);
        List<Level1Node> level1Nodes = level1Entities.stream()
                .map(mapper::toLevel1Domain)
                .toList();
        return Optional.of(new ProcessNormativeTree(processId, level1Nodes));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateNormativeTree> findTemplateTree(UUID templateId) {
        List<TemplateLevel1NodeJpaEntity> level1Entities =
                templateLevel1NodeRepository.findByTemplateIdOrderByOrderAsc(templateId);
        if (level1Entities.isEmpty()) {
            return Optional.empty();
        }
        level1Entities.forEach(this::initializeTemplateSubtree);
        List<TemplateLevel1Node> level1Nodes = level1Entities.stream()
                .map(mapper::toTemplateLevel1Domain)
                .toList();
        return Optional.of(new TemplateNormativeTree(templateId, level1Nodes));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NormativeIndicator> findIndicatorById(UUID indicatorId) {
        return indicatorRepository.findById(indicatorId).map(mapper::toIndicatorDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TemplateNormativeIndicator> findTemplateIndicatorById(UUID indicatorId) {
        return templateIndicatorRepository.findById(indicatorId).map(mapper::toTemplateIndicatorDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NormativeIndicatorContext> findIndicatorContext(UUID indicatorId) {
        return indicatorRepository.findWithProcessById(indicatorId).map(entity -> {
            Level1NodeJpaEntity level1 = entity.getLevel3Node().getLevel2Node().getLevel1Node();
            return new NormativeIndicatorContext(
                    entity.getId(),
                    level1.getProcess().getId(),
                    level1.getProcess().getCareerId(),
                    level1.getId(),
                    level1.getName(),
                    entity.getCode(),
                    entity.getDescription(),
                    parseIndicatorState(entity.getStatus()));
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Level1Context> findLevel1Context(UUID level1Id) {
        return level1NodeRepository.findWithProcessById(level1Id).map(entity -> new Level1Context(
                entity.getId(),
                entity.getProcess().getId(),
                entity.getProcess().getCareerId(),
                entity.getName(),
                parsePhaseState(entity.getStatus())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicatorStatusItem> listIndicatorsWithStatusByLevel1Id(UUID level1Id) {
        return indicatorRepository.findAllByLevel1Id(level1Id).stream()
                .map(entity -> new IndicatorStatusItem(
                        entity.getId(),
                        entity.getCode(),
                        entity.getDescription(),
                        parseIndicatorState(entity.getStatus()),
                        entity.getOrder()))
                .sorted(Comparator.comparing(
                        IndicatorStatusItem::order,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    private void initializeProcessSubtree(Level1NodeJpaEntity level1) {
        level1.getLevel2Nodes().forEach(level2 -> {
            level2.getLevel3Nodes().forEach(level3 -> level3.getIndicators().size());
        });
    }

    private void initializeTemplateSubtree(TemplateLevel1NodeJpaEntity level1) {
        level1.getLevel2Nodes().forEach(level2 -> {
            level2.getLevel3Nodes().forEach(level3 -> level3.getIndicators().size());
        });
    }

    private IndicatorState parseIndicatorState(String status) {
        if (status == null || status.isBlank()) {
            return IndicatorState.PENDIENTE;
        }
        return IndicatorState.valueOf(status);
    }

    private PhaseState parsePhaseState(String status) {
        if (status == null || status.isBlank()) {
            return PhaseState.ABIERTA;
        }
        return PhaseState.valueOf(status);
    }
}
