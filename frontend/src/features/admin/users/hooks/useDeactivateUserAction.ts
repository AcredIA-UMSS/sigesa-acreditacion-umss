import { useQueryClient } from '@tanstack/react-query';
import { useDeactivate } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { getListUsersQueryKey } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';

export function useDeactivateUserAction() {
  const queryClient = useQueryClient();

  const mutation = useDeactivate({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getListUsersQueryKey(undefined) });
      },
    },
  });

  const deactivateUser = async (userId: string): Promise<{ ok: true } | { ok: false; message: string }> => {
    try {
      await mutation.mutateAsync({ id: userId });
      return { ok: true };
    } catch (error) {
      return { ok: false, message: getApiErrorMessage(error) };
    }
  };

  return {
    deactivateUser,
    isDeactivating: mutation.isPending,
    deactivatingUserId: mutation.variables?.id ?? null,
  };
}
