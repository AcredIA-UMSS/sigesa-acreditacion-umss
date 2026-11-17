import { useGetEvaluationMetrics } from '../../../api/endpoints/evaluation/evaluation';
import { isApiError } from '../../../lib/api/apiError';
import { EvaluationMetricsPanel } from './EvaluationMetricsPanel';

interface EvaluationMetricsPanelContainerProps {
  processId: string;
}

export function EvaluationMetricsPanelContainer({ processId }: EvaluationMetricsPanelContainerProps) {
  const query = useGetEvaluationMetrics(processId, {
    query: { enabled: Boolean(processId) },
  });
  const metrics = query.data?.status === 200 ? query.data.data : undefined;

  return (
    <EvaluationMetricsPanel
      metrics={
        metrics
          ? {
              totalIndicators: metrics.totalIndicators ?? 0,
              indicatorsWithEvidence: metrics.indicatorsWithEvidence ?? 0,
              evidenceCompletenessRatio: metrics.evidenceCompletenessRatio ?? 0,
              fodaComplete: metrics.fodaComplete ?? 0,
              fodaCompletenessRatio: metrics.fodaCompletenessRatio ?? 0,
              primarySurveyBatchCount: metrics.primarySurveyBatchCount ?? 0,
            }
          : undefined
      }
      isLoading={query.isLoading}
      isError={query.isError}
      errorMessage={isApiError(query.error) ? query.error.message : undefined}
      onRefresh={() => {
        void query.refetch();
      }}
    />
  );
}
