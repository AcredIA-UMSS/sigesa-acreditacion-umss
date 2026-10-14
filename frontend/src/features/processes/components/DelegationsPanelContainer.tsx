import { useQueryClient } from '@tanstack/react-query';
import type { NormativeLevel1NodeDto } from '../../../api/model';
import {
  getListDelegationsQueryKey,
  useCreateDelegation,
  useDeleteDelegation,
  useListCaeCandidates,
  useListDelegations,
} from '../../../api/endpoints/evaluation/evaluation';
import { isApiError } from '../../../lib/api/apiError';
import { DelegationsPanel } from './DelegationsPanel';

interface DelegationsPanelContainerProps {
  processId: string;
  level1Nodes: NormativeLevel1NodeDto[];
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

function flattenScopes(level1Nodes: NormativeLevel1NodeDto[]) {
  const level1Options: { value: string; label: string }[] = [];
  const level2Options: { value: string; label: string }[] = [];
  const indicatorOptions: { value: string; label: string }[] = [];

  for (const level1 of level1Nodes) {
    if (level1.id) {
      level1Options.push({
        value: level1.id,
        label: level1.name ?? level1.label ?? level1.id,
      });
    }
    for (const level2 of level1.level2Nodes ?? []) {
      if (level2.id) {
        level2Options.push({
          value: level2.id,
          label: level2.name ?? level2.label ?? level2.id,
        });
      }
      for (const level3 of level2.level3Nodes ?? []) {
        for (const indicator of level3.indicators ?? []) {
          if (indicator.id) {
            indicatorOptions.push({
              value: indicator.id,
              label: `${indicator.code ?? ''} ${indicator.description ?? ''}`.trim() || indicator.id,
            });
          }
        }
      }
    }
  }

  return { level1Options, level2Options, indicatorOptions };
}

export function DelegationsPanelContainer({
  processId,
  level1Nodes,
}: DelegationsPanelContainerProps) {
  const queryClient = useQueryClient();
  const scopes = flattenScopes(level1Nodes);

  const listQuery = useListDelegations(processId, {
    query: { enabled: Boolean(processId) },
  });
  const candidatesQuery = useListCaeCandidates(processId, {
    query: { enabled: Boolean(processId) },
  });
  const createMutation = useCreateDelegation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListDelegationsQueryKey(processId) });
      },
    },
  });
  const deleteMutation = useDeleteDelegation({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({ queryKey: getListDelegationsQueryKey(processId) });
      },
    },
  });

  const candidates =
    candidatesQuery.data?.status === 200
      ? candidatesQuery.data.data.map((user) => ({
          value: user.id ?? '',
          label: user.fullName || user.email || user.id || 'CAE',
        }))
      : [];

  const items =
    listQuery.data?.status === 200
      ? listQuery.data.data
          .filter((item) => Boolean(item.id))
          .map((item) => {
            const candidate = candidates.find((option) => option.value === item.userId);
            let scopeLabel = 'Alcance';
            if (item.indicatorId) {
              const match = scopes.indicatorOptions.find((option) => option.value === item.indicatorId);
              scopeLabel = `Indicador: ${match?.label ?? item.indicatorId}`;
            } else if (item.level2Id) {
              const match = scopes.level2Options.find((option) => option.value === item.level2Id);
              scopeLabel = `N2: ${match?.label ?? item.level2Id}`;
            } else if (item.level1Id) {
              const match = scopes.level1Options.find((option) => option.value === item.level1Id);
              scopeLabel = `N1: ${match?.label ?? item.level1Id}`;
            }
            return {
              id: item.id,
              userId: item.userId,
              userLabel: candidate?.label ?? item.userId,
              scopeLabel,
            };
          })
      : [];

  const mutationError = createMutation.error ?? deleteMutation.error;

  return (
    <DelegationsPanel
      items={items}
      caeOptions={candidates.filter((option) => option.value)}
      level1Options={scopes.level1Options}
      level2Options={scopes.level2Options}
      indicatorOptions={scopes.indicatorOptions}
      canManage
      isLoading={listQuery.isLoading}
      isSaving={createMutation.isPending || deleteMutation.isPending}
      errorMessage={
        mutationError
          ? extractErrorMessage(mutationError)
          : listQuery.isError
            ? extractErrorMessage(listQuery.error)
            : undefined
      }
      onCreate={(draft) => {
        createMutation.mutate({
          processId,
          data: {
            userId: draft.userId,
            level1Id: draft.level1Id,
            level2Id: draft.level2Id,
            indicatorId: draft.indicatorId,
          },
        });
      }}
      onDelete={(delegationId) => deleteMutation.mutate({ processId, delegationId })}
    />
  );
}
