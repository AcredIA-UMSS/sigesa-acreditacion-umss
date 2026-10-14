package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.ProcessFreezeEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataProcessFreezeRepository;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.domain.model.ProcessFreeze;
import com.umss.sigesa.domain.model.ProcessFreezeMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessFreezeJpaAdapter implements ProcessFreezePort {

    private final SpringDataProcessFreezeRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ProcessFreeze> findByProcessId(UUID processId) {
        return repository.findByProcessId(processId).map(this::toDomain);
    }

    @Override
    @Transactional
    public ProcessFreeze save(ProcessFreeze freeze) {
        ProcessFreezeEntity entity = repository.findById(freeze.getId())
                .orElseGet(ProcessFreezeEntity::new);
        entity.setId(freeze.getId());
        entity.setProcessId(freeze.getProcessId());
        entity.setMode(freeze.getMode().name());
        entity.setFrozenAt(freeze.getFrozenAt());
        entity.setFrozenBy(freeze.getFrozenBy());
        entity.setLiftedAt(freeze.getLiftedAt());
        entity.setLiftedBy(freeze.getLiftedBy());
        return toDomain(repository.save(entity));
    }

    private ProcessFreeze toDomain(ProcessFreezeEntity entity) {
        return ProcessFreeze.builder()
                .id(entity.getId())
                .processId(entity.getProcessId())
                .mode(ProcessFreezeMode.valueOf(entity.getMode()))
                .frozenAt(entity.getFrozenAt())
                .frozenBy(entity.getFrozenBy())
                .liftedAt(entity.getLiftedAt())
                .liftedBy(entity.getLiftedBy())
                .build();
    }
}
