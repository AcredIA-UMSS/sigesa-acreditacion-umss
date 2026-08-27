import { ChevronDown, ExternalLink, Layers, ListChecks } from 'lucide-react';
import { useState } from 'react';
import type { PhaseDto } from '../../../api/model';
import { PhaseCloseAction } from '../../phases/components/PhaseCloseAction';
import { SubphaseCollaborationSection } from '../../subphases/components/SubphaseCollaborationSection';

interface ProcessPhaseTreeProps {
  phases: PhaseDto[];
  processId: string;
  canUploadEvidence?: boolean;
  canObserveEvidence?: boolean;
  canReviewEvidence?: boolean;
  canSubsanateEvidence?: boolean;
  canClosePhase?: boolean;
  onStructureUpdated?: () => void;
  onNavigateToSubphase?: (subphaseId: string) => void;
}

export function ProcessPhaseTree({
  phases,
  processId,
  canUploadEvidence = false,
  canObserveEvidence = false,
  canReviewEvidence = false,
  canSubsanateEvidence = false,
  canClosePhase = false,
  onStructureUpdated,
  onNavigateToSubphase,
}: ProcessPhaseTreeProps) {
  const sortedPhases = [...phases].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));

  if (sortedPhases.length === 0) {
    return (
      <p className="text-body-md text-gray-600">Este proceso no tiene fases registradas.</p>
    );
  }

  return (
    <div className="space-y-3">
      {sortedPhases.map((phase) => (
        <PhaseAccordion
          key={phase.id ?? phase.name}
          phase={phase}
          processId={processId}
          canUploadEvidence={canUploadEvidence}
          canObserveEvidence={canObserveEvidence}
          canReviewEvidence={canReviewEvidence}
          canSubsanateEvidence={canSubsanateEvidence}
          canClosePhase={canClosePhase}
          onStructureUpdated={onStructureUpdated}
          onNavigateToSubphase={onNavigateToSubphase}
        />
      ))}
    </div>
  );
}

function PhaseAccordion({
  phase,
  processId,
  canUploadEvidence,
  canObserveEvidence,
  canReviewEvidence,
  canSubsanateEvidence,
  canClosePhase,
  onStructureUpdated,
  onNavigateToSubphase,
}: {
  phase: PhaseDto;
  processId: string;
  canUploadEvidence: boolean;
  canObserveEvidence: boolean;
  canReviewEvidence: boolean;
  canSubsanateEvidence: boolean;
  canClosePhase: boolean;
  onStructureUpdated?: () => void;
  onNavigateToSubphase?: (subphaseId: string) => void;
}) {
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
              Fase {phase.order ?? '—'} · {subphases.length} subfase
              {subphases.length === 1 ? '' : 's'}
              {phase.status && (
                <span className="ml-2 rounded-full bg-primary-100 px-2 py-0.5 text-label-md font-medium text-primary-800">
                  {phase.status}
                </span>
              )}
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
              id={sub.id ? `subphase-${sub.id}` : undefined}
              className="scroll-mt-24 px-5 py-4 transition-shadow"
            >
              <div className="flex flex-col gap-1 sm:flex-row sm:items-start sm:justify-between">
                <div>
                  <p className="text-body-md font-medium text-gray-800">{sub.name}</p>
                  {sub.description && (
                    <p className="text-body-md text-gray-500">{sub.description}</p>
                  )}
                  {sub.requirements && (
                    <div className="mt-2 rounded-md border border-gray-200 bg-gray-50 px-3 py-2">
                      <p className="flex items-center gap-1 text-label-md font-semibold uppercase text-gray-700">
                        <ListChecks size={14} aria-hidden />
                        Requisitos para completar
                      </p>
                      <p className="mt-1 whitespace-pre-wrap text-body-md text-gray-700">
                        {sub.requirements}
                      </p>
                    </div>
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
                <span className="text-label-md text-gray-500">
                  Orden {sub.order ?? '—'}
                </span>
              </div>

              <SubphaseCollaborationSection
                processId={processId}
                phaseName={phase.name ?? 'Fase'}
                subphaseId={sub.id}
                subphaseName={sub.name ?? 'Subfase'}
                canUpload={canUploadEvidence}
                canObserve={canObserveEvidence}
                canReview={canReviewEvidence}
                canSubsanate={canSubsanateEvidence}
              />
            </li>
          ))}
          {subphases.length === 0 && (
            <li className="px-5 py-3 text-body-md text-gray-500">Sin subfases</li>
          )}
        </ul>
      )}

      {open && canClosePhase && (
        <PhaseCloseAction
          processId={processId}
          phaseId={phase.id}
          phaseName={phase.name ?? 'Fase'}
          phaseStatus={phase.status}
          onCompleted={() => onStructureUpdated?.()}
          onNavigateToSubphase={onNavigateToSubphase}
        />
      )}
    </div>
  );
}
