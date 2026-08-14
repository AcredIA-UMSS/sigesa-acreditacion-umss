package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CheckEvidenceCompletenessUseCase;
import com.umss.sigesa.application.port.in.GetEvidenceDetailUseCase;
import com.umss.sigesa.application.port.in.ListPendingEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListUploadableIndicatorsUseCase;
import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.in.SearchEvidenceUseCase;
import com.umss.sigesa.application.port.in.DownloadEvidenceUseCase;
import com.umss.sigesa.application.port.out.AssistantQueryPort;
import com.umss.sigesa.application.port.out.SearchEvidenceQueryPort;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceControlQueryPort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evidence.CheckEvidenceCompletenessService;
import com.umss.sigesa.application.service.evidence.GetEvidenceDetailService;
import com.umss.sigesa.application.service.evidence.ListPendingEvidencesService;
import com.umss.sigesa.application.service.evidence.ListUploadableIndicatorsService;
import com.umss.sigesa.application.service.evidence.UploadEvidenceService;
import com.umss.sigesa.application.service.evidence.SearchEvidenceService;
import com.umss.sigesa.application.service.evidence.DownloadEvidenceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvidenceModuleConfig {

    @Bean
    UploadEvidenceUseCase uploadEvidenceUseCase(
            IndicatorRepositoryPort indicatorRepository,
            EvidenceRepositoryPort evidenceRepository,
            EvidenceUploadPersistencePort uploadPersistence,
            EvidenceBlobStoragePort blobStorage,
            ContentHashPort contentHashPort,
            EvidenceUploadLockPort uploadLock,
            NotificationOutboxPort notificationOutbox,
            AuditLogPort auditLogPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new UploadEvidenceService(
                indicatorRepository,
                evidenceRepository,
                uploadPersistence,
                blobStorage,
                contentHashPort,
                uploadLock,
                notificationOutbox,
                auditLogPort,
                assignmentRepository
        );
    }

    @Bean
    SearchEvidenceUseCase searchEvidenceUseCase(
            SearchEvidenceQueryPort queryPort,
            AssistantQueryPort assistantQueryPort,
            AssistantProperties assistantProperties,
            com.umss.sigesa.adapter.out.persistance.EvaluationDimensionJpaRepository dimensionRepository) {
        return new SearchEvidenceService(
                queryPort,
                assistantQueryPort,
                assistantProperties,
                dimensionRepository
        );
    }

    @Bean
    DownloadEvidenceUseCase downloadEvidenceUseCase(
            SearchEvidenceQueryPort queryPort,
            EvidenceBlobStoragePort blobStorage) {
        return new DownloadEvidenceService(queryPort, blobStorage);
    }

    @Bean
    ListPendingEvidencesUseCase listPendingEvidencesUseCase(EvidenceControlQueryPort evidenceControlQueryPort) {
        return new ListPendingEvidencesService(evidenceControlQueryPort);
    }

    @Bean
    GetEvidenceDetailUseCase getEvidenceDetailUseCase(EvidenceControlQueryPort evidenceControlQueryPort) {
        return new GetEvidenceDetailService(evidenceControlQueryPort);
    }

    @Bean
    CheckEvidenceCompletenessUseCase checkEvidenceCompletenessUseCase(
            EvidenceControlQueryPort evidenceControlQueryPort) {
        return new CheckEvidenceCompletenessService(evidenceControlQueryPort);
    }

    @Bean
    ListUploadableIndicatorsUseCase listUploadableIndicatorsUseCase(
            EvidenceControlQueryPort evidenceControlQueryPort) {
        return new ListUploadableIndicatorsService(evidenceControlQueryPort);
    }
}
