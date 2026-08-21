import { useState, useMemo } from 'react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { useListPending, useApprove, useReject } from '../../../api/endpoints/indicator-workflow/indicator-workflow';
import { Button } from '../../../components/ui/Button';
import { Alert } from '../../../components/ui/Alert';
import { FileText, RefreshCw, Layers, Eye } from 'lucide-react';
import { RejectIndicatorModal, type EvidenceItemInfo } from '../components/RejectIndicatorModal';
import { IndicatorReviewModal, type IndicatorReviewGroup } from '../components/IndicatorReviewModal';
import { EvidenceCopilotPanel } from '../../evidence/components/EvidenceCopilotPanel';

export function ProcessEvaluationPage() {
  const { data, isLoading, isError, refetch } = useListPending();
  const approveMutation = useApprove();
  const rejectMutation = useReject();

  const [selectedIndicator, setSelectedIndicator] = useState<IndicatorReviewGroup | null>(null);
  const [rejectingGroup, setRejectingGroup] = useState<IndicatorReviewGroup | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [tableSearchQuery, setTableSearchQuery] = useState<string>('');
  const [activeFilterStatus, setActiveFilterStatus] = useState<string>('ALL');
  const [currentPage, setCurrentPage] = useState<number>(1);
  const ITEMS_PER_PAGE = 15;

  // Group evidence items by indicatorId so each item in the list is an Indicator containing its set of evidences
  const filteredIndicators = useMemo<IndicatorReviewGroup[]>(() => {
    const rawItems = data?.data ?? [];
    const map = new Map<string, IndicatorReviewGroup>();

    for (const item of rawItems) {
      if (!item.indicatorId) continue;
      const id = item.indicatorId;
      const existing = map.get(id);

      const evInfo: EvidenceItemInfo = {
        evidenceId: item.evidenceId,
        versionNumber: item.versionNumber,
        contentHash: item.contentHash,
        description: item.description,
      };

      if (!existing) {
        map.set(id, {
          indicatorId: id,
          programId: item.programId ?? '—',
          criterionId: item.criterionId ?? '—',
          currentState: item.currentState ?? 'SUBIDO',
          evidences: evInfo.evidenceId || evInfo.description ? [evInfo] : [],
        });
      } else {
        if (evInfo.evidenceId || evInfo.description) {
          existing.evidences.push(evInfo);
        }
      }
    }

    const allGroups = Array.from(map.values());

    // Extract core keywords if input is a conversational sentence (e.g., "Mostrar indicadores de infraestructura")
    const cleanSearchQuery = tableSearchQuery
      .toLowerCase()
      .replace(/^(mostrar|buscar|filtrar|ver|listar|los|las|de|del|en|para|que|están|estan|un|una|mis)\s+/gi, '')
      .trim();

    return allGroups.filter((group) => {
      const matchesStatus = activeFilterStatus === 'ALL' || group.currentState === activeFilterStatus;
      if (!cleanSearchQuery) return matchesStatus;

      const tokens = cleanSearchQuery.split(/\s+/).filter((t) => t.length > 2);
      if (tokens.length === 0) return matchesStatus;

      const matchesAnyToken = tokens.some((token) => {
        return (
          group.indicatorId.toLowerCase().includes(token) ||
          group.programId.toLowerCase().includes(token) ||
          group.criterionId.toLowerCase().includes(token) ||
          group.currentState.toLowerCase().includes(token) ||
          group.evidences.some((e) => (e.description ?? '').toLowerCase().includes(token))
        );
      });

      return matchesStatus && matchesAnyToken;
    });
  }, [data, tableSearchQuery, activeFilterStatus]);

  // Paginate 15 items per page
  const totalPages = Math.ceil(filteredIndicators.length / ITEMS_PER_PAGE) || 1;
  const paginatedIndicators = useMemo(() => {
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    return filteredIndicators.slice(startIndex, startIndex + ITEMS_PER_PAGE);
  }, [filteredIndicators, currentPage]);

  const handleCopilotFilter = (query: string, status?: string) => {
    setTableSearchQuery(query);
    if (status) {
      setActiveFilterStatus(status);
    }
    setCurrentPage(1);
  };

  const handleApprove = (indicatorId: string) => {
    setErrorMsg(null);
    approveMutation.mutate(
      { indicatorId },
      {
        onSuccess: () => {
          setSelectedIndicator(null);
          refetch();
        },
        onError: (err: any) => {
          setErrorMsg(err?.response?.data?.message || 'Error al aprobar el indicador.');
        },
      }
    );
  };

  const handleRejectConfirm = (justification: string) => {
    if (!rejectingGroup) return;
    setErrorMsg(null);

    rejectMutation.mutate(
      { indicatorId: rejectingGroup.indicatorId, data: { justification } },
      {
        onSuccess: () => {
          setRejectingGroup(null);
          setSelectedIndicator(null);
          refetch();
        },
        onError: (err: any) => {
          setErrorMsg(err?.response?.data?.message || 'Error al rechazar el indicador.');
        },
      }
    );
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main className="flex-1 overflow-y-auto px-8 py-8">
        <div className="max-w-7xl mx-auto space-y-6">
          {/* Page Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-heading-xl font-bold tracking-tight text-primary-800">
                Bandeja de Evaluación de Indicadores (TD / JD)
              </h1>
              <p className="mt-1 text-body-lg text-gray-700">
                Selecciona un indicador para revisar el conjunto de evidencias adjuntas o interactúa con el copiloto IA en el panel lateral.
              </p>
            </div>
            <Button variant="ghost" onClick={() => refetch()} isLoading={isLoading}>
              <RefreshCw size={16} />
              Actualizar
            </Button>
          </div>

          {errorMsg && (
            <Alert variant="error" title="Error en la operación">
              {errorMsg}
            </Alert>
          )}

          {/* Two-Column Layout: Table on Left, Copilot Chat on Right */}
          <div className="grid grid-cols-1 gap-6 xl:grid-cols-3">
            <div className="space-y-6 xl:col-span-2">
              {/* Search & Filter Toolbar */}
              <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between bg-white p-4 rounded-xl border border-gray-200 shadow-sm">
                <div className="relative flex-1">
                  <input
                    type="text"
                    value={tableSearchQuery}
                    onChange={(e) => setTableSearchQuery(e.target.value)}
                    placeholder="Filtrar por texto, código, criterio o descripción..."
                    className="w-full rounded-lg border border-gray-300 pl-9 pr-3 py-2 text-sm focus:border-primary-500 focus:outline-none focus:ring-1 focus:ring-primary-500"
                  />
                  <span className="absolute left-3 top-2.5 text-gray-400">🔍</span>
                </div>
                <div className="flex items-center gap-1.5 overflow-x-auto text-xs">
                  {['ALL', 'SUBIDO', 'OBSERVADO', 'SUBSANADO', 'APROBADO'].map((status) => (
                    <button
                      key={status}
                      type="button"
                      onClick={() => setActiveFilterStatus(status)}
                      className={`rounded-full px-3 py-1 font-semibold transition-colors ${
                        activeFilterStatus === status
                          ? 'bg-primary-700 text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      {status}
                    </button>
                  ))}
                </div>
              </div>

              {isLoading && (
                <div className="rounded-2xl border border-gray-200 bg-white p-12 text-center shadow-sm">
                  <p className="text-body-md text-gray-600">Cargando indicadores pendientes de evaluación…</p>
                </div>
              )}

              {isError && !isLoading && (
                <Alert variant="error" title="Error de conexión">
                  No se pudieron cargar los indicadores pendientes de revisión técnica.
                </Alert>
              )}

              {!isLoading && !isError && filteredIndicators.length === 0 && (
                <div className="rounded-2xl border border-gray-200 bg-white p-12 text-center shadow-sm">
                  <p className="text-body-lg text-gray-600">
                    {tableSearchQuery || activeFilterStatus !== 'ALL'
                      ? 'No hay indicadores que coincidan con el filtro seleccionado.'
                      : 'No hay indicadores pendientes de revisión técnica.'}
                  </p>
                </div>
              )}

              {!isLoading && !isError && filteredIndicators.length > 0 && (
                <div className="bg-white rounded-2xl border border-primary-100 overflow-hidden shadow-sm flex flex-col justify-between">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-gray-50/70 text-label-sm text-primary-700 border-b border-primary-100">
                        <th className="p-4 font-semibold">Indicador</th>
                        <th className="p-4 font-semibold">Programa / Criterio</th>
                        <th className="p-4 font-semibold">Evidencias</th>
                        <th className="p-4 font-semibold">Estado</th>
                        <th className="p-4 font-semibold text-right">Acción</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-primary-100 text-body-sm text-gray-700">
                      {paginatedIndicators.map((group) => (
                        <tr key={group.indicatorId} className="hover:bg-primary-50/10 transition-colors">
                          <td className="p-4 font-semibold text-primary-900">
                            <div className="flex items-center gap-2">
                              <Layers size={18} className="text-primary-600 shrink-0" />
                              <div>
                                <span className="block font-bold">Indicador {group.indicatorId.substring(0, 8)}...</span>
                                <span className="text-xs text-gray-500 font-mono font-normal">ID: {group.indicatorId}</span>
                              </div>
                            </div>
                          </td>

                          <td className="p-4">
                            <div className="space-y-1">
                              <span className="text-xs bg-gray-100 text-gray-700 px-2 py-0.5 rounded font-mono block w-fit">
                                Prog: {group.programId.substring(0, 8)}
                              </span>
                              <span className="text-xs text-gray-500 block font-mono">
                                Crit: {group.criterionId.substring(0, 8)}
                              </span>
                            </div>
                          </td>

                          <td className="p-4">
                            <div className="flex items-center gap-2">
                              <FileText size={16} className="text-secondary shrink-0" />
                              <span className="font-semibold text-gray-800">
                                {group.evidences.length} {group.evidences.length === 1 ? 'Evidencia' : 'Evidencias'}
                              </span>
                            </div>
                          </td>

                          <td className="p-4">
                            <span
                              className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                                group.currentState === 'SUBSANADO'
                                  ? 'bg-warning/10 text-warning'
                                  : group.currentState === 'APROBADO'
                                  ? 'bg-success/10 text-success'
                                  : 'bg-info/10 text-info'
                              }`}
                            >
                              {group.currentState}
                            </span>
                          </td>

                          <td className="p-4 text-right">
                            <Button
                              variant="primary"
                              className="bg-primary-600 hover:bg-primary-700 text-white inline-flex items-center gap-1.5 py-1.5 px-3.5 text-xs font-semibold"
                              onClick={() => setSelectedIndicator(group)}
                            >
                              <Eye size={14} />
                              Evaluar
                            </Button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>

                  {/* Pagination Footer (15 records per page) */}
                  <div className="flex items-center justify-between border-t border-gray-100 bg-gray-50/50 px-4 py-3 text-xs text-gray-600">
                    <div>
                      Mostrando <span className="font-semibold text-gray-900">{((currentPage - 1) * ITEMS_PER_PAGE) + 1}</span> a{' '}
                      <span className="font-semibold text-gray-900">
                        {Math.min(currentPage * ITEMS_PER_PAGE, filteredIndicators.length)}
                      </span>{' '}
                      de <span className="font-semibold text-gray-900">{filteredIndicators.length}</span> registros (15 máx/pág)
                    </div>
                    <div className="flex items-center gap-2">
                      <Button
                        variant="ghost"
                        className="px-2.5 py-1 text-xs"
                        disabled={currentPage === 1}
                        onClick={() => setCurrentPage((p) => Math.max(1, p - 1))}
                      >
                        Anterior
                      </Button>
                      <span className="font-semibold text-gray-800">
                        Página {currentPage} de {totalPages}
                      </span>
                      <Button
                        variant="ghost"
                        className="px-2.5 py-1 text-xs"
                        disabled={currentPage >= totalPages}
                        onClick={() => setCurrentPage((p) => Math.min(totalPages, p + 1))}
                      >
                        Siguiente
                      </Button>
                    </div>
                  </div>
                </div>
              )}
            </div>

            {/* AI Copilot Panel with Action History & Traceability */}
            <div className="xl:col-span-1">
              <EvidenceCopilotPanel onFilterChange={handleCopilotFilter} />
            </div>
          </div>

          {/* Modal for Opening and Inspecting the Indicator + Evidences */}
          <IndicatorReviewModal
            isOpen={Boolean(selectedIndicator)}
            indicator={selectedIndicator}
            isLoadingApprove={approveMutation.isPending}
            isLoadingReject={rejectMutation.isPending}
            onClose={() => setSelectedIndicator(null)}
            onApprove={handleApprove}
            onOpenRejectModal={(ind) => setRejectingGroup(ind)}
          />

          {/* Modal for Rejecting/Observing the Indicator with Comments */}
          {rejectingGroup && (
            <RejectIndicatorModal
              isOpen={Boolean(rejectingGroup)}
              indicatorId={rejectingGroup.indicatorId}
              programId={rejectingGroup.programId}
              evidences={rejectingGroup.evidences}
              isLoading={rejectMutation.isPending}
              onClose={() => setRejectingGroup(null)}
              onSubmit={handleRejectConfirm}
            />
          )}
        </div>
      </main>
    </div>
  );
}
