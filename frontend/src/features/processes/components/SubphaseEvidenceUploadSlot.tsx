import { useState } from 'react';
import type { UploadableIndicatorDto } from '../../evidence/api/fetchUploadableIndicators';
import { SubphaseEvidenceUploadModal } from './SubphaseEvidenceUploadModal';

export type SubphaseEvidenceUploadSlotProps = {
  processId: string;
  phaseName: string;
  subphaseId?: string;
  subphaseName: string;
  canUpload: boolean;
  indicators: UploadableIndicatorDto[];
  indicatorsLoading: boolean;
  indicatorsError: string | null;
};

export function SubphaseEvidenceUploadSlot(props: SubphaseEvidenceUploadSlotProps) {
  const [modalOpen, setModalOpen] = useState(false);

  return (
    <>
      <button
        type="button"
        onClick={() => setModalOpen(true)}
        className="mt-2 text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2 transition-colors hover:text-primary-800"
      >
        {props.canUpload ? 'Subir evidencia' : 'Cargar evidencia'}
      </button>

      <SubphaseEvidenceUploadModal
        {...props}
        isOpen={modalOpen}
        onClose={() => setModalOpen(false)}
      />
    </>
  );
}
