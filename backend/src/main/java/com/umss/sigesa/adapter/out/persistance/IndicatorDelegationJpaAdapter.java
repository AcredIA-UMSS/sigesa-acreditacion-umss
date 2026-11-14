package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.IndicatorDelegationEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataIndicatorDelegationRepository;
import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.domain.model.IndicatorDelegation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IndicatorDelegationJpaAdapter implements IndicatorDelegationPort {

    private final SpringDataIndicatorDelegationRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<IndicatorDelegation> findByProcessId(UUID processId) {
        return repository.findByProcessIdOrderByCreatedAtAsc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<IndicatorDelegation> findByProcessIdAndUserId(UUID processId, UUID userId) {
        return repository.findByProcessIdAndUserId(processId, userId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<IndicatorDelegation> findByIdAndProcessId(UUID delegationId, UUID processId) {
        return repository.findByIdAndProcessId(delegationId, processId).map(this::toDomain);
    }

    @Override
    @Transactional
    public IndicatorDelegation save(IndicatorDelegation delegation) {
        IndicatorDelegationEntity entity = repository.findById(delegation.getId())
                .orElseGet(IndicatorDelegationEntity::new);
        entity.setId(delegation.getId());
        entity.setProcessId(delegation.getProcessId());
        entity.setUserId(delegation.getUserId());
        entity.setLevel1Id(delegation.getLevel1Id());
        entity.setLevel2Id(delegation.getLevel2Id());
        entity.setIndicatorId(delegation.getIndicatorId());
        entity.setPermission(delegation.getPermission());
        entity.setCreatedAt(delegation.getCreatedAt());
        return toDomain(repository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID delegationId) {
        repository.deleteById(delegationId);
    }

    private IndicatorDelegation toDomain(IndicatorDelegationEntity entity) {
        return IndicatorDelegation.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .userId(entity.getUserId())
                .level1Id(entity.getLevel1Id())
                .level2Id(entity.getLevel2Id())
                .indicatorId(entity.getIndicatorId())
                .permission(entity.getPermission())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
