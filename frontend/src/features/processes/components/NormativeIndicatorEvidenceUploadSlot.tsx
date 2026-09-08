import { useState } from 'react';
import { NormativeIndicatorEvidenceUploadModal } from '../../evidence/components/NormativeIndicatorEvidenceUploadModal';

export type NormativeIndicatorEvidenceUploadSlotProps = {
  indicatorId: string;
  indicatorLabel: string;
  breadcrumb: string;
  canUpload: boolean;
  onUploaded?: () => void;
};

export function NormativeIndicatorEvidenceUploadSlot({
  indicatorId,
  indicatorLabel,
  breadcrumb,
  canUpload,
  onUploaded,
}: NormativeIndicatorEvidenceUploadSlotProps) {
  const [modalOpen, setModalOpen] = useState(false);

  if (!canUpload) {
    return null;
  }

  return (
    <>
      <button
        type="button"
        onClick={() => setModalOpen(true)}
        className="mt-2 text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2 transition-colors hover:text-primary-800"
      >
        Subir evidencia
      </button>

      <NormativeIndicatorEvidenceUploadModal
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
        indicatorId={indicatorId}
        indicatorLabel={indicatorLabel}
        breadcrumb={breadcrumb}
        canUpload={canUpload}
        onUploaded={() => {
          onUploaded?.();
        }}
      />
    </>
  );
}
