import { ChevronDown, ExternalLink, Layers } from 'lucide-react';
import { useState } from 'react';
import type { PhaseDto } from '../../../api/model';

interface ProcessPhaseTreeProps {
  phases: PhaseDto[];
}

export function ProcessPhaseTree({ phases }: ProcessPhaseTreeProps) {
  const sortedPhases = [...phases].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));

  if (sortedPhases.length === 0) {
    return (
      <p className="text-body-md text-gray-600">Este proceso no tiene fases registradas.</p>
    );
  }

  return (
    <div className="space-y-3">
      {sortedPhases.map((phase) => (
        <PhaseAccordion key={phase.id ?? phase.name} phase={phase} />
      ))}
    </div>
  );
}

function PhaseAccordion({ phase }: { phase: PhaseDto }) {
  const [open, setOpen] = useState(true);
  const subphases = [...(phase.subphases ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );

  return (
    <div className="overflow-hidden rounded-xl border border-gray-200 bg-body shadow-sm">
      <button
        type="button"
        onClick={() => setOpen((prev) => !prev)}
        className="flex w-full items-center justify-between gap-3 bg-primary-50 px-5 py-4 text-left transition-colors hover:bg-primary-100"
      >
        <div className="flex items-center gap-3">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-primary-600 text-body">
            <Layers size={18} />
          </div>
          <div>
            <p className="text-heading-sm font-semibold text-primary-800">{phase.name}</p>
            <p className="text-label-md text-gray-600">
              Fase {phase.order ?? '—'} · {subphases.length} subfase{subphases.length === 1 ? '' : 's'}
            </p>
          </div>
        </div>
        <ChevronDown
          size={20}
          className={`text-primary-600 transition-transform ${open ? 'rotate-180' : ''}`}
        />
      </button>

      {open && (
        <ul className="divide-y divide-gray-100 border-t border-gray-100">
          {subphases.map((sub) => (
            <li
              key={sub.id ?? `${phase.id}-${sub.order}`}
              className="flex flex-col gap-1 px-5 py-3 sm:flex-row sm:items-center sm:justify-between"
            >
              <div>
                <p className="text-body-md text-gray-800">{sub.name}</p>
                {sub.description && (
                  <p className="text-body-md text-gray-500">{sub.description}</p>
                )}
                {sub.referenceUrl && (
                  <a
                    href={sub.referenceUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="mt-1 inline-flex items-center gap-1 text-body-md text-primary-600 hover:text-primary-800"
                  >
                    <ExternalLink size={14} />
                    Referencia normativa
                  </a>
                )}
              </div>
              <span className="text-label-md text-gray-500">Orden {sub.order ?? '—'}</span>
            </li>
          ))}
          {subphases.length === 0 && (
            <li className="px-5 py-3 text-body-md text-gray-500">Sin subfases</li>
          )}
        </ul>
      )}
    </div>
  );
}
