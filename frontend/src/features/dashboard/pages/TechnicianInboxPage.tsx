import { useCallback, useEffect, useMemo, useState } from 'react';
import { Sidebar } from '../../../components/layout/Sidebar';
import {
  fetchTechnicianInbox,
  approveIndicator,
  rejectIndicator,
  type TechnicianInboxItemDto,
} from '../api/technicianInboxApi';
import { AlertTriangle, CheckCircle2, Search, X, XCircle } from 'lucide-react';

const STATUS_FILTERS: { id: string; label: string }[] = [
  { id: '', label: 'Pendientes' },
  { id: 'SUBIDO', label: 'Nuevas' },
  { id: 'SUBSANADO', label: 'Subsanadas' },
  { id: 'OBSERVADO', label: 'Observadas' },
  { id: 'APROBADO', label: 'Aprobados' },
];

function canDictate(status: string) {
  return status === 'SUBIDO' || status === 'SUBSANADO';
}

function statusLabel(status: string) {
  switch (status) {
    case 'SUBIDO':
      return 'Nueva';
    case 'SUBSANADO':
      return 'Subsanada';
    case 'OBSERVADO':
      return 'Observada';
    case 'APROBADO':
      return 'Aprobada';
    default:
      return status;
  }
}

function statusClasses(status: string) {
  switch (status) {
    case 'APROBADO':
      return 'bg-success/10 text-success';
    case 'OBSERVADO':
      return 'bg-secondary/10 text-secondary';
    case 'SUBSANADO':
      return 'bg-info/10 text-info';
    default:
      return 'bg-warning/15 text-gray-900';
  }
}

