import { useQueryClient } from '@tanstack/react-query';
import { useAuth } from '../../../lib/auth/useAuth';
import {
  getGetPeerRoomChecklistQueryKey,
  useGetPeerRoomChecklist,
  useUpdatePeerRoomChecklist,
} from '../../../api/endpoints/stage-workflow/stage-workflow';
import { isApiError } from '../../../lib/api/apiError';
import { PeerRoomChecklistPanel } from './PeerRoomChecklistPanel';

interface PeerRoomChecklistPanelContainerProps {
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

export function PeerRoomChecklistPanelContainer({
  processId,
}: PeerRoomChecklistPanelContainerProps) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const canEdit = session?.role === 'TD' || session?.role === 'EE';

  const query = useGetPeerRoomChecklist(processId, {
    query: { enabled: Boolean(processId) },
  });
  const updateMutation = useUpdatePeerRoomChecklist({
    mutation: {
      onSuccess: () => {
        void queryClient.invalidateQueries({
          queryKey: getGetPeerRoomChecklistQueryKey(processId),
        });
      },
    },
  });

  const items =
    query.data?.status === 200
      ? query.data.data
          .filter((item) => Boolean(item.itemCode))
          .map((item) => ({
            id: item.id,
            itemCode: item.itemCode ?? '',
            label: item.label ?? item.itemCode ?? '',
            checked: item.checked === true,
          }))
      : [];

  return (
    <PeerRoomChecklistPanel
      items={items}
      canEdit={canEdit}
      isLoading={query.isLoading}
      isSaving={updateMutation.isPending}
      errorMessage={
        updateMutation.error
          ? extractErrorMessage(updateMutation.error)
          : query.isError
            ? extractErrorMessage(query.error)
            : undefined
      }
      onToggle={(itemCode, checked) => {
        updateMutation.mutate({
          processId,
          data: { items: [{ itemCode, checked }] },
        });
      }}
    />
  );
}
