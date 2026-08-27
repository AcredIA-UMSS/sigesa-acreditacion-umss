package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.ApproveIndicatorUseCase;
import com.umss.sigesa.application.port.in.ApproveSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectIndicatorUseCase;
import com.umss.sigesa.application.port.in.RejectSubphaseIndicatorUseCase;
import com.umss.sigesa.application.port.out.IndicatorRepositoryPort;
import com.umss.sigesa.application.port.out.NotificationOutboxPort;
import com.umss.sigesa.application.port.out.SubphaseEvidenceQueryPort;
import com.umss.sigesa.application.port.out.SubphaseObservationPort;
import com.umss.sigesa.application.port.out.SubphaseQueryPort;
import com.umss.sigesa.application.port.out.PhaseWorkflowPort;
import com.umss.sigesa.application.port.out.SubphaseWorkflowPort;
import com.umss.sigesa.application.service.workflow.ApproveIndicatorService;
import com.umss.sigesa.application.service.workflow.ApproveSubphaseIndicatorService;
import com.umss.sigesa.application.service.workflow.IndicatorTransitionHelper;
import com.umss.sigesa.application.service.workflow.RejectIndicatorService;
import com.umss.sigesa.application.service.workflow.SubphaseTransitionHelper;
import com.umss.sigesa.application.port.in.ClosePhaseUseCase;
import com.umss.sigesa.application.service.workflow.ClosePhaseService;
import com.umss.sigesa.application.service.workflow.RejectSubphaseIndicatorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WorkflowModuleConfig {

    @Bean
    IndicatorTransitionHelper indicatorTransitionHelper(IndicatorRepositoryPort indicatorRepository) {
        return new IndicatorTransitionHelper(indicatorRepository);
    }

    @Bean
    SubphaseTransitionHelper subphaseTransitionHelper(SubphaseWorkflowPort subphaseWorkflowPort) {
        return new SubphaseTransitionHelper(subphaseWorkflowPort);
    }

    @Bean
    ClosePhaseUseCase closePhaseUseCase(PhaseWorkflowPort phaseWorkflowPort,
                                        NotificationOutboxPort notificationOutbox) {
        return new ClosePhaseService(phaseWorkflowPort, notificationOutbox);
    }

    @Bean
    RejectSubphaseIndicatorUseCase rejectSubphaseIndicatorUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            SubphaseObservationPort observationPort,
            SubphaseTransitionHelper subphaseTransitionHelper,
            NotificationOutboxPort notificationOutbox) {
        return new RejectSubphaseIndicatorService(
                subphaseQueryPort, evidenceQueryPort, observationPort, subphaseTransitionHelper, notificationOutbox);
    }

    @Bean
    ApproveSubphaseIndicatorUseCase approveSubphaseIndicatorUseCase(
            SubphaseQueryPort subphaseQueryPort,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            SubphaseObservationPort observationPort,
            SubphaseTransitionHelper subphaseTransitionHelper,
            NotificationOutboxPort notificationOutbox) {
        return new ApproveSubphaseIndicatorService(
                subphaseQueryPort, evidenceQueryPort, observationPort, subphaseTransitionHelper, notificationOutbox);
    }

    @Bean
    RejectIndicatorUseCase rejectIndicatorUseCase(
            IndicatorRepositoryPort indicatorRepository,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            SubphaseObservationPort observationPort,
            IndicatorTransitionHelper transitionHelper,
            NotificationOutboxPort notificationOutbox) {
        return new RejectIndicatorService(
                indicatorRepository, evidenceQueryPort, observationPort, transitionHelper, notificationOutbox);
    }

    @Bean
    ApproveIndicatorUseCase approveIndicatorUseCase(
            IndicatorRepositoryPort indicatorRepository,
            SubphaseEvidenceQueryPort evidenceQueryPort,
            SubphaseObservationPort observationPort,
            IndicatorTransitionHelper transitionHelper,
            NotificationOutboxPort notificationOutbox) {
        return new ApproveIndicatorService(
                indicatorRepository, evidenceQueryPort, observationPort, transitionHelper, notificationOutbox);
    }
}
