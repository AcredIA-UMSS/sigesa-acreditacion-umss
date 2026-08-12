import { useGetProcess } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type { ProcessResponseDto } from '../../../api/model';
import { isApiError } from '../../../lib/api/apiError';

interface UseProcessDetailReturn {
  process: ProcessResponseDto | null;
  isLoading: boolean;
  isError: boolean;
  isNotFound: boolean;
  errorMessage: string | null;
  refetch: () => void;
}

export function useProcessDetail(processId: string | undefined): UseProcessDetailReturn {
  const { data, isLoading, isError, error, refetch } = useGetProcess(processId ?? '', {
    query: {
      enabled: Boolean(processId),
      retry: (failureCount: number, err: unknown) => {
        if (isApiError(err) && (err.status === 404 || err.status === 401)) {
          return false;
        }
        return failureCount < 2;
      },
    },
  });

  const process = data?.status === 200 ? data.data : null;
  const isNotFound = isApiError(error) && error.status === 404;

  let errorMessage: string | null = null;
  if (isError) {
    if (isNotFound) {
      errorMessage = 'Proceso no encontrado o no tiene permiso para verlo.';
    } else {
      errorMessage = isApiError(error)
        ? error.message
        : 'No se pudo cargar el detalle del proceso.';
    }
  }

  return {
    process,
    isLoading,
    isError,
    isNotFound,
    errorMessage,
    refetch: () => {
      void refetch();
    },
  };
}
