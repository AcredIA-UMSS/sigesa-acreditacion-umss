import { useQueryClient } from '@tanstack/react-query';
import {
  getGetTemplateQueryKey,
  getListTemplatesQueryKey,
  useArchiveTemplate,
  useDeleteTemplate,
  useDuplicateTemplate,
  usePublishTemplate,
} from '../../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';

export function useTemplateActions(templateId?: string) {
  const queryClient = useQueryClient();

  const invalidateTemplates = async (targetTemplateId?: string) => {
    await queryClient.invalidateQueries({ queryKey: getListTemplatesQueryKey() });
    if (targetTemplateId) {
      await queryClient.invalidateQueries({ queryKey: getGetTemplateQueryKey(targetTemplateId) });
    }
  };

  const publishMutation = usePublishTemplate({
    mutation: {
      onSuccess: async (_response, variables) => {
        await invalidateTemplates(variables.templateId);
      },
    },
  });

  const archiveMutation = useArchiveTemplate({
    mutation: {
      onSuccess: async (_response, variables) => {
        await invalidateTemplates(variables.templateId);
      },
    },
  });

  const duplicateMutation = useDuplicateTemplate({
    mutation: {
      onSuccess: async () => {
        await invalidateTemplates();
      },
    },
  });

  const deleteMutation = useDeleteTemplate({
    mutation: {
      onSuccess: async () => {
        await invalidateTemplates();
      },
    },
  });

  const runAction = async (
    action: 'publish' | 'archive' | 'duplicate' | 'delete',
    targetId: string,
  ): Promise<{ ok: true; duplicateId?: string } | { ok: false; message: string }> => {
    try {
      if (action === 'publish') {
        await publishMutation.mutateAsync({ templateId: targetId });
        return { ok: true };
      }

      if (action === 'archive') {
        await archiveMutation.mutateAsync({ templateId: targetId });
        return { ok: true };
      }

      if (action === 'duplicate') {
        const response = await duplicateMutation.mutateAsync({ templateId: targetId });
        return { ok: true, duplicateId: response.data.id };
      }

      await deleteMutation.mutateAsync({ templateId: targetId });
      return { ok: true };
    } catch (error) {
      return { ok: false, message: getApiErrorMessage(error) };
    }
  };

  const isBusy =
    publishMutation.isPending ||
    archiveMutation.isPending ||
    duplicateMutation.isPending ||
    deleteMutation.isPending;

  return {
    runAction,
    isBusy,
    busyTemplateId: templateId,
  };
}
