import { useState, useEffect, useCallback } from 'react';
import { Sidebar } from '../../../../components/layout/Sidebar';
import {
  fetchAuditLogs,
  downloadAuditLogsCsv,
  type AuditLogItemDto,
} from '../api/auditLogApi';
import {
  ShieldAlert,
  Download,
  RefreshCw,
  ChevronLeft,
  ChevronRight,
  Clock,
  User,
  Activity,
} from 'lucide-react';

export function AuditLogPage() {
  const [logs, setLogs] = useState<AuditLogItemDto[]>([]);
  const [totalElements, setTotalElements] = useState<number>(0);
  const [totalPages, setTotalPages] = useState<number>(0);
  const [page, setPage] = useState<number>(0);
  const [actorEmail, setActorEmail] = useState<string>('');
  const [actionFilter, setActionFilter] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const loadLogs = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await fetchAuditLogs({
        actorEmail: actorEmail.trim() || undefined,
        action: actionFilter.trim() || undefined,
        page,
        size: 20,
      });
      setLogs(response?.content ?? []);
      setTotalElements(response?.totalElements ?? 0);
      setTotalPages(response?.totalPages ?? 0);
    } catch (err) {
      setErrorMessage(
        err instanceof Error ? err.message : 'Error al cargar la bitácora de auditoría.',
      );
    } finally {
      setIsLoading(false);
    }
  }, [page, actorEmail, actionFilter]);

  useEffect(() => {
    void loadLogs();
  }, [loadLogs]);

  const handleExportCsv = async () => {
    setIsExporting(true);
    setErrorMessage(null);
    try {
      await downloadAuditLogsCsv({
        actorEmail: actorEmail.trim() || undefined,
        action: actionFilter.trim() || undefined,
      });
    } catch (err) {
      setErrorMessage(
        err instanceof Error ? err.message : 'Error al descargar el archivo CSV de auditoría.',
      );
    } finally {
      setIsExporting(false);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="history" />

      <main className="flex-1 overflow-y-auto px-8 py-8 text-primary-900 bg-gray-50/50">
        {/* Banner */}
        <div className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-gray-900 to-primary-900 p-6 text-body shadow-md mb-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-body/10">
                <ShieldAlert size={26} />
              </div>
              <div>
                <h1 className="text-heading-lg font-bold">Bitácora Inmutable de Auditoría [JD]</h1>
                <p className="text-body-md text-gray-300">
                  Trazabilidad legal append-only para Sala de Pares (CEUB / ARCU-SUR · FSD-UC-017 / FEAT-07).
                </p>
              </div>
            </div>

            <button
              onClick={() => void handleExportCsv()}
              disabled={isExporting || totalElements === 0}
              className="flex items-center gap-2 rounded-xl bg-primary-600 hover:bg-primary-500 disabled:opacity-50 px-4 py-2.5 text-label-md font-semibold text-body transition-colors shadow-sm"
            >
              <Download size={16} className={isExporting ? 'animate-bounce' : ''} />
              <span>{isExporting ? 'Exportando CSV...' : 'Exportar CSV'}</span>
            </button>
          </div>
        </div>

        {errorMessage && (
          <div className="mb-4 rounded-xl border border-danger/30 bg-danger/10 p-4 text-danger flex items-center justify-between">
            <span>{errorMessage}</span>
            <button onClick={() => setErrorMessage(null)} className="font-bold">✕</button>
          </div>
        )}

        {/* Filters */}
        <div className="rounded-2xl border border-primary-200/40 bg-body p-4 shadow-sm mb-6 flex flex-wrap items-center justify-between gap-4">
          <div className="flex flex-wrap items-center gap-3">
            <div className="flex items-center gap-2 rounded-xl border border-gray-300 px-3 py-2">
              <User size={16} className="text-gray-400" />
              <input
                type="text"
                placeholder="Filtrar por email de actor..."
                value={actorEmail}
                onChange={(e) => {
                  setActorEmail(e.target.value);
                  setPage(0);
                }}
                className="bg-transparent text-body-md focus:outline-none"
              />
            </div>

            <div className="flex items-center gap-2 rounded-xl border border-gray-300 px-3 py-2">
              <Activity size={16} className="text-gray-400" />
              <input
                type="text"
                placeholder="Acción (ej: LOGIN, EVIDENCE_UPLOADED)..."
                value={actionFilter}
                onChange={(e) => {
                  setActionFilter(e.target.value);
                  setPage(0);
                }}
                className="bg-transparent text-body-md focus:outline-none"
              />
            </div>
          </div>

          <div className="text-label-md text-gray-600">
            Registros inmutables: <span className="font-bold text-primary-800">{totalElements}</span>
          </div>
        </div>

        {/* Audit Log Table */}
        <div className="rounded-2xl border border-primary-200/40 bg-body shadow-sm overflow-hidden">
          {isLoading ? (
            <div className="flex h-64 items-center justify-center text-gray-500">
              <RefreshCw size={28} className="animate-spin text-primary-500 mr-2" />
              <span>Cargando registros de auditoría...</span>
            </div>
          ) : (!logs || logs.length === 0) ? (
            <div className="flex h-64 flex-col items-center justify-center text-gray-500">
              <Clock size={40} className="text-gray-300 mb-2" />
              <span className="text-heading-sm font-semibold">No se encontraron eventos de auditoría</span>
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-body-md">
                <thead>
                  <tr className="border-b border-gray-100 bg-gray-50/50 text-label-sm uppercase font-semibold text-gray-600">
                    <th className="py-3 px-4">Fecha y Hora</th>
                    <th className="py-3 px-4">Actor</th>
                    <th className="py-3 px-4">Rol</th>
                    <th className="py-3 px-4">Acción</th>
                    <th className="py-3 px-4">Entidad Afectada</th>
                    <th className="py-3 px-4">Detalle / Diferencia</th>
                    <th className="py-3 px-4">IP Origen</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100 font-mono text-xs">
                  {logs.map((log) => (
                    <tr key={log.id} className="hover:bg-gray-50/70 transition-colors">
                      <td className="py-3 px-4 text-gray-700 whitespace-nowrap">
                        {log.createdAt}
                      </td>
                      <td className="py-3 px-4 font-semibold text-primary-800">
                        {log.actorEmail}
                      </td>
                      <td className="py-3 px-4">
                        <span className="rounded-md bg-gray-100 px-2 py-0.5 text-gray-700 font-sans font-bold">
                          {log.actorRole || 'N/A'}
                        </span>
                      </td>
                      <td className="py-3 px-4 font-bold text-primary-700">
                        {log.action}
                      </td>
                      <td className="py-3 px-4 text-gray-600">
                        {log.entityType} ({log.entityId.slice(0, 8)}...)
                      </td>
                      <td className="py-3 px-4 text-gray-700 max-w-xs truncate font-sans">
                        {log.payloadDiff || '-'}
                      </td>
                      <td className="py-3 px-4 text-gray-500">
                        {log.ipAddress || '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="flex items-center justify-between border-t border-gray-100 p-4">
              <span className="text-body-sm text-gray-600 font-sans">
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
      </main>
    </div>
  );
}
