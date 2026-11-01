export type EvaluationMetricsView = {
  totalIndicators: number;
  indicatorsWithEvidence: number;
  evidenceCompletenessRatio: number;
  fodaComplete: number;
  fodaCompletenessRatio: number;
  primarySurveyBatchCount: number;
};

export type EvaluationMetricsPanelProps = {
  metrics?: EvaluationMetricsView;
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  onRefresh: () => void;
};

function formatRatio(ratio: number): string {
  return `${Math.round(ratio * 100)}%`;
}

export function EvaluationMetricsPanel({
  metrics,
  isLoading,
  isError,
  errorMessage,
  onRefresh,
}: EvaluationMetricsPanelProps) {
  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <div className="mb-4 flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-heading-lg font-semibold text-primary-800">
            Completitud de evaluación
          </h2>
          <p className="mt-1 text-body-md text-gray-600">
            Evidencias (E2), análisis FODA (E3) y lotes de encuesta primaria.
          </p>
        </div>
        <button
          type="button"
          onClick={onRefresh}
          className="text-body-md font-medium text-primary-600 hover:text-primary-800"
        >
          Actualizar métricas
        </button>
      </div>

      {isLoading && (
        <p className="text-body-md text-gray-600">Cargando métricas…</p>
      )}

      {isError && (
        <p className="text-body-md text-danger">{errorMessage ?? 'No se pudieron cargar las métricas.'}</p>
      )}

      {!isLoading && !isError && metrics && (
        <dl className="grid gap-3 sm:grid-cols-3">
          <div className="rounded-xl border border-primary-100 bg-primary-50 p-4">
            <dt className="text-label-md font-medium uppercase text-primary-700">Evidencias</dt>
            <dd className="mt-1 text-heading-md font-semibold text-primary-800">
              {metrics.indicatorsWithEvidence}/{metrics.totalIndicators}
            </dd>
            <dd className="text-body-md text-gray-600">
              {formatRatio(metrics.evidenceCompletenessRatio)}
            </dd>
          </div>
          <div className="rounded-xl border border-primary-100 bg-primary-50 p-4">
            <dt className="text-label-md font-medium uppercase text-primary-700">FODA</dt>
            <dd className="mt-1 text-heading-md font-semibold text-primary-800">
              {metrics.fodaComplete}/{metrics.totalIndicators}
            </dd>
            <dd className="text-body-md text-gray-600">
              {formatRatio(metrics.fodaCompletenessRatio)}
            </dd>
          </div>
          <div className="rounded-xl border border-primary-100 bg-primary-50 p-4">
            <dt className="text-label-md font-medium uppercase text-primary-700">Encuestas</dt>
            <dd className="mt-1 text-heading-md font-semibold text-primary-800">
              {metrics.primarySurveyBatchCount}
            </dd>
            <dd className="text-body-md text-gray-600">lotes registrados</dd>
          </div>
        </dl>
      )}
    </section>
  );
}
