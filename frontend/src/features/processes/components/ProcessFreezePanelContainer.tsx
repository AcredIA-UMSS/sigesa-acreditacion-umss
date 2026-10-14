import { useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../../lib/auth/useAuth';
import { getGetProcessQueryKey } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import {
  getGetProcessFreezeQueryKey,
  useFreezeProcess,
  useGetProcessFreeze,
  useLiftProcessFreeze,
} from '../../../api/endpoints/stage-workflow/stage-workflow';
import { isApiError } from '../../../lib/api/apiError';
import { ProcessFreezePanel } from './ProcessFreezePanel';

interface ProcessFreezePanelContainerProps {
  processId: string;
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

export function ProcessFreezePanelContainer({ processId }: ProcessFreezePanelContainerProps) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const canManage = session?.role === 'TD';

  const invalidate = () => {
    void queryClient.invalidateQueries({ queryKey: getGetProcessFreezeQueryKey(processId) });
    void queryClient.invalidateQueries({ queryKey: getGetProcessQueryKey(processId) });
  };

  const freezeQuery = useGetProcessFreeze(processId, {
    query: {
      enabled: Boolean(processId),
      retry: (failureCount: number, error: unknown) => {
        if (isApiError(error) && error.status === 404) {
          return false;
        }
        return failureCount < 2;
      },
    },
  });

  const freezeMutation = useFreezeProcess({
    mutation: { onSuccess: invalidate },
  });
  const liftMutation = useLiftProcessFreeze({
    mutation: { onSuccess: invalidate },
  });

  const notFound = isApiError(freezeQuery.error) && freezeQuery.error.status === 404;
  const freeze = freezeQuery.data?.status === 200 ? freezeQuery.data.data : undefined;
  const mutationError = freezeMutation.error ?? liftMutation.error;

  return (
    <ProcessFreezePanel
      freeze={notFound ? { active: false } : freeze}
      canFreeze={canManage}
      canLift={canManage}
      isLoading={freezeQuery.isLoading}
      isMutating={freezeMutation.isPending || liftMutation.isPending}
      errorMessage={
        mutationError
          ? extractErrorMessage(mutationError)
          : freezeQuery.isError && !notFound
            ? extractErrorMessage(freezeQuery.error)
            : undefined
      }
      onFreeze={() => freezeMutation.mutate({ processId })}
      onLift={(targetMode) =>
        liftMutation.mutate({ processId, data: { targetMode } })}
    />
  );
}
