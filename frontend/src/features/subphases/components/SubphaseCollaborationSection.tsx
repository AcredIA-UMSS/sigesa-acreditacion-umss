import { useState } from 'react';
import { useSubphaseCollaboration } from '../hooks/useSubphaseCollaboration';
import { SubphaseEvidenceList, SubphaseObservationPanel } from './SubphaseObservationPanel';
import { SubphaseReviewActions } from './SubphaseReviewActions';
import { SubphaseSubsanationModal } from './SubphaseSubsanationModal';
import { SubphaseEvidenceUploadSlot } from '../../processes/components/SubphaseEvidenceUploadSlot';

export type SubphaseCollaborationSectionProps = {
  processId: string;
  phaseName: string;
  subphaseId?: string;
  subphaseName: string;
  canUpload: boolean;
  canObserve: boolean;
  canReview: boolean;
  canSubsanate: boolean;
};

export function SubphaseCollaborationSection({
  processId,
  phaseName,
  subphaseId,
  subphaseName,
  canUpload,
  canObserve,
  canReview,
  canSubsanate,
}: SubphaseCollaborationSectionProps) {
  const {
    evidences,
    observations,
    eligibility,
    hasOpenObservation,
    isLoading,
    error,
    reload,
    postObservation,
  } = useSubphaseCollaboration(subphaseId, canSubsanate);

  const [subsanationTarget, setSubsanationTarget] = useState<{
    evidenceId: string;
    filename: string;
  } | null>(null);

  const effectiveCanUpload = canUpload && !hasOpenObservation;
  const effectiveCanObserve = canObserve && !canReview && !hasOpenObservation;
  const openObservationId = eligibility?.openObservationId;

  return (
    <div className="mt-3 space-y-1">
      {error && (
        <p className="text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {hasOpenObservation && (
        <p className="rounded-md border border-warning/30 bg-warning/10 px-3 py-2 text-body-md text-gray-800">
          Hay una observación pendiente. El coordinador debe subsanar la evidencia; no se permiten
          cargas nuevas ni observaciones adicionales hasta resolverla.
        </p>
      )}

      <SubphaseEvidenceList
        evidences={evidences}
        isLoading={isLoading}
        canSubsanate={Boolean(eligibility?.canSubsanate && openObservationId)}
        onSubsanate={(evidenceId, filename) => setSubsanationTarget({ evidenceId, filename })}
      />

      {effectiveCanUpload && (
        <SubphaseEvidenceUploadSlot
          processId={processId}
          phaseName={phaseName}
          subphaseId={subphaseId}
          subphaseName={subphaseName}
          canUpload={effectiveCanUpload}
          onUploaded={() => void reload()}
        />
      )}

      <SubphaseObservationPanel
        subphaseId={subphaseId}
        canObserve={effectiveCanObserve}
        observations={observations}
        isLoading={isLoading}
        onSubmitObservation={postObservation}
      />

      {canReview && (
        <SubphaseReviewActions
          subphaseId={subphaseId}
          subphaseName={subphaseName}
          hasEvidences={evidences.length > 0}
          hasOpenObservation={hasOpenObservation}
          onCompleted={() => void reload()}
        />
      )}

      {subsanationTarget && subphaseId && openObservationId && (
        <SubphaseSubsanationModal
          isOpen
          subphaseId={subphaseId}
          subphaseName={subphaseName}
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
