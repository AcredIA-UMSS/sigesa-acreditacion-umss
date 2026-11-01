package com.umss.sigesa.application.service.workflow;

import com.umss.sigesa.application.port.in.GenerateStageDeliverableDocumentUseCase;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.DraftingDocumentRendererPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.EvaluationAccessGuard;
import com.umss.sigesa.domain.exception.DeliverableImmutableException;
import com.umss.sigesa.domain.exception.InvalidDeliverableStageException;
import com.umss.sigesa.domain.exception.ProcessNotFoundException;
import com.umss.sigesa.domain.exception.StageNotFoundException;
import com.umss.sigesa.domain.model.AccreditationProcess;
import com.umss.sigesa.domain.model.DocumentAsset;
import com.umss.sigesa.domain.model.FodaQuadrant;
import com.umss.sigesa.domain.model.GeneratedStageDeliverable;
import com.umss.sigesa.domain.model.ImprovementPlanItem;
import com.umss.sigesa.domain.model.IndicatorAnalysis;
import com.umss.sigesa.domain.model.MethodologicalStage;
import com.umss.sigesa.domain.model.MethodologicalStageCode;
import com.umss.sigesa.domain.model.StageDeliverable;
import com.umss.sigesa.domain.model.StageDeliverableApprovalStatus;
import com.umss.sigesa.domain.model.StageDeliverableCode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateStageDeliverableDocumentService implements GenerateStageDeliverableDocumentUseCase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ProcessQueryPort processQueryPort;
    private final MethodologicalStagePort stagePort;
    private final ImprovementPlanItemPort planItemPort;
    private final IndicatorAnalysisPort analysisPort;
    private final EvaluationMetricsPort evaluationMetricsPort;
    private final DraftingDocumentRendererPort rendererPort;
    private final ContentHashPort contentHashPort;
    private final EvidenceBlobStoragePort blobStorage;
    private final DocumentAssetPort documentAssetPort;
    private final UserProgramAssignmentRepositoryPort assignmentRepository;
    private final ProcessFreezeGuard freezeGuard;

    public GenerateStageDeliverableDocumentService(
            ProcessQueryPort processQueryPort,
            MethodologicalStagePort stagePort,
            ImprovementPlanItemPort planItemPort,
            IndicatorAnalysisPort analysisPort,
            EvaluationMetricsPort evaluationMetricsPort,
            DraftingDocumentRendererPort rendererPort,
            ContentHashPort contentHashPort,
            EvidenceBlobStoragePort blobStorage,
            DocumentAssetPort documentAssetPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        this.processQueryPort = processQueryPort;
        this.stagePort = stagePort;
        this.planItemPort = planItemPort;
        this.analysisPort = analysisPort;
        this.evaluationMetricsPort = evaluationMetricsPort;
        this.rendererPort = rendererPort;
        this.contentHashPort = contentHashPort;
        this.blobStorage = blobStorage;
        this.documentAssetPort = documentAssetPort;
        this.assignmentRepository = assignmentRepository;
        this.freezeGuard = freezeGuard;
    }

    @Override
    public GeneratedStageDeliverable generate(
            UUID processId,
            UUID stageId,
            UUID deliverableId,
            UUID requesterId,
            List<String> roles,
            List<UUID> jwtProgramScope) {
        AccreditationProcess process = processQueryPort.findDetailById(processId)
                .orElseThrow(() -> new ProcessNotFoundException(processId));
        EvaluationAccessGuard.assertProcessVisible(
                processId,
                process.getCareerId(),
                requesterId,
                roles,
                jwtProgramScope,
                assignmentRepository);

        MethodologicalStage stage = stagePort.findByIdAndProcessId(stageId, processId)
                .orElseThrow(() -> new StageNotFoundException("Etapa no encontrada en el proceso."));

        StageDeliverable deliverable = stagePort.findDeliverableById(deliverableId)
                .filter(item -> item.getStageId().equals(stageId))
                .orElseThrow(() -> new StageNotFoundException("Entregable no encontrado en la etapa."));

        boolean e4 = StageDeliverableCode.isE4(deliverable.getDeliverableCode());
        boolean e7 = StageDeliverableCode.isE7(deliverable.getDeliverableCode());
        boolean draftingE4 = stage.getCode() == MethodologicalStageCode.DRAFTING && e4;
        boolean postE7 = (stage.getCode() == MethodologicalStageCode.POST_ACCREDITATION
                || process.getOperationalMode() == com.umss.sigesa.domain.model.OperationalMode.IMPROVEMENT_MONITORING)
                && e7;
        if (!draftingE4 && !postE7) {
            throw new InvalidDeliverableStageException(
                    "Solo se pueden generar borradores E4 en DRAFTING o E7 en POST_ACCREDITATION/monitoreo.");
        }
        if (deliverable.getApprovalStatus() == StageDeliverableApprovalStatus.APPROVED) {
            throw new DeliverableImmutableException(
                    "No se puede regenerar un entregable ya aprobado.");
        }
        freezeGuard.assertGenerateWritable(processId, deliverable.getDeliverableCode());

        String title = titleFor(deliverable.getDeliverableCode());
        List<String> paragraphs = buildParagraphs(process, deliverable.getDeliverableCode());
        byte[] pdf = rendererPort.render(title, paragraphs);
        String hash = contentHashPort.sha256Hex(pdf);

        DocumentAsset asset = documentAssetPort.findByHashInProcess(processId, hash)
                .orElseGet(() -> persistNewAsset(processId, deliverableId, title, hash, pdf, requesterId));

        stagePort.updateDeliverableDocumentAsset(deliverableId, asset.getId());
        return new GeneratedStageDeliverable(deliverableId, asset.getId(), asset.getFolioCode(), title);
    }

    private DocumentAsset persistNewAsset(
            UUID processId,
            UUID deliverableId,
            String title,
            String hash,
            byte[] pdf,
            UUID requesterId) {
        String storageKey = blobStorage.store(deliverableId, 1, pdf, "e4-borrador.pdf");
        DocumentAsset created = DocumentAsset.create(
                processId,
                title,
                storageKey,
                hash,
                requesterId,
                LocalDateTime.now());
        return documentAssetPort.save(created);
    }

    private List<String> buildParagraphs(AccreditationProcess process, StageDeliverableCode code) {
        return switch (code) {
            case SELF_ASSESSMENT_REPORT -> selfAssessmentParagraphs(process.getId());
            case OFFICIAL_DATA_FORM -> officialFormParagraphs(process);
            case IMPROVEMENT_PLAN, IMPROVEMENT_PROGRESS_REPORT -> improvementPlanParagraphs(process.getId());
            case ACADEMIC_DEV_PLAN -> academicDevParagraphs(process.getId());
            default -> List.of("Sin contenido para " + code.name());
        };
    }

    private List<String> selfAssessmentParagraphs(UUID processId) {
        List<String> paragraphs = new ArrayList<>();
        long total = evaluationMetricsPort.countIndicatorsByProcessId(processId);
        long withEvidence = evaluationMetricsPort.countIndicatorsWithEvidenceByProcessId(processId);
        long fodaComplete = evaluationMetricsPort.countFodaCompleteByProcessId(processId);
        paragraphs.add("Métricas del proceso");
        paragraphs.add(String.format("Indicadores: %d · Con evidencia: %d · FODA completo: %d",
                total, withEvidence, fodaComplete));
        paragraphs.add("Síntesis FODA por indicador");
        List<IndicatorAnalysis> analyses = analysisPort.findByProcessId(processId);
        if (analyses.isEmpty()) {
            paragraphs.add("Aún no hay análisis FODA registrados.");
        } else {
            for (IndicatorAnalysis analysis : analyses) {
                paragraphs.add(String.format(
                        "Indicador %s · Cuadrante: %s · Cumplimiento: %s · %s",
                        analysis.getIndicatorId(),
                        analysis.getFodaQuadrant() != null ? analysis.getFodaQuadrant().name() : "—",
                        analysis.getCompliance() != null ? analysis.getCompliance().name() : "—",
                        analysis.getSynthesis() != null ? analysis.getSynthesis() : ""));
            }
        }
        return paragraphs;
    }

    private static List<String> officialFormParagraphs(AccreditationProcess process) {
        return List.of(
                "Ficha de datos oficiales del proceso (borrador institucional).",
                "Este documento no es el formulario CNACU oficial.",
                "Proceso: " + process.getId(),
                "Carrera (careerId): " + process.getCareerId(),
                "Plantilla (templateId): " + process.getTemplateId(),
                "Estado: " + process.getStatus(),
                "Fecha de inicio: " + (process.getStartDate() != null
                        ? process.getStartDate().toLocalDate().format(DATE_FORMAT)
                        : "—"));
    }

    private List<String> improvementPlanParagraphs(UUID processId) {
        List<String> paragraphs = new ArrayList<>();
        paragraphs.add("Plan de Mejoras del proceso");
        List<ImprovementPlanItem> items = planItemPort.findByProcessId(processId);
        if (items.isEmpty()) {
            paragraphs.add("No hay ítems registrados en el plan de mejoras.");
            return paragraphs;
        }
        int index = 1;
        for (ImprovementPlanItem item : items) {
            paragraphs.add(String.format(
                    "%d. Objetivo: %s · Actividad: %s · Responsable: %s · Plazo: %s · Avance: %d%%",
                    index++,
                    item.getObjective(),
                    item.getActivity() != null ? item.getActivity() : "—",
                    item.getResponsibleId() != null ? item.getResponsibleId().toString() : "—",
                    item.getDeadline() != null ? item.getDeadline().format(DATE_FORMAT) : "—",
                    item.getProgress()));
        }
        return paragraphs;
    }

    private List<String> academicDevParagraphs(UUID processId) {
        List<String> paragraphs = new ArrayList<>();
        paragraphs.add("Plan de desarrollo académico a partir de fortalezas y oportunidades FODA.");
        List<IndicatorAnalysis> analyses = analysisPort.findByProcessId(processId).stream()
                .filter(analysis -> analysis.getFodaQuadrant() == FodaQuadrant.STRENGTH
                        || analysis.getFodaQuadrant() == FodaQuadrant.OPPORTUNITY)
                .toList();
        if (analyses.isEmpty()) {
            paragraphs.add("No hay análisis FODA en cuadrantes STRENGTH u OPPORTUNITY.");
            return paragraphs;
        }
        for (IndicatorAnalysis analysis : analyses) {
            paragraphs.add(String.format(
                    "Indicador %s · %s · %s",
                    analysis.getIndicatorId(),
                    analysis.getFodaQuadrant().name(),
                    analysis.getSynthesis() != null ? analysis.getSynthesis() : ""));
        }
        return paragraphs;
    }

    static String titleFor(StageDeliverableCode code) {
        return switch (code) {
            case SELF_ASSESSMENT_REPORT -> "Informe de Autoevaluación (borrador institucional)";
            case OFFICIAL_DATA_FORM -> "Formulario de Datos Oficiales (borrador institucional)";
            case IMPROVEMENT_PLAN -> "Plan de Mejoras (borrador institucional)";
            case ACADEMIC_DEV_PLAN -> "Plan de Desarrollo Académico (borrador institucional)";
            case IMPROVEMENT_PROGRESS_REPORT -> "Informe de avance del Plan de Mejoras (borrador institucional)";
            default -> "Borrador " + code.name();
        };
    }
}
