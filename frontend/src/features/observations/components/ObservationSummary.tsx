import type { ObservationCounts } from '../model/observationView';

interface ObservationSummaryProps {
  counts: ObservationCounts;
  activeStatus: string;
  onSelect: (status: string) => void;
}

const CARDS: { status: string; label: string; hint: string; countKey: keyof ObservationCounts }[] = [
  { status: 'BORRADOR', label: 'Borradores', hint: 'En elaboración', countKey: 'drafts' },
  { status: 'PENDIENTES', label: 'Pendientes', hint: 'Enviadas, en revisión o respondidas', countKey: 'pending' },
  { status: 'ATENDIDA', label: 'Atendidas', hint: 'Con acción del coordinador', countKey: 'attended' },
  { status: 'CERRADA', label: 'Cerradas', hint: 'Resueltas por el técnico', countKey: 'closed' },
];

export function ObservationSummary({ counts, activeStatus, onSelect }: ObservationSummaryProps) {
  return (
    <div className="grid grid-cols-2 gap-3 lg:grid-cols-4">
      {CARDS.map((card) => {
        const selected = activeStatus === card.status;
        return (
          <button
            key={card.status}
            type="button"
            onClick={() => onSelect(selected ? '' : card.status)}
            className={`rounded-xl border px-4 py-3 text-left transition-colors ${
              selected
                ? 'border-primary-600 bg-primary-50'
                : 'border-gray-200 bg-body hover:border-primary-300 hover:bg-primary-50'
            }`}
          >
            <p className="text-label-md text-gray-600">{card.label}</p>
            <p className="mt-1 text-heading-md font-semibold text-primary-800">{counts[card.countKey]}</p>
            <p className="mt-1 text-label-md text-gray-500">{card.hint}</p>
          </button>
        );
      })}
    </div>
  );
}
