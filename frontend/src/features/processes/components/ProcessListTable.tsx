import { useNavigate } from 'react-router-dom';
import { FolderOpen, Trash2 } from 'lucide-react';
import type { ProcessSummaryResponseDto } from '../../../api/model';
import { ProcessStatusBadge } from './ProcessStatusBadge';

interface ProcessListTableProps {
  processes: ProcessSummaryResponseDto[];
  canDelete?: boolean;
  isDeleteBusy?: boolean;
  onDeleteRequest?: (process: ProcessSummaryResponseDto) => void;
}

function formatDate(iso?: string): string {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('es-BO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

function isActiveProcess(status?: string): boolean {
  return status?.toUpperCase() === 'ACTIVE';
}

export function ProcessListTable({
  processes,
  canDelete = false,
  isDeleteBusy = false,
  onDeleteRequest,
}: ProcessListTableProps) {
  const navigate = useNavigate();

  const handleRowNavigate = (processId: string) => {
    navigate(`/procesos/${processId}`);
  };

  if (processes.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-gray-300 bg-body px-8 py-16 text-center">
        <FolderOpen className="mb-4 text-primary-400" size={48} />
        <h3 className="text-heading-md font-semibold text-primary-800">Sin procesos visibles</h3>
        <p className="mt-2 max-w-md text-body-md text-gray-600">
          No hay procesos de acreditación en su alcance. Si es Jefe de Departamento, puede iniciar
          uno nuevo.
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-hidden rounded-2xl border border-gray-200 bg-body shadow-sm">
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-primary-50">
            <tr>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Carrera
              </th>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Plantilla
              </th>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Estado
              </th>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Inicio
              </th>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Responsable
              </th>
              <th className="px-6 py-4 text-left text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Estructura
              </th>
              {canDelete && (
                <th className="w-16 px-4 py-4 text-right text-label-md font-semibold uppercase tracking-wide text-primary-700">
                  <span className="sr-only">Eliminar</span>
                </th>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {processes.map((process) => {
              const processId = process.id;
              const showDelete =
                canDelete && processId && isActiveProcess(process.status) && onDeleteRequest;

              return (
                <tr
                  key={processId ?? process.careerCode}
                  className="cursor-pointer transition-colors hover:bg-primary-50 focus-within:bg-primary-50"
                  onClick={() => {
                    if (processId) {
                      handleRowNavigate(processId);
                    }
                  }}
                  onKeyDown={(event) => {
                    if (!processId) return;
                    if (event.key === 'Enter' || event.key === ' ') {
                      event.preventDefault();
                      handleRowNavigate(processId);
                    }
                  }}
                  tabIndex={processId ? 0 : -1}
                  role="link"
                  aria-label={
                    processId
                      ? `Ver detalle del proceso ${process.careerName ?? process.careerCode ?? ''}`
                      : undefined
                  }
                >
                  <td className="px-6 py-4">
                    <p className="text-body-md font-semibold text-primary-900">
                      {process.careerName ?? '—'}
                    </p>
                    <p className="text-label-md text-gray-500">{process.careerCode ?? '—'}</p>
                  </td>
                  <td className="px-6 py-4">
                    <p className="text-body-md text-gray-800">{process.templateName ?? '—'}</p>
                    <p className="text-label-md text-gray-500">{process.templateType ?? '—'}</p>
                  </td>
                  <td className="px-6 py-4">
                    <ProcessStatusBadge status={process.status ?? 'UNKNOWN'} />
                  </td>
                  <td className="px-6 py-4 text-body-md text-gray-700">
                    {formatDate(process.startDate)}
                  </td>
                  <td className="px-6 py-4 text-body-md text-gray-700">
                    {process.responsible?.fullName ?? '—'}
                  </td>
                  <td className="px-6 py-4 text-body-md text-gray-700">
                    {process.level1Count ?? 0} N1 · {process.indicatorCount ?? 0} indicadores
                  </td>
                  {canDelete && (
                    <td className="px-4 py-4 text-right">
                      {showDelete && (
                        <button
                          type="button"
                          className="inline-flex rounded-lg p-2 text-gray-500 transition-colors hover:bg-danger/10 hover:text-danger disabled:cursor-not-allowed disabled:opacity-50"
                          aria-label={`Eliminar proceso ${process.careerName ?? ''}`}
                          disabled={isDeleteBusy}
                          onClick={(event) => {
                            event.stopPropagation();
                            onDeleteRequest(process);
                          }}
                        >
                          <Trash2 size={18} />
                        </button>
                      )}
                    </td>
                  )}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}
