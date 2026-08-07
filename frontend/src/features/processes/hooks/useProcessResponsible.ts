import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import {
  getGetProcessQueryKey,
  getListProcessesQueryKey,
  useAssignResponsible,
  useListCandidates,
  useRemoveResponsible,
} from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type { EligibleResponsibleDto, ProcessResponsibleDto } from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';

interface UseProcessResponsibleOptions {
  processId: string;
  canManage: boolean;
  isModalOpen: boolean;
}

interface UseProcessResponsibleReturn {
  candidates: EligibleResponsibleDto[];
  isLoadingCandidates: boolean;
  selectedUserId: string;
  setSelectedUserId: (userId: string) => void;
  actionError: string | null;
  isAssigning: boolean;
  isRemoving: boolean;
  assignResponsible: () => Promise<boolean>;
  removeResponsible: () => Promise<boolean>;
}

export function useProcessResponsible({
  processId,
  canManage,
  isModalOpen,
}: UseProcessResponsibleOptions): UseProcessResponsibleReturn {
  const queryClient = useQueryClient();
  const [selectedUserId, setSelectedUserId] = useState('');
  const [actionError, setActionError] = useState<string | null>(null);

  const candidatesQuery = useListCandidates(processId, {
    query: {
      enabled: canManage && isModalOpen,
    },
  });

  const candidates =
    candidatesQuery.data?.status === 200 ? candidatesQuery.data.data : [];

  const invalidateProcessQueries = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: getGetProcessQueryKey(processId) });
    await queryClient.invalidateQueries({ queryKey: getListProcessesQueryKey() });
  }, [processId, queryClient]);

  const assignMutation = useAssignResponsible();
  const removeMutation = useRemoveResponsible();

  const assignResponsible = useCallback(async (): Promise<boolean> => {
    if (!selectedUserId) {
      setActionError('Seleccione un coordinador de la lista.');
      return false;
    }

    setActionError(null);
    try {
      const response = await assignMutation.mutateAsync({
        processId,
        data: { userId: selectedUserId },
      });
      if (response.status !== 200) {
        setActionError('No se pudo asignar el responsable.');
        return false;
      }
      await invalidateProcessQueries();
      return true;
    } catch (error) {
      setActionError(getApiErrorMessage(error));
      return false;
    }
  }, [assignMutation, invalidateProcessQueries, processId, selectedUserId]);

  const removeResponsible = useCallback(async (): Promise<boolean> => {
    setActionError(null);
    try {
      const response = await removeMutation.mutateAsync({ processId });
      if (response.status !== 204) {
        setActionError('No se pudo quitar el responsable.');
        return false;
      }
      await invalidateProcessQueries();
      return true;
    } catch (error) {
      setActionError(getApiErrorMessage(error));
      return false;
    }
  }, [invalidateProcessQueries, processId, removeMutation]);

  return {
    candidates,
    isLoadingCandidates: candidatesQuery.isLoading,
    selectedUserId,
    setSelectedUserId,
    actionError,
    isAssigning: assignMutation.isPending,
    isRemoving: removeMutation.isPending,
    assignResponsible,
    removeResponsible,
  };
}

export type { ProcessResponsibleDto };
