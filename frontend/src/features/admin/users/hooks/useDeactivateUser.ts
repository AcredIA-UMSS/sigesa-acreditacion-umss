import { useDeactivate } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';

/**
 * Hook listo para revocación desde filas de la tabla cuando exista GET /admin/users.
 * No se expone UI de revocación hasta contar con listado backend.
 */
export function useDeactivateUser() {
  const mutation = useDeactivate({
    mutation: {
      onError: () => {
        /* Consumido por la futura tabla */
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
  };
}
