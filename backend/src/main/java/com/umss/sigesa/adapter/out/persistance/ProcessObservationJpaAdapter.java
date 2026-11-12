package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessObservationJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.ProcessObservationMessageJpaEntity;
import com.umss.sigesa.application.port.out.ProcessObservationRepositoryPort;
import com.umss.sigesa.domain.model.observation.ObservationMessage;
import com.umss.sigesa.domain.model.observation.ProcessObservation;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Transactional(readOnly = true)
public class ProcessObservationJpaAdapter implements ProcessObservationRepositoryPort {

    private final ProcessObservationJpaRepository repository;

    public ProcessObservationJpaAdapter(ProcessObservationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ProcessObservation save(ProcessObservation observation) {
        ProcessObservationJpaEntity entity = repository.findById(observation.getId())
                .orElseGet(ProcessObservationJpaEntity::new);
        entity.setId(observation.getId());
        entity.setProcessId(observation.getProcessId());
        entity.setEvidenceId(observation.getEvidenceId());
        entity.setIndicatorId(observation.getIndicatorId());
        entity.setDescription(observation.getDescription());
        entity.setKind(observation.getKind());
        entity.setPriority(observation.getPriority());
        entity.setCompliance(observation.getCompliance());
        entity.setRecommendation(observation.getRecommendation());
        entity.setAuthorId(observation.getAuthorId());
        entity.setStatus(observation.getStatus());
        entity.setCreatedAt(observation.getCreatedAt());
        entity.setUpdatedAt(observation.getUpdatedAt());
        entity.getMessages().clear();
        for (ObservationMessage message : observation.getMessages()) {
            ProcessObservationMessageJpaEntity row = new ProcessObservationMessageJpaEntity();
            row.setId(message.id());
            row.setObservation(entity);
            row.setAuthorId(message.authorId());
            row.setAuthorRole(message.authorRole());
            row.setBody(message.body());
            row.setCreatedAt(message.createdAt());
            entity.getMessages().add(row);
        }
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<ProcessObservation> findById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public List<ProcessObservation> findByProcessId(UUID processId) {
        return repository.findByProcessIdOrderByUpdatedAtDesc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ProcessObservation toDomain(ProcessObservationJpaEntity entity) {
        List<ObservationMessage> messages = entity.getMessages().stream()
                .map(row -> new ObservationMessage(
                        row.getId(), row.getAuthorId(), row.getAuthorRole(), row.getBody(), row.getCreatedAt()))
                .toList();
        return ProcessObservation.restore(
                entity.getId(),
                entity.getProcessId(),
                entity.getEvidenceId(),
                entity.getIndicatorId(),
                entity.getDescription(),
                entity.getKind(),
                entity.getPriority(),
                entity.getCompliance(),
                entity.getRecommendation(),
                entity.getAuthorId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                messages);
    }
}