export function TechnicianInboxPage() {
  const [items, setItems] = useState<TechnicianInboxItemDto[]>([]);
  const [careers, setCareers] = useState<{ id: string; name: string }[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [programFilter, setProgramFilter] = useState('');
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [actionSuccessMessage, setActionSuccessMessage] = useState<string | null>(null);

  const [selectedItem, setSelectedItem] = useState<TechnicianInboxItemDto | null>(null);
  const [isApproveModalOpen, setIsApproveModalOpen] = useState(false);
  const [isRejectModalOpen, setIsRejectModalOpen] = useState(false);
  const [justification, setJustification] = useState('');
  const [isSubmittingAction, setIsSubmittingAction] = useState(false);

  const loadInbox = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await fetchTechnicianInbox({
        status: statusFilter || undefined,
        programId: programFilter || undefined,
        page,
        size: 15,
      });
      const next = response?.content ?? [];
      setItems(next);
      setTotalElements(response?.totalElements ?? 0);
      setTotalPages(response?.totalPages ?? 0);
      setCareers((current) => {
        const merged = new Map(current.map((career) => [career.id, career.name]));
        for (const item of next) {
          if (item.programId && item.programName) {
            merged.set(item.programId, item.programName);
          }
        }
        return [...merged.entries()]
          .map(([id, name]) => ({ id, name }))
          .sort((a, b) => a.name.localeCompare(b.name, 'es'));
      });
    } catch (err) {
      setErrorMessage(
        err instanceof Error
          ? err.message
          : 'Error al cargar la bandeja de revisión técnica.',
      );
    } finally {
      setIsLoading(false);
    }
  }, [page, programFilter, statusFilter]);

  useEffect(() => {
    void loadInbox();
  }, [loadInbox]);

  useEffect(() => {
    if (!isApproveModalOpen && !isRejectModalOpen) return;
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape' && !isSubmittingAction) {
        setIsApproveModalOpen(false);
        setIsRejectModalOpen(false);
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isApproveModalOpen, isRejectModalOpen, isSubmittingAction]);

  const visibleItems = useMemo(() => {
    const term = search.trim().toLowerCase();
    if (!term) return items;
    return items.filter((item) =>
      [item.indicatorCode, item.indicatorDescription, item.programName, item.evidenceFilename]
        .filter(Boolean)
        .some((value) => value!.toLowerCase().includes(term)),
    );
  }, [items, search]);

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
      setPage(0);
      if (statusFilter === 'APROBADO') {
        void loadInbox();
      } else {
        setStatusFilter('APROBADO');
      }
    } catch (err) {
      setErrorMessage(err instanceof Error ? err.message : 'Error al aprobar el indicador.');
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
      setErrorMessage(err instanceof Error ? err.message : 'Error al observar el indicador.');
    } finally {
      setIsSubmittingAction(false);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="inbox" />

      <main className="flex-1 overflow-y-auto px-8 py-8">
        <header className="mb-6 flex items-end justify-between gap-4">
          <div>
            <h1 className="text-heading-xl font-bold text-primary-800">Bandeja de dictamen</h1>
            <p className="mt-1 text-body-lg text-gray-700">
              Indicadores con evidencia lista para aprobar u observar.
            </p>
          </div>
          <p className="text-body-md text-gray-700">
            <span className="font-semibold text-primary-800">{totalElements}</span> en esta vista
          </p>
        </header>

        {actionSuccessMessage && (
          <div className="mb-4 flex items-center justify-between rounded-xl border border-success/30 bg-success/10 p-4 text-success">
            <div className="flex items-center gap-2">
              <CheckCircle2 size={18} />
              <span className="text-body-md">{actionSuccessMessage}</span>
            </div>
            <button type="button" onClick={() => setActionSuccessMessage(null)} aria-label="Cerrar aviso">
              <X size={16} />
            </button>
          </div>
        )}

        {errorMessage && (
          <div className="mb-4 flex items-center justify-between rounded-xl border border-danger/30 bg-danger/10 p-4 text-danger">
            <div className="flex items-center gap-2">
              <AlertTriangle size={18} />
              <span className="text-body-md">{errorMessage}</span>
            </div>
            <button type="button" onClick={() => setErrorMessage(null)} aria-label="Cerrar error">
              <X size={16} />
            </button>
          </div>
        )}

        <div className="mb-4 flex flex-wrap items-center gap-3">
          <label className="relative min-w-56 flex-1">
            <span className="sr-only">Buscar indicador</span>
            <Search className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-gray-600" size={16} />
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Buscar código, carrera o archivo"
              className="w-full rounded-xl border border-gray-300 bg-body py-2 pl-9 pr-3 text-body-md text-gray-900 focus:border-primary focus:outline-none"
            />
          </label>
          <select
            aria-label="Filtrar por carrera"
            value={programFilter}
            onChange={(event) => {
              setProgramFilter(event.target.value);
              setPage(0);
            }}
            className="rounded-xl border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 focus:border-primary focus:outline-none"
          >
            <option value="">Todas las carreras</option>
            {careers.map((career) => (
              <option key={career.id} value={career.id}>
                {career.name}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-4 flex flex-wrap gap-2">
          {STATUS_FILTERS.map((filter) => {
            const active = statusFilter === filter.id;
            return (
              <button
                key={filter.id || 'pendientes'}
                type="button"
                onClick={() => {
                  setStatusFilter(filter.id);
                  setPage(0);
                }}
                className={`rounded-full px-3 py-1.5 text-label-md font-medium ${
                  active
                    ? filter.id === 'APROBADO'
                      ? 'bg-success text-body'
                      : 'bg-primary text-body'
                    : 'bg-body text-gray-800 ring-1 ring-gray-300'
                }`}
              >
                {filter.label}
              </button>
            );
          })}
        </div>

        <div className="overflow-hidden rounded-2xl border border-gray-200 bg-body shadow-sm">
          {isLoading ? (
            <div className="h-64 animate-pulse bg-gray-100" />
          ) : visibleItems.length === 0 ? (
            <div className="px-6 py-16 text-center">
              <p className="text-heading-sm font-semibold text-primary-800">Sin indicadores en esta vista</p>
              <p className="mt-2 text-body-md text-gray-700">
                {statusFilter === 'APROBADO'
                  ? 'Todavía no hay dictámenes aprobados.'
                  : 'Amplíe la carrera o el estado para ver más evidencias.'}
              </p>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-gray-200 bg-gray-50 text-label-md uppercase text-gray-700">
                    <th className="px-4 py-3 font-medium">Indicador</th>
                    <th className="px-4 py-3 font-medium">Carrera</th>
                    <th className="px-4 py-3 font-medium">Estado</th>
                    <th className="px-4 py-3 font-medium">Evidencia</th>
                    <th className="px-4 py-3 text-right font-medium">Dictamen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {visibleItems.map((item) => (
                    <tr key={item.indicatorId}>
                      <td className="px-4 py-3">
                        <p className="text-body-md font-semibold text-primary-800">{item.indicatorCode}</p>
                        <p className="text-body-md text-gray-700">{item.indicatorDescription}</p>
                        <p className="text-label-md text-gray-600">{item.pathHierarchy}</p>
                      </td>
                      <td className="px-4 py-3 text-body-md text-gray-800">{item.programName}</td>
                      <td className="px-4 py-3">
                        <span className={`rounded-full px-2.5 py-1 text-label-md font-medium ${statusClasses(item.status)}`}>
                          {statusLabel(item.status)}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-body-md text-gray-800">
                        {item.evidenceFilename ?? 'Sin archivo'}
                      </td>
                      <td className="px-4 py-3 text-right">
                        {canDictate(item.status) ? (
                          <div className="flex justify-end gap-2">
                            <button
                              type="button"
                              onClick={() => handleOpenApprove(item)}
                              className="rounded-lg bg-success px-3 py-1.5 text-label-md font-semibold text-body"
                            >
                              Aprobar
                            </button>
                            <button
                              type="button"
                              onClick={() => handleOpenReject(item)}
                              className="rounded-lg border border-secondary px-3 py-1.5 text-label-md font-semibold text-secondary"
                            >
                              Observar
                            </button>
                          </div>
                        ) : item.status === 'APROBADO' ? (
                          <span className="text-label-md font-semibold text-success">Aprobado</span>
                        ) : (
                          <span className="text-label-md text-gray-600">En corrección</span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-200 px-4 py-3">
              <span className="text-body-md text-gray-700">
                Página {page + 1} de {totalPages}
              </span>
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={page === 0}
                  onClick={() => setPage((current) => Math.max(0, current - 1))}
                  className="rounded-lg border border-gray-300 px-3 py-1.5 text-label-md disabled:opacity-40"
                >
                  Anterior
                </button>
                <button
                  type="button"
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage((current) => current + 1)}
                  className="rounded-lg border border-gray-300 px-3 py-1.5 text-label-md disabled:opacity-40"
                >
                  Siguiente
                </button>
              </div>
            </div>
          )}
        </div>

        {isApproveModalOpen && selectedItem && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-primary-900/40 p-4"
            onClick={() => {
              if (!isSubmittingAction) setIsApproveModalOpen(false);
            }}
          >
            <div
              role="dialog"
              aria-modal="true"
              aria-labelledby="approve-title"
              className="w-full max-w-md rounded-2xl border border-gray-200 bg-body p-5 shadow-md"
              onClick={(event) => event.stopPropagation()}
            >
              <h3 id="approve-title" className="text-heading-sm font-semibold text-primary-800">
                Confirmar Aprobación Técnica
              </h3>
              <p className="mt-2 text-body-md text-gray-700">
                ¿Aprueba el indicador <span className="font-semibold">{selectedItem.indicatorCode}</span> de{' '}
                {selectedItem.programName}?
              </p>
              <p className="mt-1 text-body-md text-gray-600">{selectedItem.evidenceFilename}</p>
              <div className="mt-5 flex justify-end gap-2">
                <button
                  type="button"
                  disabled={isSubmittingAction}
                  onClick={() => setIsApproveModalOpen(false)}
                  className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  disabled={isSubmittingAction}
                  onClick={() => void handleConfirmApprove()}
                  className="rounded-xl bg-success px-4 py-2 text-label-md font-semibold text-body"
                >
                  {isSubmittingAction ? 'Aprobando...' : 'Confirmar Aprobación'}
                </button>
              </div>
            </div>
          </div>
        )}

        {isRejectModalOpen && selectedItem && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-primary-900/40 p-4"
            onClick={() => {
              if (!isSubmittingAction) setIsRejectModalOpen(false);
            }}
          >
            <div
              role="dialog"
              aria-modal="true"
              aria-labelledby="reject-title"
              className="w-full max-w-lg rounded-2xl border border-gray-200 bg-body p-5 shadow-md"
              onClick={(event) => event.stopPropagation()}
            >
              <h3 id="reject-title" className="text-heading-sm font-semibold text-primary-800">
                Emitir Observación Técnica
              </h3>
              <p className="mt-2 text-body-md text-gray-700">
                Fundamentación para <span className="font-semibold">{selectedItem.indicatorCode}</span> (mínimo 20
                caracteres).
              </p>
              <textarea
                rows={4}
                value={justification}
                onChange={(event) => setJustification(event.target.value)}
                placeholder="La evidencia adjunta no acredita el cumplimiento del estándar..."
                className="mt-3 w-full rounded-xl border border-gray-300 p-3 text-body-md focus:border-primary focus:outline-none"
              />
              <div className="mt-4 flex justify-end gap-2">
                <button
                  type="button"
                  disabled={isSubmittingAction}
                  onClick={() => setIsRejectModalOpen(false)}
                  className="rounded-xl border border-gray-300 px-4 py-2 text-label-md font-semibold"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  disabled={isSubmittingAction || justification.trim().length < 20}
                  onClick={() => void handleConfirmReject()}
                  className="rounded-xl bg-secondary px-4 py-2 text-label-md font-semibold text-body disabled:opacity-50"
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
