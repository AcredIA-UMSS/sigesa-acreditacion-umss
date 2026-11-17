import { useMemo } from 'react';
import { AlertTriangle, XCircle } from 'lucide-react';
import { useListProcesses } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import { Sidebar } from '../../../components/layout/Sidebar';
import { NotificationBellPopover } from '../../notifications';
import { useDashboardSummary } from '../api/dashboardHooks';
import { SemaphoreBoard } from '../components/SemaphoreBoard';

export function ExecutiveSemaphorePage() {
  const { summary, isLoading, error, refetch } = useDashboardSummary();
  const section = summary?.executiveSection ?? null;
  const processesQuery = useListProcesses({
    query: {
      enabled: section != null,
      staleTime: 30_000,
    },
  });

  const processIdByProgramId = useMemo(() => {
    const map = new Map<string, string>();
    const processes =
      processesQuery.data?.status === 200 && Array.isArray(processesQuery.data.data)
        ? processesQuery.data.data
        : [];
    for (const process of processes) {
      if (!process.id || !process.careerId) {
        continue;
      }
      // Prefer the first process listed for a career (list is already scoped for JD).
      if (!map.has(process.careerId)) {
        map.set(process.careerId, process.id);
      }
    }
    return map;
  }, [processesQuery.data]);

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="semaphore" />
      <main className="flex-1 overflow-y-auto px-8 py-8">
        <header className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <h1 className="text-heading-xl font-bold text-primary-800">Semáforo institucional</h1>
            <p className="mt-1 text-body-lg text-gray-700">
              Carreras por facultad. Rojo, amarillo, verde o gris si todavía no hay indicadores.
            </p>
          </div>
          <NotificationBellPopover variant="header" />
        </header>

        {isLoading && (
          <div className="h-64 animate-pulse rounded-2xl bg-gray-200" />
        )}

        {error && !isLoading && (
          <div className="mx-auto mt-12 max-w-xl rounded-2xl border border-secondary-500/20 bg-secondary-50 p-6 text-center text-secondary-900 shadow-sm">
            <XCircle className="mx-auto mb-4 text-secondary-500" size={48} />
            <h2 className="text-heading-md font-bold">No se pudo cargar el semáforo</h2>
            <button
              type="button"
              onClick={() => refetch()}
              className="mt-6 rounded-xl bg-secondary px-5 py-2.5 text-label-md font-semibold text-body hover:bg-secondary-600"
            >
              Reintentar
            </button>
          </div>
        )}

        {!isLoading && !error && section == null && (
          <div className="mx-auto mt-12 max-w-xl rounded-2xl border border-warning/20 bg-warning/5 p-8 text-center shadow-sm">
            <AlertTriangle className="mx-auto mb-4 text-warning" size={48} />
            <h2 className="text-heading-md font-bold text-primary-800">Sin datos ejecutivos</h2>
            <p className="mt-2 text-body-md text-gray-700">
              Esta vista corresponde a jefatura DUEA.
            </p>
          </div>
        )}

        {!isLoading && !error && section && (
          <SemaphoreBoard section={section} processIdByProgramId={processIdByProgramId} />
        )}
      </main>
    </div>
  );
}
