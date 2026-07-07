import { useState } from 'react';
import { useDashboardDetails } from '../api/dashboardHooks';
import { ArrowUpDown, ChevronLeft, ChevronRight, ExternalLink, Filter, HelpCircle, Loader2 } from 'lucide-react';

export const CoordinatorObservationsTable = () => {
  const [page, setPage] = useState(0);
  const [size] = useState(5);
  const [sort, setSort] = useState('dueDate,asc'); // default sort is fechaLimite (dueDate) asc
  const [phaseId, setPhaseId] = useState<number | undefined>(undefined);
  const [estado, setEstado] = useState<string | undefined>(undefined);

  const { details, isLoading, isFetching } = useDashboardDetails({
    page,
    size,
    sort,
    phaseId,
    estado,
  });

  const toggleSort = () => {
    setSort((current) => (current === 'dueDate,asc' ? 'dueDate,desc' : 'dueDate,asc'));
    setPage(0);
  };

  const handlePhaseChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setPhaseId(value ? Number(value) : undefined);
    setPage(0);
  };

  const handleEstadoChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const value = e.target.value;
    setEstado(value || undefined);
    setPage(0);
  };

  const totalPages = details?.totalPages ?? 0;
  const content = details?.content ?? [];

  return (
    <div className="flex flex-col gap-6 rounded-2xl border border-primary-200/10 bg-primary-900/20 p-6 backdrop-blur-md">
      {/* Header and Filters */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-heading-sm font-semibold text-body">Observaciones Pendientes de Subsanación</h3>
          <p className="text-body-md text-primary-200">
            Listado detallado de las observaciones emitidas que requieren subsanación.
          </p>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 rounded-xl bg-primary-900/60 border border-primary-800 px-3.5 py-2">
            <Filter size={16} className="text-primary-200" />
            <select
              value={phaseId ?? ''}
              onChange={handlePhaseChange}
              className="bg-transparent text-label-md font-medium text-body focus:outline-none cursor-pointer"
            >
              <option value="" className="bg-primary-900 text-body">Todas las Fases</option>
              <option value="1" className="bg-primary-900 text-body">Fase 1: Autoevaluación</option>
              <option value="2" className="bg-primary-900 text-body">Fase 2: Verificación</option>
            </select>
          </div>

          <div className="flex items-center gap-2 rounded-xl bg-primary-900/60 border border-primary-800 px-3.5 py-2">
            <Filter size={16} className="text-primary-200" />
            <select
              value={estado ?? ''}
              onChange={handleEstadoChange}
              className="bg-transparent text-label-md font-medium text-body focus:outline-none cursor-pointer"
            >
              <option value="" className="bg-primary-900 text-body">Todos los Estados</option>
              <option value="PENDIENTE_SUBSANACION" className="bg-primary-900 text-body">Pendiente Subsanación</option>
              <option value="EN_REVISION" className="bg-primary-900 text-body">En Revisión</option>
              <option value="APROBADO" className="bg-primary-900 text-body">Aprobado</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table Container */}
      <div className="relative overflow-x-auto rounded-xl border border-primary-800/80 bg-primary-900/10">
        {isLoading ? (
          <div className="flex h-64 items-center justify-center gap-2 text-primary-200">
            <Loader2 className="animate-spin" size={24} />
            <span className="text-body-md font-medium">Cargando observaciones...</span>
          </div>
        ) : content.length === 0 ? (
          <div className="flex h-64 flex-col items-center justify-center gap-2 text-primary-300">
            <HelpCircle size={32} className="text-primary-400" />
            <span className="text-body-md font-medium">No se encontraron observaciones</span>
          </div>
        ) : (
          <table className="w-full text-left text-body-md text-primary-100">
            <thead className="bg-primary-900/50 text-label-md uppercase font-semibold text-primary-300 border-b border-primary-800/60">
              <tr>
                <th className="px-6 py-4">Indicador</th>
                <th className="px-6 py-4">Descripción</th>
                <th className="px-6 py-4">F. Emisión</th>
                <th className="px-6 py-4 cursor-pointer hover:bg-primary-800/30 transition-colors" onClick={toggleSort}>
                  <div className="flex items-center gap-1">
                    F. Límite
                    <ArrowUpDown size={14} className="text-primary-400" />
                  </div>
                </th>
                <th className="px-6 py-4">Días Rest.</th>
                <th className="px-6 py-4">Estado</th>
                <th className="px-6 py-4 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary-800/40">
              {content.map((obs) => (
                <tr key={obs.observacionId} className="hover:bg-primary-900/35 transition-colors duration-150">
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span className="font-semibold text-secondary-300 block">{obs.codigoIndicador}</span>
                    <span className="text-xs text-primary-300 block max-w-[180px] truncate" title={obs.tituloIndicador}>
                      {obs.tituloIndicador}
                    </span>
                  </td>
                  <td className="px-6 py-4.5 max-w-sm truncate" title={obs.descripcion}>
                    {obs.descripcion}
                  </td>
                  <td className="px-6 py-4.5 whitespace-nowrap text-primary-200">{obs.fechaEmision}</td>
                  <td className="px-6 py-4.5 whitespace-nowrap font-medium text-body">{obs.fechaLimite}</td>
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                        obs.diasRestantes <= 3
                          ? 'bg-secondary-900/40 text-secondary-200 border border-secondary-800'
                          : obs.diasRestantes <= 7
                          ? 'bg-warning/20 text-warning border border-warning/30'
                          : 'bg-primary-800/60 text-primary-200 border border-primary-800'
                      }`}
                    >
                      {obs.diasRestantes} días
                    </span>
                  </td>
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                        obs.estado === 'PENDIENTE_SUBSANACION'
                          ? 'bg-secondary-900/30 text-secondary-200 border border-secondary-800/50'
                          : obs.estado === 'EN_REVISION'
                          ? 'bg-warning/10 text-warning border border-warning/20'
                          : 'bg-success/15 text-success border border-success/20'
                      }`}
                    >
                      {obs.estado.replace(/_/g, ' ')}
                    </span>
                  </td>
                  <td className="px-6 py-4.5 whitespace-nowrap text-right">
                    {obs.urlSubsanacion ? (
                      <a
                        href={obs.urlSubsanacion}
                        className="inline-flex items-center gap-1.5 rounded-lg bg-primary-800/80 hover:bg-primary-700 text-body hover:text-body px-3 py-1.5 text-xs font-medium transition-all border border-primary-800 hover:border-primary-600 active:scale-95"
                      >
                        Subsanar
                        <ExternalLink size={12} />
                      </a>
                    ) : (
                      <span className="text-xs text-primary-400">N/A</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Pagination Controls */}
      {!isLoading && content.length > 0 && (
        <div className="flex items-center justify-between border-t border-primary-800/50 pt-4">
          <span className="text-body-md text-primary-300">
            Total: <span className="font-semibold text-body">{details?.totalElements}</span> observaciones
          </span>

          <div className="flex items-center gap-2">
            <button
              type="button"
              disabled={page === 0 || isFetching}
              onClick={() => setPage((p) => p - 1)}
              className="flex h-9 w-9 items-center justify-center rounded-lg border border-primary-800 bg-primary-900/40 text-primary-200 hover:text-body disabled:opacity-40 disabled:cursor-not-allowed hover:bg-primary-800 transition-colors"
            >
              <ChevronLeft size={18} />
            </button>
            <span className="text-body-md text-primary-200 font-medium">
              Pág. <span className="text-body">{page + 1}</span> de {totalPages}
            </span>
            <button
              type="button"
              disabled={page >= totalPages - 1 || isFetching}
              onClick={() => setPage((p) => p + 1)}
              className="flex h-9 w-9 items-center justify-center rounded-lg border border-primary-800 bg-primary-900/40 text-primary-200 hover:text-body disabled:opacity-40 disabled:cursor-not-allowed hover:bg-primary-800 transition-colors"
            >
              <ChevronRight size={18} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
