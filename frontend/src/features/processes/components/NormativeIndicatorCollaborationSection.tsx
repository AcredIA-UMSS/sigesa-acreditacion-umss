import { useState } from 'react';
import { FileText } from 'lucide-react';
import { useNormativeIndicatorCollaboration } from '../../evidence/hooks/useNormativeIndicatorCollaboration';
import { NormativeIndicatorEvidenceUploadSlot } from '../components/NormativeIndicatorEvidenceUploadSlot';
import { NormativeIndicatorReviewActions } from '../components/NormativeIndicatorReviewActions';
import { NormativeIndicatorSubsanationModal } from '../../evidence/components/NormativeIndicatorSubsanationModal';

export type NormativeIndicatorCollaborationSectionProps = {
  indicatorId: string;
  indicatorLabel: string;
  indicatorStatus?: string;
  breadcrumb: string;
  canUpload: boolean;
  canSubsanate: boolean;
  canReview: boolean;
};

function NormativeIndicatorEvidenceList({
  evidences,
  isLoading,
  canSubsanate,
  onSubsanate,
}: {
  evidences: ReturnType<typeof useNormativeIndicatorCollaboration>['evidences'];
  isLoading: boolean;
  canSubsanate: boolean;
  onSubsanate?: (evidenceId: string, filename: string) => void;
}) {
  return (
    <div className="rounded-lg border border-primary-100 bg-primary-50/40 p-3">
      <div className="mb-2 flex items-center gap-2 text-label-md font-semibold uppercase text-primary-700">
        <FileText size={16} aria-hidden />
        Evidencias cargadas ({evidences.length})
      </div>
      {isLoading && evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Cargando evidencias…</p>
      ) : evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Sin evidencias cargadas.</p>
      ) : (
        <ul className="space-y-2">
          {evidences.map((item) => (
            <li
              key={item.evidenceId}
              className="rounded-md border border-primary-100 bg-body px-3 py-2 text-body-md text-gray-800"
            >
              <p className="font-medium">{item.originalFilename}</p>
              <p className="text-body-md text-gray-600">{item.description}</p>
              <p className="mt-1 text-label-md text-gray-500">
                v{item.version}
                {item.externalUrl ? ` · ${item.externalUrl}` : ''}
              </p>
              {canSubsanate && onSubsanate && (
                <button
                  type="button"
                  className="mt-2 text-body-md font-medium text-secondary-600 underline decoration-secondary-400 underline-offset-2 transition-colors hover:text-secondary-800"
                  onClick={() => onSubsanate(item.evidenceId, item.originalFilename)}
                >
                  Subsanar evidencia
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

export function NormativeIndicatorCollaborationSection({
  indicatorId,
  indicatorLabel,
  indicatorStatus,
  breadcrumb,
  canUpload,
  canSubsanate,
  canReview,
}: NormativeIndicatorCollaborationSectionProps) {
  const { evidences, eligibility, hasOpenObservation, isLoading, error, reload } =
    useNormativeIndicatorCollaboration(indicatorId, canSubsanate);

  const [subsanationTarget, setSubsanationTarget] = useState<{
    evidenceId: string;
    filename: string;
  } | null>(null);

  const effectiveHasOpenObservation =
    hasOpenObservation || indicatorStatus === 'OBSERVADO';
  const effectiveCanUpload = canUpload && !effectiveHasOpenObservation;
  const openObservationId = eligibility?.openObservationId;

  return (
    <div className="mt-3 space-y-2">
      {error && (
        <p className="text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {effectiveHasOpenObservation && (
        <p className="rounded-md border border-warning/30 bg-warning/10 px-3 py-2 text-body-md text-gray-800">
          Hay una observación pendiente. Debe subsanar la evidencia; no se permiten cargas nuevas
          hasta resolverla.
        </p>
      )}

      <NormativeIndicatorEvidenceList
        evidences={evidences}
        isLoading={isLoading}
        canSubsanate={Boolean(eligibility?.canSubsanate && openObservationId)}
        onSubsanate={(evidenceId, filename) => setSubsanationTarget({ evidenceId, filename })}
      />

      <NormativeIndicatorEvidenceUploadSlot
        indicatorId={indicatorId}
        indicatorLabel={indicatorLabel}
        breadcrumb={breadcrumb}
        canUpload={effectiveCanUpload}
        onUploaded={() => void reload()}
      />

      {canReview && (
        <NormativeIndicatorReviewActions
          indicatorId={indicatorId}
          indicatorLabel={indicatorLabel}
          indicatorStatus={indicatorStatus}
          hasEvidences={evidences.length > 0}
          onCompleted={() => void reload()}
        />
      )}

      {subsanationTarget && openObservationId && (
        <NormativeIndicatorSubsanationModal
          isOpen
          indicatorId={indicatorId}
          indicatorLabel={indicatorLabel}
          evidenceId={subsanationTarget.evidenceId}
          evidenceFilename={subsanationTarget.filename}
          observationId={openObservationId}
          onClose={() => setSubsanationTarget(null)}
          onSubsanated={() => {
            setSubsanationTarget(null);
            void reload();
          }}
        />
      )}
    </div>
  );
}
