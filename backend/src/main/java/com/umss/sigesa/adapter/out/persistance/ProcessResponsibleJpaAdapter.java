package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessResponsibleAssignmentJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProcessResponsibleRepository;
import com.umss.sigesa.application.port.out.ProcessResponsiblePort;
import com.umss.sigesa.domain.model.ProcessResponsibleAssignment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProcessResponsibleJpaAdapter implements ProcessResponsiblePort {

    private final SpringDataProcessResponsibleRepository repository;

    public ProcessResponsibleJpaAdapter(SpringDataProcessResponsibleRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public ProcessResponsibleAssignment save(ProcessResponsibleAssignment assignment) {
        ProcessResponsibleAssignmentJpaEntity entity = toEntity(assignment);
        if (entity.getId() == null) {
            entity.setId(UUID.randomUUID());
        }
        if (entity.getAssignedAt() == null) {
            entity.setAssignedAt(LocalDateTime.now());
        }
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessResponsibleAssignment> findActiveByProcessId(UUID processId) {
        return repository.findByProcessIdAndRevokedAtIsNull(processId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessResponsibleAssignment> findActiveByUserId(UUID userId) {
        return repository.findByUserIdAndRevokedAtIsNull(userId).map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeActiveByProcessId(UUID processId) {
        repository.revokeActiveByProcessId(processId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Set<UUID> findUserIdsWithActiveAssignment() {
        return repository.findByRevokedAtIsNull().stream()
                .map(ProcessResponsibleAssignmentJpaEntity::getUserId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessResponsibleAssignment> findAllActive() {
        return repository.findByRevokedAtIsNull().stream()
                .map(this::toDomain)
                .toList();
    }

    private ProcessResponsibleAssignmentJpaEntity toEntity(ProcessResponsibleAssignment domain) {
        ProcessResponsibleAssignmentJpaEntity entity = new ProcessResponsibleAssignmentJpaEntity();
        entity.setId(domain.getId());
        entity.setProcessId(domain.getProcessId());
        entity.setUserId(domain.getUserId());
        entity.setAssignedBy(domain.getAssignedBy());
        entity.setAssignedAt(domain.getAssignedAt());
        entity.setRevokedAt(domain.getRevokedAt());
        return entity;
    }

    private ProcessResponsibleAssignment toDomain(ProcessResponsibleAssignmentJpaEntity entity) {
        return ProcessResponsibleAssignment.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .userId(entity.getUserId())
                .assignedBy(entity.getAssignedBy())
                .assignedAt(entity.getAssignedAt())
                .revokedAt(entity.getRevokedAt())
                .build();
    }
}
