import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import {
  useAddPhase,
  useAddSubphase,
  useDeletePhase,
  useDeleteSubphase,
  useUpdatePhase,
  useUpdateSubphase,
} from '../../../api/endpoints/estructura-de-proceso/estructura-de-proceso';
import { getGetProcessQueryKey } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type {
  CreatePhaseRequestDto,
  CreateSubphaseRequestDto,
  UpdatePhaseRequestDto,
  UpdateSubphaseRequestDto,
} from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useProcessDetail } from './useProcessDetail';

export function useProcessStructureEditor(processId: string) {
  const queryClient = useQueryClient();
  const detail = useProcessDetail(processId);
  const [actionError, setActionError] = useState<string | null>(null);

  const invalidateDetail = useCallback(async () => {
    await queryClient.invalidateQueries({ queryKey: getGetProcessQueryKey(processId) });
  }, [processId, queryClient]);

  const wrapMutation = <TArgs, TResult>(
    mutateAsync: (args: TArgs) => Promise<TResult>,
  ) => {
    return async (args: TArgs): Promise<boolean> => {
      setActionError(null);
      try {
        await mutateAsync(args);
        await invalidateDetail();
        return true;
      } catch (error) {
        setActionError(getApiErrorMessage(error));
        return false;
      }
    };
  };

  const addPhaseMutation = useAddPhase();
  const updatePhaseMutation = useUpdatePhase();
  const deletePhaseMutation = useDeletePhase();
  const addSubphaseMutation = useAddSubphase();
  const updateSubphaseMutation = useUpdateSubphase();
  const deleteSubphaseMutation = useDeleteSubphase();

  const isEditable = detail.process?.status === 'ACTIVE';

  const isBusy =
    addPhaseMutation.isPending ||
    updatePhaseMutation.isPending ||
    deletePhaseMutation.isPending ||
    addSubphaseMutation.isPending ||
    updateSubphaseMutation.isPending ||
    deleteSubphaseMutation.isPending;

  return {
    ...detail,
    actionError,
    isEditable,
    isBusy,
    addPhase: wrapMutation((data: CreatePhaseRequestDto) =>
      addPhaseMutation.mutateAsync({ processId, data }),
    ),
    updatePhase: wrapMutation(
      (args: { phaseId: string; data: UpdatePhaseRequestDto }) =>
        updatePhaseMutation.mutateAsync({ processId, phaseId: args.phaseId, data: args.data }),
    ),
    deletePhase: wrapMutation((phaseId: string) =>
      deletePhaseMutation.mutateAsync({ processId, phaseId }),
    ),
    addSubphase: wrapMutation(
      (args: { phaseId: string; data: CreateSubphaseRequestDto }) =>
        addSubphaseMutation.mutateAsync({
          processId,
          phaseId: args.phaseId,
          data: args.data,
        }),
    ),
    updateSubphase: wrapMutation(
      (args: { phaseId: string; subphaseId: string; data: UpdateSubphaseRequestDto }) =>
        updateSubphaseMutation.mutateAsync({
          processId,
          phaseId: args.phaseId,
          subphaseId: args.subphaseId,
          data: args.data,
        }),
    ),
    deleteSubphase: wrapMutation(
      (args: { phaseId: string; subphaseId: string }) =>
        deleteSubphaseMutation.mutateAsync({
          processId,
          phaseId: args.phaseId,
          subphaseId: args.subphaseId,
        }),
    ),
  };
}
