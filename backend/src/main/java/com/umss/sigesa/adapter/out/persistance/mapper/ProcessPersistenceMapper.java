package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.Phase;
import com.umss.sigesa.domain.model.Subphase;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.PhaseJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.SubphaseJpaEntity;
import com.umss.sigesa.domain.model.PhaseState;
import com.umss.sigesa.domain.model.SubphaseState;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ProcessPersistenceMapper {

    public AccreditationProcessJpaEntity toJpaEntity(AccreditationProcess domain) {
        AccreditationProcessJpaEntity processEntity = AccreditationProcessJpaEntity.builder()
                .id(domain.getId())
                .careerId(domain.getCareerId())
                .templateId(domain.getTemplateId())
                .status(domain.getStatus())
                .startDate(domain.getStartDate())
                .build();

        if (domain.getPhases() != null) {
            processEntity.setPhases(domain.getPhases().stream().map(pDomain -> {
                PhaseJpaEntity phaseEntity = PhaseJpaEntity.builder()
                        .id(pDomain.getId())
                        .name(pDomain.getName())
                        .order(pDomain.getOrder())
                        .description(pDomain.getDescription())
                        .status(pDomain.getStatus() != null
                                ? pDomain.getStatus().name()
                                : PhaseState.ABIERTA.name())
                        .process(processEntity)
                        .build();

                if (pDomain.getSubphases() != null) {
                    phaseEntity.setSubphases(pDomain.getSubphases().stream().map(sDomain -> SubphaseJpaEntity.builder()
                            .id(sDomain.getId())
                            .name(sDomain.getName())
                            .order(sDomain.getOrder())
                            .referenceUrl(sDomain.getReferenceUrl())
                            .description(sDomain.getDescription())
                            .requirements(sDomain.getRequirements())
                            .status(sDomain.getStatus() != null
                                    ? sDomain.getStatus().name()
                                    : SubphaseState.PENDIENTE.name())
                            .phase(phaseEntity)
                            .build()).collect(Collectors.toList()));
                }
                return phaseEntity;
            }).collect(Collectors.toList()));
        }
        return processEntity;
    }

    public AccreditationProcess toDomain(AccreditationProcessJpaEntity entity) {
        return AccreditationProcess.builder()
                .id(entity.getId())
                .careerId(entity.getCareerId())
                .templateId(entity.getTemplateId())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .phases(entity.getPhases().stream().map(p -> Phase.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .order(p.getOrder())
                        .description(p.getDescription())
                        .status(p.getStatus() != null
                                ? PhaseState.valueOf(p.getStatus())
                                : PhaseState.ABIERTA)
                        .subphases(p.getSubphases().stream().map(s -> Subphase.builder()
                                .id(s.getId())
                                .name(s.getName())
                                .order(s.getOrder())
                                .referenceUrl(s.getReferenceUrl())
                                .description(s.getDescription())
                                .requirements(s.getRequirements())
                                .status(s.getStatus() != null
                                        ? SubphaseState.valueOf(s.getStatus())
                                        : SubphaseState.PENDIENTE)
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .build();
    }
}