package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.UploadEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceRepositoryPort;
import com.umss.sigesa.application.port.out.EvidenceUploadLockPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evidence.UploadEvidenceService;
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
}
