import { useCallback, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../../lib/auth/useAuth';
import {
  getListImprovementPlanQueryKey,
  useCreateImprovementPlanItem,
  useImportImprovementPlanFromFoda,
  useListImprovementPlan,
  useUpdateImprovementPlanItem,
} from '../../../api/endpoints/evaluation/evaluation';
import { isApiError } from '../../../lib/api/apiError';
import {
  ImprovementPlanPanel,
  type ImprovementPlanDraft,
  type ImprovementPlanItemView,
} from './ImprovementPlanPanel';

interface ImprovementPlanPanelContainerProps {
  processId: string;
  operationalMode?: string;
  frozen?: boolean;
}

function extractErrorMessage(error: unknown): string {
  if (isApiError(error)) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'Ocurrió un error inesperado.';
}

export function ImprovementPlanPanelContainer({
  processId,
  operationalMode,
  frozen = false,
}: ImprovementPlanPanelContainerProps) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const [editingItemId, setEditingItemId] = useState<string | undefined>();
  const [successMessage, setSuccessMessage] = useState<string | undefined>();

  const invalidatePlan = useCallback(() => {
    void queryClient.invalidateQueries({ queryKey: getListImprovementPlanQueryKey(processId) });
  }, [processId, queryClient]);

  const planQuery = useListImprovementPlan(processId, {
    query: { enabled: Boolean(processId) },
  });

  const createMutation = useCreateImprovementPlanItem({
    mutation: {
      onSuccess: () => {
        setSuccessMessage('Ítem añadido al plan.');
        invalidatePlan();
      },
    },
  });

  const updateMutation = useUpdateImprovementPlanItem({
    mutation: {
      onSuccess: () => {
        setEditingItemId(undefined);
        setSuccessMessage('Ítem actualizado.');
        invalidatePlan();
      },
      onError: () => {
        setEditingItemId(undefined);
      },
    },
  });

  const importMutation = useImportImprovementPlanFromFoda({
    mutation: {
      onSuccess: (response) => {
        const imported = response.data.importedCount ?? 0;
        const skipped = response.data.skippedCount ?? 0;
        setSuccessMessage(`Importación FODA: ${imported} nuevos, ${skipped} omitidos.`);
        invalidatePlan();
      },
    },
  });

  const items: ImprovementPlanItemView[] =
    planQuery.data?.status === 200
      ? planQuery.data.data
          .filter((item) => Boolean(item.id))
          .map((item) => ({
            id: item.id,
            processId: item.processId,
            sourceIndicatorId: item.sourceIndicatorId,
            objective: item.objective ?? '',
            activity: item.activity,
            responsibleId: item.responsibleId,
            deadline: item.deadline,
            progress: item.progress ?? 0,
            justification: item.justification,
            progressEvidenceAssetId: item.progressEvidenceAssetId,
          }))
      : [];

  const mutationError = createMutation.error ?? updateMutation.error ?? importMutation.error;

  return (
    <ImprovementPlanPanel
      items={items}
      canEdit={
        session?.role === 'CC'
        && !frozen
        && operationalMode !== 'IMPROVEMENT_MONITORING'
      }
      canEditProgressOnly={
        session?.role === 'CC' && operationalMode === 'IMPROVEMENT_MONITORING'
      }
      isLoading={planQuery.isLoading}
      isError={planQuery.isError || Boolean(mutationError)}
      errorMessage={
        mutationError
          ? extractErrorMessage(mutationError)
          : planQuery.isError
            ? extractErrorMessage(planQuery.error)
            : undefined
      }
      successMessage={successMessage}
      isSaving={createMutation.isPending || updateMutation.isPending}
      isImporting={importMutation.isPending}
      editingItemId={editingItemId}
      onRefresh={() => void planQuery.refetch()}
      onImport={() => importMutation.mutate({ processId })}
      onCreate={(draft: ImprovementPlanDraft) => {
        setSuccessMessage(undefined);
        createMutation.mutate({
          processId,
          data: {
            objective: draft.objective,
            activity: draft.activity || undefined,
            deadline: draft.deadline || undefined,
            progress: draft.progress ?? 0,
          },
        });
      }}
      onUpdate={(itemId, draft) => {
        setEditingItemId(itemId);
        setSuccessMessage(undefined);
        updateMutation.mutate({
          processId,
          itemId,
          data: {
            objective: draft.objective,
            activity: draft.activity || undefined,
            deadline: draft.deadline || undefined,
            progress: draft.progress ?? 0,
            justification: draft.justification || undefined,
          },
        });
      }}
    />
  );
}
