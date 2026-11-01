import { useQueryClient } from '@tanstack/react-query';
import { useCallback, useState } from 'react';
import {
  useAddIndicator,
  useAddLevel1,
  useAddLevel2,
  useAddLevel3,
  useDeleteIndicator,
  useDeleteLevel1,
  useDeleteLevel2,
  useDeleteLevel3,
  useUpdateIndicator,
  useUpdateLevel1,
  useUpdateLevel2,
  useUpdateLevel3,
} from '../../../../api/endpoints/jerarquía-normativa-plantilla-v2/jerarquía-normativa-plantilla-v2';
import { getGetTemplateQueryKey } from '../../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import type {
  CreateLevel1NodeRequestDto,
  CreateNormativeIndicatorRequestDto,
  CreateNormativeNodeRequestDto,
  UpdateLevel1NodeRequestDto,
  UpdateNormativeIndicatorRequestDto,
  UpdateNormativeNodeRequestDto,
} from '../../../../api/model';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import { useTemplateDetail } from './useTemplateDetail';

export function useTemplateNormativeStructureEditor(templateId: string | undefined) {
  const queryClient = useQueryClient();
  const detail = useTemplateDetail(templateId);
  const [actionError, setActionError] = useState<string | null>(null);

  const invalidateDetail = useCallback(async () => {
    if (!templateId) {
      return;
    }
    await queryClient.invalidateQueries({ queryKey: getGetTemplateQueryKey(templateId) });
  }, [templateId, queryClient]);

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

  const isEditable = detail.template?.status === 'DRAFT';

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

  const requireTemplateId = (): string => {
    if (!templateId) {
      throw new Error('Guarde la plantilla antes de editar la jerarquía normativa.');
    }
    return templateId;
  };

  return {
    ...detail,
    actionError,
    isEditable,
    isBusy,
    addLevel1: wrapMutation((data: CreateLevel1NodeRequestDto) =>
      addLevel1Mutation.mutateAsync({ templateId: requireTemplateId(), data }),
    ),
    updateLevel1: wrapMutation(
      (args: { level1Id: string; data: UpdateLevel1NodeRequestDto }) =>
        updateLevel1Mutation.mutateAsync({
          templateId: requireTemplateId(),
          level1Id: args.level1Id,
          data: args.data,
        }),
    ),
    deleteLevel1: wrapMutation((level1Id: string) =>
      deleteLevel1Mutation.mutateAsync({ templateId: requireTemplateId(), level1Id }),
    ),
    addLevel2: wrapMutation(
      (args: { level1Id: string; data: CreateNormativeNodeRequestDto }) =>
        addLevel2Mutation.mutateAsync({
          templateId: requireTemplateId(),
          level1Id: args.level1Id,
          data: args.data,
        }),
    ),
    updateLevel2: wrapMutation(
      (args: { level2Id: string; data: UpdateNormativeNodeRequestDto }) =>
        updateLevel2Mutation.mutateAsync({
          templateId: requireTemplateId(),
          level2Id: args.level2Id,
          data: args.data,
        }),
    ),
    deleteLevel2: wrapMutation((level2Id: string) =>
      deleteLevel2Mutation.mutateAsync({ templateId: requireTemplateId(), level2Id }),
    ),
    addLevel3: wrapMutation(
      (args: { level2Id: string; data: CreateNormativeNodeRequestDto }) =>
        addLevel3Mutation.mutateAsync({
          templateId: requireTemplateId(),
          level2Id: args.level2Id,
          data: args.data,
        }),
    ),
    updateLevel3: wrapMutation(
      (args: { level3Id: string; data: UpdateNormativeNodeRequestDto }) =>
        updateLevel3Mutation.mutateAsync({
          templateId: requireTemplateId(),
          level3Id: args.level3Id,
          data: args.data,
        }),
    ),
    deleteLevel3: wrapMutation((level3Id: string) =>
      deleteLevel3Mutation.mutateAsync({ templateId: requireTemplateId(), level3Id }),
    ),
    addIndicator: wrapMutation(
      (args: { level3Id: string; data: CreateNormativeIndicatorRequestDto }) =>
        addIndicatorMutation.mutateAsync({
          templateId: requireTemplateId(),
          level3Id: args.level3Id,
          data: args.data,
        }),
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
