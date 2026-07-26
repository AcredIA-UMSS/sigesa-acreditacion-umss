package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.FileStoragePort;
import com.umss.sigesa.application.port.out.IndicatorStateHistoryPort;
import com.umss.sigesa.application.port.out.ObservationRepositoryPort;
import com.umss.sigesa.application.service.UploadEvidenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvidenceModuleConfig {

    @Bean
    public UploadEvidenceUseCase uploadEvidenceUseCase(EvidenceRepositoryPort evidenceRepository,
                                                       FileStoragePort fileStorage,
                                                       IndicatorStateHistoryPort indicatorStateHistory,
                                                       ObservationRepositoryPort observationRepository) {
        return new UploadEvidenceService(evidenceRepository, fileStorage, indicatorStateHistory, observationRepository);
    }
}
