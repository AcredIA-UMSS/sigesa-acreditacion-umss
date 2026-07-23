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
    <div className="flex flex-col gap-6 rounded-2xl border border-gray-100 bg-body p-6 shadow-sm">
      {/* Header and Filters */}
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h3 className="text-heading-sm font-bold text-primary-800">Observaciones Pendientes de Subsanación</h3>
          <p className="text-body-md text-gray-600">
            Listado detallado de las observaciones emitidas que requieren subsanación.
          </p>
        </div>

        {/* Filters */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-2 rounded-xl border border-primary-200/40 bg-body px-3.5 py-2 shadow-sm">
            <Filter size={16} className="text-primary-500" />
            <select
              value={phaseId ?? ''}
              onChange={handlePhaseChange}
              className="bg-transparent text-label-md font-semibold text-primary-800 focus:outline-none cursor-pointer"
            >
              <option value="" className="bg-body text-primary-900">Todas las Fases</option>
              <option value="1" className="bg-body text-primary-900">Fase 1: Autoevaluación</option>
              <option value="2" className="bg-body text-primary-900">Fase 2: Verificación</option>
            </select>
          </div>

          <div className="flex items-center gap-2 rounded-xl border border-primary-200/40 bg-body px-3.5 py-2 shadow-sm">
            <Filter size={16} className="text-primary-500" />
            <select
              value={estado ?? ''}
              onChange={handleEstadoChange}
              className="bg-transparent text-label-md font-semibold text-primary-800 focus:outline-none cursor-pointer"
            >
              <option value="" className="bg-body text-primary-900">Todos los Estados</option>
              <option value="PENDIENTE_SUBSANACION" className="bg-body text-primary-900">Pendiente Subsanación</option>
              <option value="EN_REVISION" className="bg-body text-primary-900">En Revisión</option>
              <option value="APROBADO" className="bg-body text-primary-900">Aprobado</option>
            </select>
          </div>
        </div>
      </div>

      {/* Table Container */}
      <div className="relative overflow-x-auto rounded-xl border border-gray-200 bg-body">
        {isLoading ? (
          <div className="flex h-64 items-center justify-center gap-2 text-gray-500">
            <Loader2 className="animate-spin" size={24} />
            <span className="text-body-md font-medium">Cargando observaciones...</span>
          </div>
        ) : content.length === 0 ? (
          <div className="flex h-64 flex-col items-center justify-center gap-2 text-gray-400">
            <HelpCircle size={32} className="text-gray-300" />
            <span className="text-body-md font-medium">No se encontraron observaciones</span>
          </div>
        ) : (
          <table className="w-full text-left text-body-md text-gray-700">
            <thead className="bg-gray-50 text-label-sm uppercase font-semibold text-gray-600 border-b border-gray-200">
              <tr>
                <th className="px-6 py-4">Indicador</th>
                <th className="px-6 py-4">Descripción</th>
                <th className="px-6 py-4">F. Emisión</th>
                <th className="px-6 py-4 cursor-pointer hover:bg-gray-100/70 transition-colors" onClick={toggleSort}>
                  <div className="flex items-center gap-1">
                    F. Límite
                    <ArrowUpDown size={14} className="text-gray-400" />
                  </div>
                </th>
                <th className="px-6 py-4">Días Rest.</th>
                <th className="px-6 py-4">Estado</th>
                <th className="px-6 py-4 text-right">Acción</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-body">
              {content.map((obs) => (
                <tr key={obs.observacionId} className="hover:bg-gray-50/50 transition-colors duration-150">
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span className="font-semibold text-secondary-600 block">{obs.codigoIndicador}</span>
                    <span className="text-xs text-gray-500 block max-w-[180px] truncate" title={obs.tituloIndicador}>
                      {obs.tituloIndicador}
                    </span>
                  </td>
                  <td className="px-6 py-4.5 max-w-sm truncate text-gray-700" title={obs.descripcion}>
                    {obs.descripcion}
                  </td>
                  <td className="px-6 py-4.5 whitespace-nowrap text-gray-600">{obs.fechaEmision}</td>
                  <td className="px-6 py-4.5 whitespace-nowrap font-medium text-gray-800">{obs.fechaLimite}</td>
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold border ${
                        obs.diasRestantes <= 3
                          ? 'bg-secondary-50 text-secondary-600 border-secondary-200'
                          : obs.diasRestantes <= 7
                          ? 'bg-warning/10 text-amber-800 border-warning/30'
                          : 'bg-gray-100 text-gray-700 border-gray-200'
                      }`}
                    >
                      {obs.diasRestantes} días
                    </span>
                  </td>
                  <td className="px-6 py-4.5 whitespace-nowrap">
                    <span
                      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold border ${
                        obs.estado === 'PENDIENTE_SUBSANACION'
                          ? 'bg-secondary-50 text-secondary-600 border-secondary-200'
                          : obs.estado === 'EN_REVISION'
                          ? 'bg-warning/10 text-amber-800 border-warning/20'
                          : 'bg-success/10 text-success border-success/20'
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
                      <span className="text-xs text-gray-400">N/A</span>
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
        <div className="flex items-center justify-between border-t border-gray-150 pt-4">
          <span className="text-body-md text-gray-500">
            Total: <span className="font-semibold text-gray-800">{details?.totalElements}</span> observaciones
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
            <span className="text-body-md text-gray-600 font-medium">
              Pág. <span className="text-gray-800 font-bold">{page + 1}</span> de {totalPages}
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
