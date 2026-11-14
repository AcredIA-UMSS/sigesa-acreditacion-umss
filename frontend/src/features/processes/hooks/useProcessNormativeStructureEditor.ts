import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import {
  useAddIndicator1 as useAddIndicator,
  useAddLevel11 as useAddLevel1,
  useAddLevel21 as useAddLevel2,
  useAddLevel31 as useAddLevel3,
  useDeleteIndicator1 as useDeleteIndicator,
  useDeleteLevel11 as useDeleteLevel1,
  useDeleteLevel21 as useDeleteLevel2,
  useDeleteLevel31 as useDeleteLevel3,
  useUpdateIndicator1 as useUpdateIndicator,
  useUpdateLevel11 as useUpdateLevel1,
  useUpdateLevel21 as useUpdateLevel2,
  useUpdateLevel31 as useUpdateLevel3,
} from '../../../api/endpoints/jerarquía-normativa-v2/jerarquía-normativa-v2';
import { getGetProcessQueryKey } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type {
  CreateLevel1NodeRequestDto,
  CreateNormativeIndicatorRequestDto,
  CreateNormativeNodeRequestDto,
  UpdateLevel1NodeRequestDto,
  UpdateNormativeIndicatorRequestDto,
  UpdateNormativeNodeRequestDto,
} from '../../../api/model';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useProcessDetail } from './useProcessDetail';

export function useProcessNormativeStructureEditor(processId: string) {
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

  const addLevel1Mutation = useAddLevel1();
  const updateLevel1Mutation = useUpdateLevel1();
  const deleteLevel1Mutation = useDeleteLevel1();
  const addLevel2Mutation = useAddLevel2();
  const updateLevel2Mutation = useUpdateLevel2();
  const deleteLevel2Mutation = useDeleteLevel2();
  const addLevel3Mutation = useAddLevel3();
  const updateLevel3Mutation = useUpdateLevel3();
  const deleteLevel3Mutation = useDeleteLevel3();
  const addIndicatorMutation = useAddIndicator();
  const updateIndicatorMutation = useUpdateIndicator();
  const deleteIndicatorMutation = useDeleteIndicator();

  const isEditable = detail.process?.status === 'ACTIVE';

  const isBusy =
    addLevel1Mutation.isPending ||
    updateLevel1Mutation.isPending ||
    deleteLevel1Mutation.isPending ||
    addLevel2Mutation.isPending ||
    updateLevel2Mutation.isPending ||
    deleteLevel2Mutation.isPending ||
    addLevel3Mutation.isPending ||
    updateLevel3Mutation.isPending ||
    deleteLevel3Mutation.isPending ||
    addIndicatorMutation.isPending ||
    updateIndicatorMutation.isPending ||
    deleteIndicatorMutation.isPending;

  return {
    ...detail,
    actionError,
    isEditable,
    isBusy,
    addLevel1: wrapMutation((data: CreateLevel1NodeRequestDto) =>
      addLevel1Mutation.mutateAsync({ processId, data }),
    ),
    updateLevel1: wrapMutation(
      (args: { level1Id: string; data: UpdateLevel1NodeRequestDto }) =>
        updateLevel1Mutation.mutateAsync({
          processId,
          level1Id: args.level1Id,
          data: args.data,
        }),
    ),
    deleteLevel1: wrapMutation((level1Id: string) =>
      deleteLevel1Mutation.mutateAsync({ processId, level1Id }),
    ),
    addLevel2: wrapMutation(
      (args: { level1Id: string; data: CreateNormativeNodeRequestDto }) =>
        addLevel2Mutation.mutateAsync({ level1Id: args.level1Id, data: args.data }),
    ),
    updateLevel2: wrapMutation(
      (args: { level2Id: string; data: UpdateNormativeNodeRequestDto }) =>
        updateLevel2Mutation.mutateAsync({ level2Id: args.level2Id, data: args.data }),
    ),
    deleteLevel2: wrapMutation((level2Id: string) =>
      deleteLevel2Mutation.mutateAsync({ level2Id }),
    ),
    addLevel3: wrapMutation(
      (args: { level2Id: string; data: CreateNormativeNodeRequestDto }) =>
        addLevel3Mutation.mutateAsync({ level2Id: args.level2Id, data: args.data }),
    ),
    updateLevel3: wrapMutation(
      (args: { level3Id: string; data: UpdateNormativeNodeRequestDto }) =>
        updateLevel3Mutation.mutateAsync({ level3Id: args.level3Id, data: args.data }),
    ),
    deleteLevel3: wrapMutation((level3Id: string) =>
      deleteLevel3Mutation.mutateAsync({ level3Id }),
    ),
    addIndicator: wrapMutation(
      (args: { level3Id: string; data: CreateNormativeIndicatorRequestDto }) =>
        addIndicatorMutation.mutateAsync({ level3Id: args.level3Id, data: args.data }),
    ),
    updateIndicator: wrapMutation(
      (args: { indicatorId: string; data: UpdateNormativeIndicatorRequestDto }) =>
        updateIndicatorMutation.mutateAsync({
          indicatorId: args.indicatorId,
          data: args.data,
        }),
    ),
    deleteIndicator: wrapMutation((indicatorId: string) =>
      deleteIndicatorMutation.mutateAsync({ indicatorId }),
    ),
  };
}
