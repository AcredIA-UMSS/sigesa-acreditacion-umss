package com.umss.sigesa.adapter.out.persistance.mapper;

import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ProcessPersistenceMapper {

    public AccreditationProcessJpaEntity toJpaEntity(AccreditationProcess domain) {
        return AccreditationProcessJpaEntity.builder()
                .id(domain.getId())
                .careerId(domain.getCareerId())
                .templateId(domain.getTemplateId())
                .status(domain.getStatus())
                .startDate(domain.getStartDate())
                .build();
    }

    public AccreditationProcess toDomain(AccreditationProcessJpaEntity entity) {
        return AccreditationProcess.builder()
                .id(entity.getId())
                .careerId(entity.getCareerId())
                .templateId(entity.getTemplateId())
                .status(entity.getStatus())
                .startDate(entity.getStartDate())
                .build();
    }
}
