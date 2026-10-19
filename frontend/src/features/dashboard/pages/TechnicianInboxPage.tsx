import { useState, useEffect, useCallback } from 'react';
import { Sidebar } from '../../../components/layout/Sidebar';
import {
  fetchTechnicianInbox,
  approveIndicator,
  rejectIndicator,
  type TechnicianInboxItemDto,
} from '../api/technicianInboxApi';
import {
  CheckCircle2,
  XCircle,
  FileCheck,
  AlertTriangle,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  FolderDot,
} from 'lucide-react';

export function TechnicianInboxPage() {
  const [items, setItems] = useState<TechnicianInboxItemDto[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [page, setPage] = useState<number>(0);
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [actionSuccessMessage, setActionSuccessMessage] = useState<string | null>(null);

  // Modals state
  const [selectedItem, setSelectedItem] = useState<TechnicianInboxItemDto | null>(null);
  const [isApproveModalOpen, setIsApproveModalOpen] = useState<boolean>(false);
  const [isRejectModalOpen, setIsRejectModalOpen] = useState<boolean>(false);
  const [justification, setJustification] = useState<string>('');
  const [isSubmittingAction, setIsSubmittingAction] = useState<boolean>(false);

  const loadInbox = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await fetchTechnicianInbox({
        status: statusFilter || undefined,
        page,
        size: 15,
      });
      setItems(response?.content ?? []);
      setTotalElements(response?.totalElements ?? 0);
      setTotalPages(response?.totalPages ?? 0);
    } catch (err) {
      setErrorMessage(
        err instanceof Error
          ? err.message
          : 'Error al cargar la bandeja de revisión técnica.',
      );
    } finally {
      setIsLoading(false);
    }
  }, [page, statusFilter]);

  useEffect(() => {
    void loadInbox();
  }, [loadInbox]);

  const handleOpenApprove = (item: TechnicianInboxItemDto) => {
    setSelectedItem(item);
    setIsApproveModalOpen(true);
  };

  const handleOpenReject = (item: TechnicianInboxItemDto) => {
    setSelectedItem(item);
    setJustification('');
    setIsRejectModalOpen(true);
  };

  const handleConfirmApprove = async () => {
    if (!selectedItem) return;
    setIsSubmittingAction(true);
    setErrorMessage(null);
    try {
      await approveIndicator(selectedItem.indicatorId);
      setIsApproveModalOpen(false);
      setActionSuccessMessage(`Indicador ${selectedItem.indicatorCode} aprobado exitosamente.`);
      void loadInbox();
    } catch (err) {
      setErrorMessage(
        err instanceof Error ? err.message : 'Error al aprobar el indicador.',
      );
    } finally {
      setIsSubmittingAction(false);
    }
  };

  const handleConfirmReject = async () => {
    if (!selectedItem) return;
    if (justification.trim().length < 20) {
      setErrorMessage('La justificación debe tener al menos 20 caracteres.');
      return;
    }
    setIsSubmittingAction(true);
    setErrorMessage(null);
    try {
      await rejectIndicator(selectedItem.indicatorId, justification);
      setIsRejectModalOpen(false);
      setActionSuccessMessage(`Indicador ${selectedItem.indicatorCode} observado con fundamentación técnica.`);
      void loadInbox();
    } catch (err) {
      setErrorMessage(
        err instanceof Error ? err.message : 'Error al observar el indicador.',
      );
    } finally {
      setIsSubmittingAction(false);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="dashboard" />

      <main className="flex-1 overflow-y-auto px-8 py-8 text-primary-900 bg-gray-50/50">
        {/* Banner */}
        <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-700 to-primary-600 p-6 text-body shadow-md mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-heading-lg font-bold">Bandeja Centralizada de Auditoría [TD]</h1>
              <p className="text-body-md text-primary-100">
                Dictamen técnico y validación de evidencias (FSD-UC-012 / FEAT-06).
              </p>
            </div>
            <button
              onClick={() => void loadInbox()}
              className="flex items-center gap-2 rounded-xl bg-body/15 hover:bg-body/25 px-4 py-2.5 text-label-md font-semibold text-body transition-colors"
            >
              <RefreshCw size={16} className={isLoading ? 'animate-spin' : ''} />
              <span>Actualizar</span>
            </button>
          </div>
        </div>

        {/* Feedback alerts */}
        {actionSuccessMessage && (
          <div className="mb-4 rounded-xl border border-success/30 bg-success/10 p-4 text-success flex items-center justify-between">
            <div className="flex items-center gap-2">
              <CheckCircle2 size={18} />
              <span>{actionSuccessMessage}</span>
            </div>
            <button
              onClick={() => setActionSuccessMessage(null)}
              className="text-success font-bold hover:opacity-80"
            >
              ✕
            </button>
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 rounded-xl border border-danger/30 bg-danger/10 p-4 text-danger flex items-center justify-between">
            <div className="flex items-center gap-2">
              <AlertTriangle size={18} />
              <span>{errorMessage}</span>
            </div>
            <button
              onClick={() => setErrorMessage(null)}
              className="text-danger font-bold hover:opacity-80"
            >
              ✕
            </button>
          </div>
        )}

        {/* Filters */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-4 shadow-sm mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <span className="text-label-md font-semibold text-gray-700">Filtrar por estado:</span>
            <select
              value={statusFilter}
              onChange={(e) => {
                setStatusFilter(e.target.value);
                setPage(0);
              }}
              className="rounded-xl border border-gray-300 bg-body px-3 py-2 text-body-md focus:border-primary-500 focus:outline-none"
            >
              <option value="">Pendientes de dictamen (SUBIDO, SUBSANADO, OBSERVADO)</option>
              <option value="SUBIDO">SUBIDO (Nuevas cargas)</option>
              <option value="SUBSANADO">SUBSANADO (Respuestas a observaciones)</option>
              <option value="OBSERVADO">OBSERVADO (Pendiente de corrección por CC)</option>
            </select>
          </div>

          <div className="text-label-md text-gray-600">
            Total indicadores encontrados: <span className="font-bold text-primary-800">{totalElements}</span>
          </div>
        </div>

        {/* Items Table */}
        <div className="rounded-2xl border border-primary-200/40 bg-body shadow-sm overflow-hidden">
          {isLoading ? (
            <div className="flex h-64 items-center justify-center text-gray-500">
              <RefreshCw size={28} className="animate-spin text-primary-500 mr-2" />
              <span>Cargando bandeja de dictamen...</span>
            </div>
          ) : (!items || items.length === 0) ? (
            <div className="flex h-64 flex-col items-center justify-center text-gray-500">
              <FolderDot size={40} className="text-gray-300 mb-2" />
              <span className="text-heading-sm font-semibold">No hay evidencias pendientes en esta vista</span>
              <p className="text-body-sm text-gray-600 mt-1">Todos los indicadores asignados han sido calificados.</p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-body-md">
                <thead>
                  <tr className="border-b border-gray-100 bg-gray-50/50 text-label-sm uppercase font-semibold text-gray-600">
                    <th className="py-3 px-4">Indicador</th>
                    <th className="py-3 px-4">Ruta Jerárquica Normativa</th>
                    <th className="py-3 px-4">Estado</th>
                    <th className="py-3 px-4">Evidencia Asociada</th>
                    <th className="py-3 px-4">Observaciones</th>
                    <th className="py-3 px-4 text-right">Acciones de Dictamen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {items.map((item) => (
                    <tr key={item.indicatorId} className="hover:bg-gray-50/70 transition-colors">
                      <td className="py-4 px-4 font-bold text-primary-800">
                        {item.indicatorCode}
                        <p className="text-xs font-normal text-gray-600 line-clamp-1">{item.indicatorDescription}</p>
                      </td>
                      <td className="py-4 px-4 text-body-sm text-gray-700 max-w-xs">
                        {item.pathHierarchy}
                      </td>
                      <td className="py-4 px-4">
                        <span
                          className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                            item.status === 'APROBADO'
                              ? 'bg-success/10 text-success'
                              : item.status === 'OBSERVADO'
                              ? 'bg-danger/10 text-danger'
                              : 'bg-warning/10 text-warning'
                          }`}
                        >
                          {item.status}
                        </span>
                      </td>
                      <td className="py-4 px-4 text-body-sm text-gray-700">
                        {item.evidenceFilename ? (
                          <div className="flex items-center gap-1.5 text-primary-700">
                            <FileCheck size={14} />
                            <span className="truncate max-w-[180px]">{item.evidenceFilename}</span>
                          </div>
                        ) : (
                          <span className="text-gray-600 italic text-xs">Sin archivo</span>
                        )}
                      </td>
                      <td className="py-4 px-4 text-body-sm">
                        {item.activeObservationsCount > 0 ? (
                          <span className="font-semibold text-secondary-650">
                            {item.activeObservationsCount} abierta(s)
                          </span>
                        ) : (
                          <span className="text-gray-600">0</span>
                        )}
                      </td>
                      <td className="py-4 px-4 text-right">
                        <div className="flex items-center justify-end gap-2">
                          <button
                            onClick={() => handleOpenApprove(item)}
                            className="inline-flex items-center gap-1 rounded-lg bg-success hover:bg-success/90 text-body px-3 py-1.5 text-xs font-semibold transition-colors"
                          >
                            <CheckCircle2 size={14} />
                            <span>Aprobar</span>
                          </button>
                          <button
                            onClick={() => handleOpenReject(item)}
                            className="inline-flex items-center gap-1 rounded-lg border border-danger/40 hover:bg-danger/10 text-danger px-3 py-1.5 text-xs font-semibold transition-colors"
                          >
                            <XCircle size={14} />
                            <span>Observar</span>
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination controls */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-100 p-4">
              <span className="text-body-sm text-gray-600">
                Página {page + 1} de {totalPages}
              </span>
              <div className="flex items-center gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                  className="rounded-lg border border-gray-200 p-2 disabled:opacity-40 hover:bg-gray-50"
                >
                  <ChevronLeft size={16} />
                </button>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                  className="rounded-lg border border-gray-200 p-2 disabled:opacity-40 hover:bg-gray-50"
                >
                  <ChevronRight size={16} />
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Modal Aprobar (UC-009) */}
        {isApproveModalOpen && selectedItem && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
            <div className="w-full max-w-md rounded-2xl bg-body p-6 shadow-xl animate-fadeIn">
              <div className="flex items-center gap-3 mb-4 text-success">
                <CheckCircle2 size={28} />
                <h3 className="text-heading-sm font-bold text-primary-900">Confirmar Aprobación Técnica</h3>
              </div>
              <p className="text-body-md text-gray-700 mb-4">
                ¿Está seguro de emitir dictamen aprobatorio para el indicador{' '}
                <span className="font-bold">{selectedItem.indicatorCode}</span>?
              </p>
              <div className="flex items-center justify-end gap-3">
                <button
                  disabled={isSubmittingAction}
                  onClick={() => setIsApproveModalOpen(false)}
                  className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold hover:bg-gray-50"
                >
                  Cancelar
                </button>
                <button
                  disabled={isSubmittingAction}
                  onClick={() => void handleConfirmApprove()}
                  className="rounded-xl bg-success hover:bg-success/90 text-body px-4 py-2 text-label-md font-semibold transition-colors"
                >
                  {isSubmittingAction ? 'Aprobando...' : 'Confirmar Aprobación'}
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Modal Observar (UC-008) */}
        {isRejectModalOpen && selectedItem && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
            <div className="w-full max-w-lg rounded-2xl bg-body p-6 shadow-xl animate-fadeIn">
              <div className="flex items-center gap-3 mb-4 text-danger">
                <XCircle size={28} />
                <h3 className="text-heading-sm font-bold text-primary-900">Emitir Observación Técnica</h3>
              </div>
              <p className="text-body-md text-gray-700 mb-3">
                Indique la fundamentación técnica de la observación para el indicador{' '}
                <span className="font-bold">{selectedItem.indicatorCode}</span> (mínimo 20 caracteres):
              </p>
              <textarea
                rows={4}
                value={justification}
                onChange={(e) => setJustification(e.target.value)}
                placeholder="Ejemplo: La evidencia adjunta no acredita el cumplimiento del estándar..."
                className="w-full rounded-xl border border-gray-300 p-3 text-body-md focus:border-primary-500 focus:outline-none mb-4"
              />
              <div className="flex items-center justify-end gap-3">
                <button
                  disabled={isSubmittingAction}
                  onClick={() => setIsRejectModalOpen(false)}
                  className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold hover:bg-gray-50"
                >
                  Cancelar
                </button>
                <button
                  disabled={isSubmittingAction || justification.trim().length < 20}
                  onClick={() => void handleConfirmReject()}
                  className="rounded-xl bg-danger hover:bg-danger/90 text-body px-4 py-2 text-label-md font-semibold disabled:opacity-50 transition-colors"
                >
                  {isSubmittingAction ? 'Guardando...' : 'Emitir Observación'}
                </button>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
