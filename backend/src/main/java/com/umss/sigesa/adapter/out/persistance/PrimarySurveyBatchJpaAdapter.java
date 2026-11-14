package com.umss.sigesa.adapter.out.persistance;

import com.umss.sigesa.adapter.out.persistance.entity.AccreditationProcessJpaEntity;
import com.umss.sigesa.adapter.out.persistance.entity.PrimarySurveyBatchJpaEntity;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataAccreditationProcessRepository;
import com.umss.sigesa.adapter.out.persistance.repository.SpringDataPrimarySurveyBatchRepository;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.domain.model.PrimarySurveyBatch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PrimarySurveyBatchJpaAdapter implements PrimarySurveyBatchPort {

    private final SpringDataPrimarySurveyBatchRepository surveyBatchRepository;
    private final SpringDataAccreditationProcessRepository processRepository;

    @Override
    @Transactional
    public PrimarySurveyBatch save(PrimarySurveyBatch batch) {
        AccreditationProcessJpaEntity process = processRepository.findById(batch.getProcessId())
                .orElseThrow(() -> new IllegalStateException("Proceso no encontrado: " + batch.getProcessId()));
        PrimarySurveyBatchJpaEntity entity = PrimarySurveyBatchJpaEntity.builder()
                .process(process)
                .audience(batch.getAudience())
                .responsesCount(batch.getResponsesCount())
                .documentAssetId(batch.getDocumentAssetId())
                .registeredAt(batch.getRegisteredAt() != null ? batch.getRegisteredAt() : LocalDateTime.now())
                .build();
        return toDomain(surveyBatchRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PrimarySurveyBatch> findByProcessId(UUID processId) {
        return surveyBatchRepository.findByProcessIdOrderByRegisteredAtDesc(processId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByProcessId(UUID processId) {
        return surveyBatchRepository.countByProcessId(processId);
    }

    private PrimarySurveyBatch toDomain(PrimarySurveyBatchJpaEntity entity) {
        return PrimarySurveyBatch.builder()
                .id(entity.getId())
                .processId(entity.getProcess().getId())
                .audience(entity.getAudience())
                .responsesCount(entity.getResponsesCount())
                .documentAssetId(entity.getDocumentAssetId())
                .registeredAt(entity.getRegisteredAt())
                .build();
    }
}
