import { useState } from 'react';
import { useAuth } from '../../../lib/auth/useAuth';
import type { ProcessResponseDto } from '../../../api/model';
import { AssignResponsibleModalUI, ProcessResponsibleSectionUI } from './ProcessResponsibleSection';
import { useProcessResponsible } from '../hooks/useProcessResponsible';

interface ProcessResponsibleContainerProps {
  processId: string;
  process: ProcessResponseDto;
  onUpdated: () => void;
}

export function ProcessResponsibleContainer({
  processId,
  process,
  onUpdated,
}: ProcessResponsibleContainerProps) {
  const { session } = useAuth();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const canManage = session?.role === 'JD' && process.status === 'ACTIVE';

  const {
    candidates,
    isLoadingCandidates,
    selectedUserId,
    setSelectedUserId,
    actionError,
    isAssigning,
    isRemoving,
    assignResponsible,
    removeResponsible,
  } = useProcessResponsible({
    processId,
    canManage,
    isModalOpen,
  });

  const handleAssign = async () => {
    const ok = await assignResponsible();
    if (ok) {
      setIsModalOpen(false);
      setSelectedUserId('');
      onUpdated();
    }
  };

  const handleRemove = async () => {
    const ok = await removeResponsible();
    if (ok) {
      onUpdated();
    }
  };

  return (
    <>
      <ProcessResponsibleSectionUI
        responsible={process.responsible}
        canManage={canManage}
        isRemoving={isRemoving}
        onOpenAssign={() => {
          setSelectedUserId(process.responsible?.userId ?? '');
          setIsModalOpen(true);
        }}
        onRemove={() => {
          void handleRemove();
        }}
      />

      <AssignResponsibleModalUI
        isOpen={isModalOpen}
        candidates={candidates}
        selectedUserId={selectedUserId}
        isLoadingCandidates={isLoadingCandidates}
        isSubmitting={isAssigning}
        submitError={actionError}
        onClose={() => {
          setIsModalOpen(false);
          setSelectedUserId('');
        }}
        onSelectUser={setSelectedUserId}
        onSubmit={() => {
          void handleAssign();
        }}
      />
    </>
  );
}
