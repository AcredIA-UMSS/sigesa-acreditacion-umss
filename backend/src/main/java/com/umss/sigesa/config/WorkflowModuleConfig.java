package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorEvidenceQueryPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorObservationPort;
import com.umss.sigesa.application.port.out.NormativeIndicatorWorkflowPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.service.workflow.ApproveIndicatorService;
import com.umss.sigesa.application.service.workflow.IndicatorTransitionHelper;
import com.umss.sigesa.application.service.workflow.RejectIndicatorService;
import com.umss.sigesa.application.port.in.CloseLevel1UseCase;
import com.umss.sigesa.application.service.workflow.CloseLevel1Service;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowModuleConfig {

    @Bean
    IndicatorTransitionHelper indicatorTransitionHelper(IndicatorRepositoryPort indicatorRepository) {
        return new IndicatorTransitionHelper(indicatorRepository);
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
