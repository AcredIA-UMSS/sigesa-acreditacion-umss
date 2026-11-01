import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { actionLabel, actorLabel, splitOccurredAt, type ActorDirectory } from '../lib/auditDisplay';

interface AuditTimelineProps {
  rows: AuditLogResponse[];
  directory: ActorDirectory;
  selectedId: string | null;
  onSelect: (id: string) => void;
}

export function AuditTimeline({ rows, directory, selectedId, onSelect }: AuditTimelineProps) {
  if (rows.length === 0) {
    return (
      <p className="rounded-xl border border-gray-200 bg-body px-4 py-10 text-center text-body-md text-gray-500" data-testid="audit-timeline">
        No hay registros para los filtros seleccionados.
      </p>
    );
  }

  return (
    <ol className="space-y-4 border-l border-primary-200 pl-6" data-testid="audit-timeline">
      {rows.map((row) => {
        const when = splitOccurredAt(row.occurredAt);
        const selected = row.id === selectedId;
        return (
          <li key={row.id} className="relative">
            <span className="absolute -left-[1.85rem] top-1 h-3 w-3 rounded-full border-2 border-primary-600 bg-body" />
            <button
              type="button"
              onClick={() => row.id && onSelect(row.id)}
              className={`w-full rounded-xl border px-4 py-3 text-left ${
                selected ? 'border-secondary bg-primary-50' : 'border-gray-200 bg-body hover:border-primary-300'
              }`}
            >
              <p className="text-label-md text-gray-500">{when.date} · {when.time}</p>
              <p className="mt-1 text-body-md font-medium text-primary-800">{actionLabel(row.action)}</p>
              <p className="mt-1 text-body-md text-gray-700">{row.description?.trim() || '—'}</p>
              <p className="mt-1 text-label-md text-gray-500">{actorLabel(row, directory)}</p>
            </button>
          </li>
        );
      })}
    </ol>
  );
}
