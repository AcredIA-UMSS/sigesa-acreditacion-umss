package com.umss.sigesa.application.service.evidence;

import com.umss.sigesa.application.port.in.LinkDocumentAssetToIndicatorUseCase;
import com.umss.sigesa.application.port.out.AuditLogPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvidenceUploadPersistencePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.CaeDelegationGuard;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.domain.exception.DocumentAssetNotFoundException;
import com.umss.sigesa.domain.exception.EvidenceUnclassifiedException;
import com.umss.sigesa.domain.exception.IndicatorAlreadyHasEvidenceException;
import com.umss.sigesa.domain.exception.IndicatorNotFoundException;
import com.umss.sigesa.domain.exception.ProgramScopeDeniedException;
import com.umss.sigesa.domain.exception.SubsanationNotAllowedException;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.Evidence;
import com.umss.sigesa.domain.model.EvidenceVersion;
import com.umss.sigesa.domain.model.IndicatorEvidenceLink;
import com.umss.sigesa.domain.model.IndicatorState;
import com.umss.sigesa.domain.model.LinkDocumentAssetToIndicatorCommand;
import com.umss.sigesa.domain.model.NormativeIndicatorEvidenceUploadResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LinkDocumentAssetToIndicatorService implements LinkDocumentAssetToIndicatorUseCase {

    private final NormativeHierarchyQueryPort hierarchyQueryPort;
    private final NormativeIndicatorObservationPort observationPort;
    private final NormativeIndicatorWorkflowPort workflowPort;
    private final EvidenceUploadPersistencePort uploadPersistence;
    private final NotificationOutboxPort notificationOutbox;
    private final AuditLogPort auditLogPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final DocumentAssetPort documentAssetPort;
    private final NormativeIndicatorEvidenceQueryPort evidenceQueryPort;
    private final ProcessFreezeGuard freezeGuard;
    private final CaeDelegationGuard caeDelegationGuard;

    public LinkDocumentAssetToIndicatorService(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorWorkflowPort workflowPort,
            EvidenceUploadPersistencePort uploadPersistence,
            NotificationOutboxPort notificationOutbox,
            AuditLogPort auditLogPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            DocumentAssetPort documentAssetPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            ProcessFreezeGuard freezeGuard,
            CaeDelegationGuard caeDelegationGuard) {
        this.hierarchyQueryPort = hierarchyQueryPort;
        this.observationPort = observationPort;
        this.workflowPort = workflowPort;
        this.uploadPersistence = uploadPersistence;
        this.notificationOutbox = notificationOutbox;
        this.auditLogPort = auditLogPort;
        this.assignmentRepository = assignmentRepository;
        this.documentAssetPort = documentAssetPort;
        this.evidenceQueryPort = evidenceQueryPort;
        this.freezeGuard = freezeGuard;
        this.caeDelegationGuard = caeDelegationGuard;
    }

    @Override
    public NormativeIndicatorEvidenceUploadResult link(LinkDocumentAssetToIndicatorCommand command) {
        if (command.documentAssetId() == null) {
            throw new EvidenceUnclassifiedException("documentAssetId");
        }

        NormativeHierarchyQueryPort.NormativeIndicatorContext context = hierarchyQueryPort
                .findIndicatorContext(command.indicatorId())
                .orElseThrow(() -> new IndicatorNotFoundException(command.indicatorId()));

        observationPort.findLatestOpenByIndicatorId(command.indicatorId()).ifPresent(open -> {
            throw new SubsanationNotAllowedException(
                    "Hay una observación pendiente; subsane la evidencia en lugar de vincular un documento.");
        });

        assertProgramScope(command.uploadedBy(), context.careerId(), command.jwtProgramScope());
        freezeGuard.assertWritable(context.processId());
        caeDelegationGuard.assertCanWrite(command.uploadedBy(), context.processId(), command.indicatorId());

        if (evidenceQueryPort.hasEvidences(command.indicatorId())) {
            throw new IndicatorAlreadyHasEvidenceException(command.indicatorId());
        }

        DocumentAsset asset = documentAssetPort.findById(command.documentAssetId())
                .orElseThrow(() -> new DocumentAssetNotFoundException(command.documentAssetId()));

        if (!asset.getProcessId().equals(context.processId())) {
            throw new ProgramScopeDeniedException();
        }

        IndicatorState resultingState = context.status();
        if (context.status() == IndicatorState.PENDIENTE) {
            workflowPort.updateIndicatorStatus(command.indicatorId(), IndicatorState.SUBIDO);
            resultingState = IndicatorState.SUBIDO;
        }

        UUID evidenceId = UUID.randomUUID();
        UUID versionId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        String description = asset.getTitle() != null && !asset.getTitle().isBlank()
                ? asset.getTitle()
                : "Documento vinculado " + asset.getFolioCode();

        Evidence evidence = Evidence.forNormativeIndicator(evidenceId, command.indicatorId(), versionId, now);
        EvidenceVersion version = new EvidenceVersion(
                versionId,
                evidenceId,
                1,
                asset.getContentHash(),
                null,
                description,
                asset.getStorageKey(),
                command.uploadedBy(),
                now);
        IndicatorEvidenceLink link = IndicatorEvidenceLink.primary(
                command.indicatorId(), asset.getId(), evidenceId, now);

        uploadPersistence.persistNormativeIndicatorUpload(
                evidence, version, null, asset, false, link);

        notificationOutbox.enqueueEvidenceUploaded(
                command.indicatorId(), evidenceId, context.careerId());
        auditLogPort.logEvidenceUploaded(command.uploadedBy(), evidenceId, command.indicatorId());

        return new NormativeIndicatorEvidenceUploadResult(
                evidenceId,
                1,
                asset.getContentHash(),
                UploadNormativeIndicatorEvidenceService.EVENT_EVIDENCE_UPLOADED,
                resultingState,
                asset.getId(),
                asset.getFolioCode(),
                true);
    }

    private void assertProgramScope(UUID userId, UUID programId, List<UUID> jwtProgramScope) {
        boolean allowed = assignmentRepository.findActiveByUserId(userId).stream()
                .anyMatch(a -> a.getProgramId().equals(programId));
        if (allowed) {
            return;
        }
        if (jwtProgramScope != null && jwtProgramScope.contains(programId)) {
            return;
        }
        throw new ProgramScopeDeniedException();
    }
}
