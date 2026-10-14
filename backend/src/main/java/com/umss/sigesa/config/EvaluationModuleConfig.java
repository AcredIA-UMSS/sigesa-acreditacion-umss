package com.umss.sigesa.config;

import com.umss.sigesa.application.port.in.GetEvaluationMetricsUseCase;
import com.umss.sigesa.application.port.in.GetIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.in.ListPrimarySurveyBatchesUseCase;
import com.umss.sigesa.application.port.in.RegisterPrimarySurveyBatchUseCase;
import com.umss.sigesa.application.port.in.UpsertIndicatorAnalysisUseCase;
import com.umss.sigesa.application.port.out.DocumentAssetPort;
import com.umss.sigesa.application.port.out.EvaluationMetricsPort;
import com.umss.sigesa.application.port.out.IndicatorAnalysisPort;
import com.umss.sigesa.application.port.out.MethodologicalStagePort;
import com.umss.sigesa.application.port.out.NormativeHierarchyQueryPort;
import com.umss.sigesa.application.port.out.PrimarySurveyBatchPort;
import com.umss.sigesa.application.port.out.ProcessQueryPort;
import com.umss.sigesa.application.port.out.UserProgramAssignmentRepositoryPort;
import com.umss.sigesa.application.service.evaluation.GetEvaluationMetricsService;
import com.umss.sigesa.application.service.evaluation.GetIndicatorAnalysisService;
import com.umss.sigesa.application.service.evaluation.ListPrimarySurveyBatchesService;
import com.umss.sigesa.application.service.evaluation.RegisterPrimarySurveyBatchService;
import com.umss.sigesa.application.service.evaluation.UpsertIndicatorAnalysisService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EvaluationModuleConfig {

    @Bean
    UpsertIndicatorAnalysisUseCase upsertIndicatorAnalysisUseCase(
            NormativeHierarchyQueryPort hierarchyQueryPort,
            IndicatorAnalysisPort analysisPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new UpsertIndicatorAnalysisService(hierarchyQueryPort, analysisPort, assignmentRepository);
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
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new RegisterPrimarySurveyBatchService(
                processQueryPort, surveyBatchPort, documentAssetPort, assignmentRepository);
    }

    @Bean
    ListPrimarySurveyBatchesUseCase listPrimarySurveyBatchesUseCase(
            ProcessQueryPort processQueryPort,
            PrimarySurveyBatchPort surveyBatchPort,
            UserProgramAssignmentRepositoryPort assignmentRepository) {
        return new ListPrimarySurveyBatchesService(processQueryPort, surveyBatchPort, assignmentRepository);
    }
}
