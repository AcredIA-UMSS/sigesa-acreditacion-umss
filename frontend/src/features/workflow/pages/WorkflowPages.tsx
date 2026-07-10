import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { CheckCircle2, Loader2, Upload, XCircle } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { useListIndicators, useApproveIndicator, useRejectIndicator } from '../../../api/endpoints/indicator-controller/indicator-controller';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useAuth } from '../../../lib/auth/useAuth';
import type { IndicatorSummaryResponse } from '../../../api/model';

const REVIEWABLE_STATES = new Set(['SUBIDO', 'SUBSANADO']);

export function WorkflowReviewPage() {
  const { session } = useAuth();
  const { data, isLoading, refetch, isFetching } = useListIndicators();
  const approveMutation = useApproveIndicator();
  const rejectMutation = useRejectIndicator();
  const [rejectingId, setRejectingId] = useState<string | null>(null);
  const [justification, setJustification] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const indicators = useMemo(
    () =>
      (data?.data ?? []).filter((item: IndicatorSummaryResponse) =>
        REVIEWABLE_STATES.has(item.currentState ?? ''),
      ),
    [data?.data],
  );

  const handleApprove = async (indicatorId: string) => {
    setActionError(null);
    try {
      await approveMutation.mutateAsync({ indicatorId });
      await refetch();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  };

  const handleReject = async (indicatorId: string) => {
    setActionError(null);
    try {
      await rejectMutation.mutateAsync({ indicatorId, data: { justification } });
      setRejectingId(null);
      setJustification('');
      await refetch();
    } catch (error) {
      setActionError(getApiErrorMessage(error));
    }
  };

  if (session?.role !== 'TD') {
    return null;
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="dashboard" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="border-b border-gray-200 bg-body px-8 py-4">
          <h1 className="text-heading-lg font-bold text-primary-800">Bandeja de Revisión Técnica</h1>
          <p className="text-body-md text-gray-600">Indicadores en estado SUBIDO o SUBSANADO listos para validación.</p>
        </header>

        <main className="flex-1 overflow-y-auto p-8 space-y-4">
          {actionError && (
            <div className="rounded-xl border border-danger/30 bg-danger/5 px-4 py-3 text-body-md text-danger">
              {actionError}
            </div>
          )}

          {isLoading || isFetching ? (
            <div className="flex items-center gap-2 text-gray-500">
              <Loader2 className="animate-spin" size={20} />
              Cargando indicadores...
            </div>
          ) : indicators.length === 0 ? (
            <p className="text-body-md text-gray-500">No hay indicadores pendientes de revisión.</p>
          ) : (
            indicators.map((indicator: IndicatorSummaryResponse) => (
              <div
                key={indicator.id}
                className="rounded-2xl border border-primary-200/40 bg-body p-6 shadow-sm"
              >
                <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                  <div>
                    <p className="text-label-md font-semibold text-secondary-600">{indicator.code}</p>
                    <h2 className="text-heading-sm font-bold text-primary-800">{indicator.title}</h2>
                    <p className="mt-1 text-body-md text-gray-600">
                      Fase {indicator.phaseId} · Estado: {indicator.currentState}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => handleApprove(indicator.id ?? '')}
                      disabled={approveMutation.isPending}
                      className="inline-flex items-center gap-2 rounded-xl bg-success px-4 py-2 text-label-md font-semibold text-body hover:opacity-90 disabled:opacity-50"
                    >
                      <CheckCircle2 size={16} />
                      Aprobar
                    </button>
                    <button
                      type="button"
                      onClick={() => setRejectingId(indicator.id ?? null)}
                      className="inline-flex items-center gap-2 rounded-xl bg-secondary px-4 py-2 text-label-md font-semibold text-body hover:bg-secondary-600"
                    >
                      <XCircle size={16} />
                      Observar
                    </button>
                  </div>
                </div>

                {rejectingId === indicator.id && (
                  <div className="mt-4 space-y-3 border-t border-gray-100 pt-4">
                    <label className="block text-label-md font-medium text-gray-700">
                      Justificación (mínimo 20 caracteres)
                    </label>
                    <textarea
                      value={justification}
                      onChange={(event) => setJustification(event.target.value)}
                      rows={3}
                      className="w-full rounded-xl border border-gray-300 px-3 py-2 text-body-md focus:border-primary-500 focus:outline-none"
                    />
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => handleReject(indicator.id ?? '')}
                        disabled={justification.trim().length < 20 || rejectMutation.isPending}
                        className="rounded-xl bg-primary-600 px-4 py-2 text-label-md font-semibold text-body disabled:opacity-50"
                      >
                        Confirmar observación
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setRejectingId(null);
                          setJustification('');
                        }}
                        className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold text-gray-700"
                      >
                        Cancelar
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))
          )}
        </main>
      </div>
    </div>
  );
}

export function IndicatorsCatalogPage() {
  const { data, isLoading } = useListIndicators();
  const { session } = useAuth();
  const indicators = data?.data ?? [];

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="dashboard" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div>
            <h1 className="text-heading-lg font-bold text-primary-800">Catálogo de Indicadores</h1>
            <p className="text-body-md text-gray-600">Indicadores del proceso activo con estado actual.</p>
          </div>
          {session?.role === 'CC' && (
            <Link
              to="/evidencias/subir"
              className="inline-flex items-center gap-2 rounded-xl bg-primary-600 px-4 py-2 text-label-md font-semibold text-body hover:bg-primary-700"
            >
              <Upload size={16} />
              Cargar evidencia
            </Link>
          )}
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          {isLoading ? (
            <div className="flex items-center gap-2 text-gray-500">
              <Loader2 className="animate-spin" size={20} />
              Cargando indicadores...
            </div>
          ) : (
            <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
              {indicators.map((indicator: IndicatorSummaryResponse) => (
                <article
                  key={indicator.id}
                  className="rounded-2xl border border-primary-200/40 bg-body p-5 shadow-sm"
                >
                  <p className="text-label-md font-semibold text-secondary-600">{indicator.code}</p>
                  <h2 className="text-heading-sm font-bold text-primary-800">{indicator.title}</h2>
                  <p className="mt-2 text-body-md text-gray-600">Fase {indicator.phaseId}</p>
                  <span className="mt-3 inline-flex rounded-full bg-primary-50 px-2.5 py-0.5 text-xs font-semibold text-primary-700">
                    {indicator.currentState}
                  </span>
                  {session?.role === 'CC' && (
                    <Link
                      to={`/evidencias/${indicator.id}/subsanar`}
                      className="mt-4 inline-flex text-label-md font-semibold text-primary-600 hover:text-primary-800"
                    >
                      Gestionar evidencia →
                    </Link>
                  )}
                </article>
              ))}
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
