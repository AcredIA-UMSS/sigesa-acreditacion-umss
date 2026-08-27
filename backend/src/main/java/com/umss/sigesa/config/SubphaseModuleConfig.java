package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.AddSubphaseObservationUseCase;
import com.umss.sigesa.application.port.in.GetSubphaseSubsanationEligibilityUseCase;
import com.umss.sigesa.application.port.in.ListSubphaseEvidencesUseCase;
import com.umss.sigesa.application.port.in.ListSubphaseObservationsUseCase;
import com.umss.sigesa.application.port.in.SubsanateSubphaseEvidenceUseCase;
import com.umss.sigesa.application.port.in.UploadSubphaseEvidenceUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.subphase.AddSubphaseObservationService;
import com.umss.sigesa.application.service.subphase.GetSubphaseSubsanationEligibilityService;
import com.umss.sigesa.application.service.subphase.ListSubphaseEvidencesService;
import com.umss.sigesa.application.service.subphase.ListSubphaseObservationsService;
import com.umss.sigesa.application.service.subphase.SubsanateSubphaseEvidenceService;
import com.umss.sigesa.application.service.subphase.UploadSubphaseEvidenceService;
import com.umss.sigesa.application.service.workflow.SubphaseTransitionHelper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SubphaseModuleConfig {

    @Bean
    UploadSubphaseEvidenceUseCase uploadSubphaseEvidenceUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseObservationPort observationPort,
            SubphaseWorkflowPort subphaseWorkflowPort,
            SubphaseTransitionHelper subphaseTransitionHelper,
            EvidenceUploadPersistencePort uploadPersistence,
            EvidenceBlobStoragePort blobStorage,
            ContentHashPort contentHashPort,
            NotificationOutboxPort notificationOutbox,
            AuditLogPort auditLogPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new UploadSubphaseEvidenceService(
                subphaseQueryPort,
                observationPort,
                subphaseWorkflowPort,
                subphaseTransitionHelper,
                uploadPersistence,
                blobStorage,
                contentHashPort,
                notificationOutbox,
                auditLogPort,
                assignmentRepository);
    }

    @Bean
    ListSubphaseEvidencesUseCase listSubphaseEvidencesUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListSubphaseEvidencesService(subphaseQueryPort, evidenceQueryPort, assignmentRepository);
    }

    @Bean
    ListSubphaseObservationsUseCase listSubphaseObservationsUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseObservationPort observationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListSubphaseObservationsService(subphaseQueryPort, observationPort, assignmentRepository);
    }

    @Bean
    AddSubphaseObservationUseCase addSubphaseObservationUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseObservationPort observationPort) {
        return new AddSubphaseObservationService(subphaseQueryPort, observationPort);
    }

    @Bean
    GetSubphaseSubsanationEligibilityUseCase getSubphaseSubsanationEligibilityUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseObservationPort observationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetSubphaseSubsanationEligibilityService(
                subphaseQueryPort, observationPort, assignmentRepository);
    }

    @Bean
    SubsanateSubphaseEvidenceUseCase subsanateSubphaseEvidenceUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseObservationPort observationPort,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            EvidenceUploadPersistencePort uploadPersistence,
            EvidenceBlobStoragePort blobStorage,
            ContentHashPort contentHashPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            SubphaseTransitionHelper subphaseTransitionHelper) {
        return new SubsanateSubphaseEvidenceService(
                subphaseQueryPort,
                observationPort,
                evidenceQueryPort,
                uploadPersistence,
                blobStorage,
                contentHashPort,
                assignmentRepository,
                subphaseTransitionHelper);
    }
}
