package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.FreezeProcessUseCase;
import com.umss.sigesa.application.port.in.GetPeerRoomChecklistUseCase;
import com.umss.sigesa.application.port.in.GetProcessFreezeUseCase;
import com.umss.sigesa.application.port.in.LiftProcessFreezeUseCase;
import com.umss.sigesa.application.port.in.UpdatePeerRoomChecklistUseCase;
import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.ApproveStageDeliverableUseCase;
import com.umss.sigesa.application.port.in.ApproveStageUseCase;
import com.umss.sigesa.application.port.in.CloseLevel1UseCase;
import com.umss.sigesa.application.port.in.EvaluateStageGateUseCase;
import com.umss.sigesa.application.port.in.GenerateStageDeliverableDocumentUseCase;
import com.umss.sigesa.application.port.in.ListProcessStagesUseCase;
import com.umss.sigesa.application.port.in.ObserveStageUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.in.SubmitStageForReviewUseCase;
import com.umss.sigesa.application.port.out.ContentHashPort;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.DraftingDocumentRendererPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.EvidenceBlobStoragePort;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.PeerRoomChecklistPort;
import com.umss.sigesa.application.port.out.ProcessFreezePort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.workflow.FreezeProcessService;
import com.umss.sigesa.application.service.workflow.GetPeerRoomChecklistService;
import com.umss.sigesa.application.service.workflow.GetProcessFreezeService;
import com.umss.sigesa.application.service.workflow.LiftProcessFreezeService;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import com.umss.sigesa.application.service.workflow.UpdatePeerRoomChecklistService;
import com.umss.sigesa.application.service.workflow.ApproveIndicatorService;
import com.umss.sigesa.application.service.workflow.ApproveStageDeliverableService;
import com.umss.sigesa.application.service.workflow.ApproveStageService;
import com.umss.sigesa.application.service.workflow.CloseLevel1Service;
import com.umss.sigesa.application.service.workflow.EvaluateStageGateService;
import com.umss.sigesa.application.service.workflow.GenerateStageDeliverableDocumentService;
import com.umss.sigesa.application.service.workflow.IndicatorTransitionHelper;
import com.umss.sigesa.application.service.workflow.ListProcessStagesService;
import com.umss.sigesa.application.service.workflow.MethodologicalStageBootstrapper;
import com.umss.sigesa.application.service.workflow.ObserveStageService;
import com.umss.sigesa.application.service.workflow.RejectIndicatorService;
import com.umss.sigesa.application.service.workflow.StageGateEvaluator;
import com.umss.sigesa.application.service.workflow.SubmitStageForReviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowModuleConfig {

    @Bean
    IndicatorTransitionHelper indicatorTransitionHelper(IndicatorRepositoryPort indicatorRepository) {
        return new IndicatorTransitionHelper(indicatorRepository);
    }

    @Bean
    MethodologicalStageBootstrapper methodologicalStageBootstrapper(MethodologicalStagePort stagePort) {
        return new MethodologicalStageBootstrapper(stagePort);
    }

    @Bean
    ProcessFreezeGuard processFreezeGuard(ProcessFreezePort freezePort, ProcessQueryPort processQueryPort) {
        return new ProcessFreezeGuard(freezePort, processQueryPort);
    }

    @Bean
    StageGateEvaluator stageGateEvaluator(
            EvaluationMetricsPort evaluationMetricsPort,
            MethodologicalStagePort stagePort,
            ProcessFreezePort freezePort,
            PeerRoomChecklistPort checklistPort,
            ImprovementPlanItemPort planItemPort) {
        return new StageGateEvaluator(
                evaluationMetricsPort, stagePort, freezePort, checklistPort, planItemPort);
    }

    @Bean
    ListProcessStagesUseCase listProcessStagesUseCase(
            ProcessQueryPort processQueryPort,
            MethodologicalStagePort stagePort,
            MethodologicalStageBootstrapper stageBootstrapper) {
        return new ListProcessStagesService(processQueryPort, stagePort, stageBootstrapper);
    }

    @Bean
    SubmitStageForReviewUseCase submitStageForReviewUseCase(MethodologicalStagePort stagePort) {
        return new SubmitStageForReviewService(stagePort);
    }

    @Bean
    ApproveStageUseCase approveStageUseCase(
            MethodologicalStagePort stagePort,
            StageGateEvaluator stageGateEvaluator,
            MethodologicalStageBootstrapper stageBootstrapper) {
        return new ApproveStageService(stagePort, stageGateEvaluator, stageBootstrapper);
    }

    @Bean
    ObserveStageUseCase observeStageUseCase(MethodologicalStagePort stagePort) {
        return new ObserveStageService(stagePort);
    }

    @Bean
    EvaluateStageGateUseCase evaluateStageGateUseCase(
            MethodologicalStagePort stagePort,
            StageGateEvaluator stageGateEvaluator,
            MethodologicalStageBootstrapper stageBootstrapper) {
        return new EvaluateStageGateService(stagePort, stageGateEvaluator, stageBootstrapper);
    }

    @Bean
    ApproveStageDeliverableUseCase approveStageDeliverableUseCase(
            MethodologicalStagePort stagePort,
            PeerRoomChecklistPort checklistPort) {
        return new ApproveStageDeliverableService(stagePort, checklistPort);
    }

    @Bean
    GenerateStageDeliverableDocumentUseCase generateStageDeliverableDocumentUseCase(
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
        return new GenerateStageDeliverableDocumentService(
                processQueryPort,
                stagePort,
                planItemPort,
                analysisPort,
                evaluationMetricsPort,
                rendererPort,
                contentHashPort,
                blobStorage,
                documentAssetPort,
                assignmentRepository,
                freezeGuard);
    }

    @Bean
    FreezeProcessUseCase freezeProcessUseCase(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            PeerRoomChecklistPort checklistPort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new FreezeProcessService(
                processQueryPort, freezePort, checklistPort, stagePort, assignmentRepository);
    }

    @Bean
    LiftProcessFreezeUseCase liftProcessFreezeUseCase(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new LiftProcessFreezeService(processQueryPort, freezePort, stagePort, assignmentRepository);
    }

    @Bean
    GetProcessFreezeUseCase getProcessFreezeUseCase(
            ProcessQueryPort processQueryPort,
            ProcessFreezePort freezePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetProcessFreezeService(processQueryPort, freezePort, assignmentRepository);
    }

    @Bean
    GetPeerRoomChecklistUseCase getPeerRoomChecklistUseCase(
            ProcessQueryPort processQueryPort,
            PeerRoomChecklistPort checklistPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetPeerRoomChecklistService(processQueryPort, checklistPort, assignmentRepository);
    }

    @Bean
    UpdatePeerRoomChecklistUseCase updatePeerRoomChecklistUseCase(
            ProcessQueryPort processQueryPort,
            PeerRoomChecklistPort checklistPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new UpdatePeerRoomChecklistService(processQueryPort, checklistPort, assignmentRepository);
    }

    @Bean
    CloseLevel1UseCase closeLevel1UseCase(NormativeHierarchyQueryPort hierarchyQueryPort,
                                          NormativeIndicatorWorkflowPort workflowPort,
                                          NotificationOutboxPort notificationOutbox) {
        return new CloseLevel1Service(hierarchyQueryPort, workflowPort, notificationOutbox);
    }

    @Bean
    RejectIndicatorUseCase rejectIndicatorUseCase(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorWorkflowPort workflowPort,
            NotificationOutboxPort notificationOutbox) {
        return new RejectIndicatorService(
                hierarchyQueryPort, evidenceQueryPort, observationPort, workflowPort, notificationOutbox);
    }

    @Bean
    ApproveIndicatorUseCase approveIndicatorUseCase(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            NormativeIndicatorEvidenceQueryPort evidenceQueryPort,
            NormativeIndicatorObservationPort observationPort,
            NormativeIndicatorWorkflowPort workflowPort,
            NotificationOutboxPort notificationOutbox) {
        return new ApproveIndicatorService(
                hierarchyQueryPort, evidenceQueryPort, observationPort, workflowPort, notificationOutbox);
    }
}
