import { useQueryClient } from '@tanstack/react-query';
import {
  getGetEvaluationMetricsQueryKey,
  getGetIndicatorAnalysisQueryKey,
  useGetIndicatorAnalysis,
  useUpsertIndicatorAnalysis,
} from '../../../api/endpoints/evaluation/evaluation';
import { isApiError } from '../../../lib/api/apiError';
import {
  IndicatorFodaForm,
  type ComplianceValue,
  type FodaQuadrantValue,
  type IndicatorFodaValues,
} from './IndicatorFodaForm';

export type IndicatorFodaSectionProps = {
  processId: string;
  indicatorId: string;
  canEdit: boolean;
  enabled: boolean;
};

function isFodaQuadrant(value: string | undefined): value is FodaQuadrantValue {
  return value === 'STRENGTH' || value === 'WEAKNESS' || value === 'OPPORTUNITY' || value === 'THREAT';
}

function isCompliance(value: string | undefined): value is ComplianceValue {
  return value === 'COMPLIES' || value === 'PARTIAL' || value === 'DOES_NOT_COMPLY';
}

export function IndicatorFodaSection({
  processId,
  indicatorId,
  canEdit,
  enabled,
}: IndicatorFodaSectionProps) {
  const queryClient = useQueryClient();
  const analysisQuery = useGetIndicatorAnalysis(indicatorId, {
    query: {
      enabled: enabled && Boolean(indicatorId),
      retry: false,
    },
  });
  const upsertMutation = useUpsertIndicatorAnalysis({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getGetIndicatorAnalysisQueryKey(indicatorId) });
        void queryClient.invalidateQueries({ queryKey: getGetEvaluationMetricsQueryKey(processId) });
      },
    },
  });

  const missingAnalysis =
    isApiError(analysisQuery.error) && analysisQuery.error.code === 'ANALYSIS_NOT_FOUND';
  const analysis = analysisQuery.data?.status === 200 ? analysisQuery.data.data : undefined;
  const initialValues: Partial<IndicatorFodaValues> | undefined = analysis
    ? {
        fodaQuadrant: isFodaQuadrant(analysis.fodaQuadrant) ? analysis.fodaQuadrant : undefined,
        compliance: isCompliance(analysis.compliance) ? analysis.compliance : undefined,
        synthesis: analysis.synthesis,
      }
    : undefined;

  const handleSave = (values: IndicatorFodaValues) => {
    upsertMutation.mutate({
      indicatorId,
      data: {
        fodaQuadrant: values.fodaQuadrant,
        compliance: values.compliance,
        synthesis: values.synthesis,
      },
    });
  };

  return (
    <IndicatorFodaForm
      key={`${indicatorId}-${analysis?.updatedAt ?? 'empty'}`}
      canEdit={canEdit}
      initialValues={initialValues}
      isLoading={analysisQuery.isLoading}
      isSaving={upsertMutation.isPending}
      errorMessage={
        upsertMutation.error
          ? isApiError(upsertMutation.error)
            ? upsertMutation.error.message
            : 'No se pudo guardar el FODA.'
          : analysisQuery.isError && !missingAnalysis
            ? isApiError(analysisQuery.error)
              ? analysisQuery.error.message
              : 'No se pudo cargar el FODA.'
            : undefined
      }
      successMessage={upsertMutation.isSuccess ? 'FODA guardado.' : undefined}
      onSave={handleSave}
    />
  );
}
