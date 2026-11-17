import { useMemo } from 'react';
import type { ProcessSummaryResponseDto } from '../api/model';
import { useProcessList } from '../features/processes/hooks/useProcessList';

export interface RecentActivityItem {
  id: string;
  name: string;
  status: string;
  date: string;
}

export const MOCK_RECENT_ACTIVITIES: RecentActivityItem[] = [
  {
    id: 'mock-act-1',
    name: 'Autoevaluación Ingeniería Civil',
    status: 'Activo',
    date: '12 oct 2026, 09:30',
  },
  {
    id: 'mock-act-2',
    name: 'Carga de evidencias Sistemas',
    status: 'En proceso',
    date: '11 oct 2026, 16:05',
  },
  {
    id: 'mock-act-3',
    name: 'Revisión técnica Electrónica',
    status: 'Pendiente',
    date: '10 oct 2026, 11:20',
  },
  {
    id: 'mock-act-4',
    name: 'Reporte ejecutivo DUEA',
    status: 'Completado',
    date: '09 oct 2026, 14:45',
  },
  {
    id: 'mock-act-5',
    name: 'Cierre de fase Química',
    status: 'Cerrado',
    date: '08 oct 2026, 08:10',
  },
];

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Activo',
  CLOSED: 'Cerrado',
  COMPLETED: 'Completado',
  CANCELLED: 'Cancelado',
  ARCHIVED: 'Archivado',
  EN_PROCESO: 'En proceso',
  PENDIENTE: 'Pendiente',
};

function formatActivityDate(iso?: string): string {
  if (!iso) {
    return '—';
  }
  try {
    return new Intl.DateTimeFormat('es-BO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

function mapProcessToActivity(process: ProcessSummaryResponseDto, index: number): RecentActivityItem {
  const normalizedStatus = process.status?.toUpperCase() ?? '';
  return {
    id: process.id ?? `process-${index}`,
    name: process.careerName?.trim() || 'Proceso sin nombre',
    status: STATUS_LABELS[normalizedStatus] ?? process.status ?? 'Desconocido',
    date: formatActivityDate(process.startDate),
  };
}

function selectRecentActivities(
  processes: ProcessSummaryResponseDto[],
  isError: boolean,
): RecentActivityItem[] {
  if (isError) {
    return MOCK_RECENT_ACTIVITIES.slice(0, 5);
  }

  return processes
    .slice()
    .sort((left, right) => {
      const leftTime = left.startDate ? Date.parse(left.startDate) : 0;
      const rightTime = right.startDate ? Date.parse(right.startDate) : 0;
      return rightTime - leftTime;
    })
    .slice(0, 5)
    .map(mapProcessToActivity);
}

export function RecentActivity() {
  const { processes, isLoading, isError } = useProcessList();
  const items = useMemo(
    () => selectRecentActivities(processes, isError),
    [processes, isError],
  );

  return (
    <section aria-labelledby="recent-activity-heading" className="recent-activity">
      <h2 id="recent-activity-heading" className="text-heading-md font-semibold text-primary-800">
        Últimas actividades
      </h2>
      <p className="mt-1 text-body-md text-gray-700">
        {isError
          ? 'No se pudo consultar el servidor. Se muestran actividades de ejemplo.'
          : 'Los 5 procesos más recientes en su alcance.'}
      </p>

      {isLoading && !isError ? (
        <ul className="mt-4 space-y-3" aria-busy="true" aria-label="Cargando actividades">
          {[0, 1, 2].map((key) => (
            <li key={key} className="h-16 animate-pulse rounded-2xl bg-gray-200" />
          ))}
        </ul>
      ) : items.length === 0 ? (
        <p className="mt-4 rounded-2xl border border-dashed border-gray-300 bg-body px-4 py-8 text-center text-body-md text-gray-600">
          No hay actividades recientes.
        </p>
      ) : (
        <ul className="mt-4 divide-y divide-gray-200 overflow-hidden rounded-2xl border border-primary-200/40 bg-body shadow-sm">
          {items.map((item) => (
            <li
              key={item.id}
              className="flex flex-col gap-1 px-4 py-3 sm:flex-row sm:items-center sm:justify-between sm:gap-4"
            >
              <div className="min-w-0">
                <p className="text-body-lg font-medium text-primary-800">{item.name}</p>
                <p className="text-label-md text-gray-600">{item.date}</p>
              </div>
              <span className="inline-flex w-fit rounded-full border border-primary-200 bg-primary-50 px-3 py-1 text-label-md font-medium text-primary-700">
                {item.status}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
