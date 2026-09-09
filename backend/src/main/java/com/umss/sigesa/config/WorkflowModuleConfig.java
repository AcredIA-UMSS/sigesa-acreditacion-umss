package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.ApproveStageDeliverableUseCase;
import com.umss.sigesa.application.port.in.ApproveStageUseCase;
import com.umss.sigesa.application.port.in.CloseLevel1UseCase;
import com.umss.sigesa.application.port.in.EvaluateStageGateUseCase;
import com.umss.sigesa.application.port.in.ListProcessStagesUseCase;
import com.umss.sigesa.application.port.in.ObserveStageUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.in.SubmitStageForReviewUseCase;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.service.workflow.ApproveIndicatorService;
import com.umss.sigesa.application.service.workflow.ApproveStageDeliverableService;
import com.umss.sigesa.application.service.workflow.ApproveStageService;
import com.umss.sigesa.application.service.workflow.CloseLevel1Service;
import com.umss.sigesa.application.service.workflow.EvaluateStageGateService;
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
    StageGateEvaluator stageGateEvaluator(
            EvaluationMetricsPort evaluationMetricsPort,
            MethodologicalStagePort stagePort) {
        return new StageGateEvaluator(evaluationMetricsPort, stagePort);
    }

    @Bean
    ListProcessStagesUseCase listProcessStagesUseCase(
            ProcessQueryPort processQueryPort,
            MethodologicalStagePort stagePort) {
        return new ListProcessStagesService(processQueryPort, stagePort);
    }

    @Bean
    SubmitStageForReviewUseCase submitStageForReviewUseCase(MethodologicalStagePort stagePort) {
        return new SubmitStageForReviewService(stagePort);
    }

    @Bean
    ApproveStageUseCase approveStageUseCase(
            MethodologicalStagePort stagePort,
            StageGateEvaluator stageGateEvaluator) {
        return new ApproveStageService(stagePort, stageGateEvaluator);
    }

    @Bean
    ObserveStageUseCase observeStageUseCase(MethodologicalStagePort stagePort) {
        return new ObserveStageService(stagePort);
    }

    @Bean
    EvaluateStageGateUseCase evaluateStageGateUseCase(
            MethodologicalStagePort stagePort,
            StageGateEvaluator stageGateEvaluator) {
        return new EvaluateStageGateService(stagePort, stageGateEvaluator);
    }

    @Bean
    ApproveStageDeliverableUseCase approveStageDeliverableUseCase(MethodologicalStagePort stagePort) {
        return new ApproveStageDeliverableService(stagePort);
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
