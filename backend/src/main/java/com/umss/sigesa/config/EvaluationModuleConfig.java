package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.CreateDelegationUseCase;
import com.umss.sigesa.application.port.in.CreateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.in.DeleteDelegationUseCase;
import com.umss.sigesa.application.port.in.ListCaeCandidatesUseCase;
import com.umss.sigesa.application.port.in.ListDelegationsUseCase;
import com.umss.sigesa.application.port.in.GetEvaluationMetricsUseCase;
import com.umss.sigesa.application.port.in.GetIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.in.ImportImprovementPlanFromFodaUseCase;
import com.umss.sigesa.application.port.in.ListImprovementPlanUseCase;
import com.umss.sigesa.application.port.in.ListPrimarySurveyBatchesUseCase;
import com.umss.sigesa.application.port.in.RegisterPrimarySurveyBatchUseCase;
import com.umss.sigesa.application.port.in.UpdateImprovementPlanItemUseCase;
import com.umss.sigesa.application.port.in.UpsertIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.ImprovementPlanItemPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.IndicatorDelegationPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.port.out.UserRepositoryPort;
import com.umss.sigesa.application.service.evaluation.CaeDelegationGuard;
import com.umss.sigesa.application.service.evaluation.CreateDelegationService;
import com.umss.sigesa.application.service.evaluation.CreateImprovementPlanItemService;
import com.umss.sigesa.application.service.evaluation.DeleteDelegationService;
import com.umss.sigesa.application.service.evaluation.ListCaeCandidatesService;
import com.umss.sigesa.application.service.evaluation.ListDelegationsService;
import com.umss.sigesa.application.service.evaluation.GetEvaluationMetricsService;
import com.umss.sigesa.application.service.evaluation.GetIndicatorAnalysisService;
import com.umss.sigesa.application.service.evaluation.ImportImprovementPlanFromFodaService;
import com.umss.sigesa.application.service.evaluation.ListImprovementPlanService;
import com.umss.sigesa.application.service.evaluation.ListPrimarySurveyBatchesService;
import com.umss.sigesa.application.service.evaluation.RegisterPrimarySurveyBatchService;
import com.umss.sigesa.application.service.evaluation.UpdateImprovementPlanItemService;
import com.umss.sigesa.application.service.evaluation.UpsertIndicatorAnalysisService;
import com.umss.sigesa.application.service.workflow.ProcessFreezeGuard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluationModuleConfig {

    @Bean
    CaeDelegationGuard caeDelegationGuard(
            UserRepositoryPort userRepository,
            IndicatorDelegationPort delegationPort,
            NormativeHierarchyQueryPort hierarchyQueryPort) {
        return new CaeDelegationGuard(userRepository, delegationPort, hierarchyQueryPort);
    }

    @Bean
    UpsertIndicatorAnalysisUseCase upsertIndicatorAnalysisUseCase(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard,
            CaeDelegationGuard caeDelegationGuard) {
        return new UpsertIndicatorAnalysisService(
                hierarchyQueryPort, analysisPort, assignmentRepository, freezeGuard, caeDelegationGuard);
    }

    @Bean
    GetIndicatorAnalysisUseCase getIndicatorAnalysisUseCase(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetIndicatorAnalysisService(hierarchyQueryPort, analysisPort, assignmentRepository);
    }

    @Bean
    GetEvaluationMetricsUseCase getEvaluationMetricsUseCase(
            ProcessQueryPort processQueryPort,
            EvaluationMetricsPort evaluationMetricsPort,
            MethodologicalStagePort stagePort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new GetEvaluationMetricsService(
                processQueryPort, evaluationMetricsPort, stagePort, assignmentRepository);
    }

    @Bean
    RegisterPrimarySurveyBatchUseCase registerPrimarySurveyBatchUseCase(
            ProcessQueryPort processQueryPort,
            PrimarySurveyBatchPort surveyBatchPort,
            DocumentAssetPort documentAssetPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        return new RegisterPrimarySurveyBatchService(
                processQueryPort, surveyBatchPort, documentAssetPort, assignmentRepository, freezeGuard);
    }

    @Bean
    ListPrimarySurveyBatchesUseCase listPrimarySurveyBatchesUseCase(
            ProcessQueryPort processQueryPort,
            PrimarySurveyBatchPort surveyBatchPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListPrimarySurveyBatchesService(processQueryPort, surveyBatchPort, assignmentRepository);
    }

    @Bean
    ListImprovementPlanUseCase listImprovementPlanUseCase(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListImprovementPlanService(processQueryPort, planItemPort, assignmentRepository);
    }

    @Bean
    CreateImprovementPlanItemUseCase createImprovementPlanItemUseCase(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        return new CreateImprovementPlanItemService(
                processQueryPort, planItemPort, assignmentRepository, freezeGuard);
    }

    @Bean
    UpdateImprovementPlanItemUseCase updateImprovementPlanItemUseCase(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        return new UpdateImprovementPlanItemService(
                processQueryPort, planItemPort, assignmentRepository, freezeGuard);
    }

    @Bean
    ImportImprovementPlanFromFodaUseCase importImprovementPlanFromFodaUseCase(
            ProcessQueryPort processQueryPort,
            ImprovementPlanItemPort planItemPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository,
            ProcessFreezeGuard freezeGuard) {
        return new ImportImprovementPlanFromFodaService(
                processQueryPort, planItemPort, analysisPort, assignmentRepository, freezeGuard);
    }

    @Bean
    ListDelegationsUseCase listDelegationsUseCase(
            ProcessQueryPort processQueryPort,
            IndicatorDelegationPort delegationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListDelegationsService(processQueryPort, delegationPort, assignmentRepository);
    }

    @Bean
    CreateDelegationUseCase createDelegationUseCase(
            ProcessQueryPort processQueryPort,
            IndicatorDelegationPort delegationPort,
            UserRepositoryPort userRepository,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new CreateDelegationService(
                processQueryPort, delegationPort, userRepository, assignmentRepository);
    }

    @Bean
    DeleteDelegationUseCase deleteDelegationUseCase(
            ProcessQueryPort processQueryPort,
            IndicatorDelegationPort delegationPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new DeleteDelegationService(processQueryPort, delegationPort, assignmentRepository);
    }

    @Bean
    ListCaeCandidatesUseCase listCaeCandidatesUseCase(
            ProcessQueryPort processQueryPort,
            UserRepositoryPort userRepository,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListCaeCandidatesService(processQueryPort, userRepository, assignmentRepository);
    }
}
