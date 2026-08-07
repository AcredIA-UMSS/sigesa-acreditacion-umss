import { Link } from 'react-router-dom';
import { ArrowRight, FolderOpen } from 'lucide-react';
import type { ProcessSummaryResponseDto } from '../../../api/model';
import { ProcessStatusBadge } from './ProcessStatusBadge';

interface ProcessListTableProps {
  processes: ProcessSummaryResponseDto[];
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

export function ProcessListTable({ processes }: ProcessListTableProps) {
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
              <th className="px-6 py-4 text-right text-label-md font-semibold uppercase tracking-wide text-primary-700">
                Acción
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {processes.map((process) => (
              <tr key={process.id} className="transition-colors hover:bg-gray-50">
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
                  {process.phaseCount ?? 0} fases · {process.subphaseCount ?? 0} subfases
                </td>
                <td className="px-6 py-4 text-right">
                  {process.id && (
                    <Link
                      to={`/procesos/${process.id}`}
                      className="inline-flex items-center gap-1 text-label-md font-medium text-primary-600 hover:text-primary-800"
                    >
                      Ver detalle
                      <ArrowRight size={16} />
                    </Link>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
