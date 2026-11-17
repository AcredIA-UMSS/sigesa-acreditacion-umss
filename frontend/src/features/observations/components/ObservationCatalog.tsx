import type { ProcessObservation } from '../hooks/useProcessObservations';
import { formatWhen } from '../model/observationView';
import { ObservationBadge } from './ObservationBadge';

interface ObservationCatalogProps {
  items: ProcessObservation[];
  selectedId: string | null;
  isLoading: boolean;
  evidenceLabelFor: (evidenceId: string) => string;
  onSelect: (item: ProcessObservation) => void;
}

export function ObservationCatalog({
  items,
  selectedId,
  isLoading,
  evidenceLabelFor,
  onSelect,
}: ObservationCatalogProps) {
  if (isLoading) {
    return <p className="py-8 text-body-md text-gray-600">Cargando observaciones…</p>;
  }

  if (items.length === 0) {
    return (
      <p className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-body-md text-gray-600">
        No hay observaciones con esos filtros.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-body-md">
        <thead className="text-label-md uppercase text-gray-600">
          <tr>
            <th className="py-2 pr-3">Estado</th>
            <th className="py-2 pr-3">Evidencia e indicador</th>
            <th className="py-2 pr-3">Cumplimiento</th>
            <th className="py-2 pr-3">Prioridad</th>
            <th className="py-2">Actualización</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => {
            const selected = item.id === selectedId;
            return (
              <tr
                key={item.id}
                tabIndex={0}
                aria-selected={selected}
                className={`cursor-pointer border-t border-gray-200 hover:bg-primary-50 ${
                  selected ? 'bg-primary-50' : ''
                }`}
                onClick={() => onSelect(item)}
                onKeyDown={(event) => {
                  if (event.key === 'Enter' || event.key === ' ') {
                    event.preventDefault();
                    onSelect(item);
                  }
                }}
              >
                <td className="py-3 pr-3"><ObservationBadge kind="status" value={item.status} /></td>
                <td className="max-w-xs py-3 pr-3">
                  <p className="font-medium text-gray-900">{evidenceLabelFor(item.evidenceId)}</p>
                  <p className="mt-1 line-clamp-2 text-gray-600">{item.description}</p>
                </td>
                <td className="py-3 pr-3"><ObservationBadge kind="compliance" value={item.compliance} /></td>
                <td className="py-3 pr-3"><ObservationBadge kind="priority" value={item.priority} /></td>
                <td className="whitespace-nowrap py-3 text-gray-600">{formatWhen(item.updatedAt)}</td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
