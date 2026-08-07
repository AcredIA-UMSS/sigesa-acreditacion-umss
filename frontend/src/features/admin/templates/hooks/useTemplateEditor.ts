import { useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  getGetTemplateQueryKey,
  getListTemplatesQueryKey,
  useCreateTemplate,
  useGetTemplate,
  useUpdateTemplate,
} from '../../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import {
  createEmptyTemplateForm,
  mapDetailToForm,
  mapFormToUpsertRequest,
} from '../lib/templateFormMapper';
import {
  hasTemplateFormErrors,
  validateTemplateForm,
  type TemplateFormErrors,
} from '../lib/templateFormValidation';
import type { TemplateFormViewModel, TemplateStatusCode } from '../lib/templateTypes';

export function useTemplateEditor(templateId?: string) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const isEditMode = Boolean(templateId);
  const detailQuery = useGetTemplate(templateId ?? '', {
    query: { enabled: isEditMode },
  });

  const [form, setForm] = useState<TemplateFormViewModel>(createEmptyTemplateForm());
  const [fieldErrors, setFieldErrors] = useState<TemplateFormErrors>({});
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [isHydrated, setIsHydrated] = useState(!isEditMode);

  useEffect(() => {
    if (!isEditMode) {
      setForm(createEmptyTemplateForm());
      setIsHydrated(true);
      return;
    }

    if (detailQuery.data?.data) {
      setForm(mapDetailToForm(detailQuery.data.data));
      setIsHydrated(true);
    }
  }, [detailQuery.data, isEditMode]);

  const createMutation = useCreateTemplate({
    mutation: {
      onSuccess: async (response) => {
        await queryClient.invalidateQueries({ queryKey: getListTemplatesQueryKey() });
        const createdId = response.data.id;
        if (createdId) {
          navigate(`/admin/plantillas/${createdId}`, { replace: true });
        }
      },
    },
  });

  const updateMutation = useUpdateTemplate({
    mutation: {
      onSuccess: async (_response, variables) => {
        await queryClient.invalidateQueries({ queryKey: getListTemplatesQueryKey() });
        await queryClient.invalidateQueries({ queryKey: getGetTemplateQueryKey(variables.templateId) });
      },
    },
  });

  const status = (detailQuery.data?.data.status ?? 'DRAFT') as TemplateStatusCode;
  const isSaving = createMutation.isPending || updateMutation.isPending;

  const saveTemplate = async (): Promise<boolean> => {
    setSubmitError(null);
    const validationErrors = validateTemplateForm(form);
    setFieldErrors(validationErrors);

    if (hasTemplateFormErrors(validationErrors)) {
      setSubmitError('Revise los campos marcados antes de guardar.');
      return false;
    }

    const payload = mapFormToUpsertRequest(form);

    try {
      if (isEditMode && templateId) {
        await updateMutation.mutateAsync({ templateId, data: payload });
      } else {
        await createMutation.mutateAsync({ data: payload });
      }
      return true;
    } catch (error) {
      setSubmitError(getApiErrorMessage(error));
      return false;
    }
  };

  return {
    form,
    setForm,
    fieldErrors,
    submitError,
    status,
    isEditMode,
    isLoading: isEditMode && detailQuery.isLoading,
    isError: isEditMode && detailQuery.isError,
    loadError: detailQuery.error,
    isHydrated,
    isSaving,
    saveTemplate,
  };
}
