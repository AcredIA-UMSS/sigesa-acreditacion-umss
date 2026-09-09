package com.umss.sigesa.adapter.out.persistance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.MethodologicalStageJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.StageDeliverableJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.StageGateEvaluationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.mapper.MethodologicalStagePersistenceMapper;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataMethodologicalStageRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPrimarySurveyBatchRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataStageDeliverableRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataStageGateEvaluationRepository;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageStatus;
import com.umss.sigesa.domain.model.OperationalMode;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageGateResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MethodologicalStageJpaAdapter implements MethodologicalStagePort {

    private final SpringDataMethodologicalStageRepository stageRepository;
    private final SpringDataStageDeliverableRepository deliverableRepository;
    private final SpringDataStageGateEvaluationRepository gateEvaluationRepository;
    private final SpringDataPrimarySurveyBatchRepository surveyBatchRepository;
    private final SpringDataAccreditationProcessRepository processRepository;
    private final MethodologicalStagePersistenceMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public List<MethodologicalStage> findByProcessIdOrderByOrder(UUID processId) {
        return stageRepository.findByProcessIdOrderByOrderAsc(processId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MethodologicalStage> findByIdAndProcessId(UUID stageId, UUID processId) {
        return stageRepository.findByIdAndProcessId(stageId, processId).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MethodologicalStage> findNextStage(UUID processId, int currentOrder) {
        return stageRepository.findByProcessIdAndOrder(processId, currentOrder + 1)
                .map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void saveStages(List<MethodologicalStage> stages) {
        if (stages.isEmpty()) {
            return;
        }
        UUID processId = stages.getFirst().getProcessId();
        AccreditationProcessJpaEntity process = processRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Proceso no encontrado: " + processId));
        LocalDateTime now = LocalDateTime.now();

        for (MethodologicalStage stage : stages) {
            MethodologicalStageJpaEntity entity = MethodologicalStageJpaEntity.builder()
                    .process(process)
                    .order(stage.getOrder())
                    .code(stage.getCode().name())
                    .status(stage.getStatus().name())
                    .startedAt(stage.getStartedAt())
                    .closedAt(stage.getClosedAt())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

            for (StageDeliverable deliverable : stage.getDeliverables()) {
                StageDeliverableJpaEntity deliverableEntity = StageDeliverableJpaEntity.builder()
                        .stage(entity)
                        .deliverableCode(deliverable.getDeliverableCode().name())
                        .approvalStatus(deliverable.getApprovalStatus().name())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                entity.getDeliverables().add(deliverableEntity);
            }

            stageRepository.save(entity);

            if (stage.getOrder() == 1) {
                process.setCurrentStageId(entity.getId());
                process.setOperationalMode(OperationalMode.ACTIVE.name());
                processRepository.save(process);
            }
        }
    }

    @Override
    @Transactional
    public void updateStageStatus(UUID stageId, MethodologicalStageStatus status) {
        MethodologicalStageJpaEntity entity = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("Etapa no encontrada: " + stageId));
        entity.setStatus(status.name());
        entity.setUpdatedAt(LocalDateTime.now());
        stageRepository.save(entity);
    }

    @Override
    @Transactional
    public void markStageClosed(UUID stageId) {
        MethodologicalStageJpaEntity entity = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("Etapa no encontrada: " + stageId));
        entity.setClosedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        stageRepository.save(entity);
    }

    @Override
    @Transactional
    public void markStageStarted(UUID stageId) {
        MethodologicalStageJpaEntity entity = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("Etapa no encontrada: " + stageId));
        entity.setStartedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        stageRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateProcessCurrentStage(UUID processId, UUID stageId, OperationalMode operationalMode) {
        AccreditationProcessJpaEntity process = processRepository.findById(processId)
                .orElseThrow(() -> new IllegalStateException("Proceso no encontrado: " + processId));
        process.setCurrentStageId(stageId);
        process.setOperationalMode(operationalMode.name());
        processRepository.save(process);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StageDeliverable> findDeliverableById(UUID deliverableId) {
        return deliverableRepository.findWithStageById(deliverableId).map(mapper::toDeliverableDomain);
    }

    @Override
    @Transactional
    public void updateDeliverableApproval(
            UUID deliverableId,
            StageDeliverableApprovalStatus status,
            UUID approvedBy,
            String technicalObservations) {
        StageDeliverableJpaEntity entity = deliverableRepository.findById(deliverableId)
                .orElseThrow(() -> new IllegalStateException("Entregable no encontrado: " + deliverableId));
        entity.setApprovalStatus(status.name());
        entity.setApprovedBy(approvedBy);
        entity.setTechnicalObservations(technicalObservations);
        entity.setApprovedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        deliverableRepository.save(entity);
    }

    @Override
    @Transactional
    public void saveGateEvaluation(
            UUID stageId,
            UUID evaluatedBy,
            StageGateResult gateResult,
            String rulesSnapshotJson) {
        MethodologicalStageJpaEntity stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new IllegalStateException("Etapa no encontrada: " + stageId));

        Map<String, Object> snapshot = parseSnapshot(rulesSnapshotJson);

        StageGateEvaluationJpaEntity evaluation = StageGateEvaluationJpaEntity.builder()
                .stage(stage)
                .gateRulesSnapshot(snapshot)
                .result(gateResult.pass() ? "PASS" : "BLOCK")
                .blockReason(gateResult.pass() ? null : gateResult.summary())
                .evaluatedAt(LocalDateTime.now())
                .evaluatedBy(evaluatedBy)
                .build();
        gateEvaluationRepository.save(evaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public long countPrimarySurveyBatches(UUID processId) {
        return surveyBatchRepository.countByProcessId(processId);
    }

    private Map<String, Object> parseSnapshot(String rulesSnapshotJson) {
        if (rulesSnapshotJson == null || rulesSnapshotJson.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(rulesSnapshotJson, Map.class);
        } catch (JsonProcessingException ex) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("raw", rulesSnapshotJson);
            return fallback;
        }
    }
}
