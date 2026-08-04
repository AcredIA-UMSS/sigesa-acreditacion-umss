import { useListProcesses } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type { ProcessSummaryResponseDto } from '../../../api/model';
import { isApiError } from '../../../lib/api/apiError';

interface UseProcessListReturn {
  processes: ProcessSummaryResponseDto[];
  isLoading: boolean;
  isError: boolean;
  errorMessage: string | null;
  refetch: () => void;
}

export function useProcessList(): UseProcessListReturn {
  const { data, isLoading, isError, error, refetch } = useListProcesses({
    query: {
      staleTime: 30_000,
    },
  });

  const processes =
    data?.status === 200 && Array.isArray(data.data) ? data.data : [];

  let errorMessage: string | null = null;
  if (isError) {
    errorMessage = isApiError(error)
      ? error.message
      : 'No se pudo cargar el listado de procesos.';
  }

  return {
    processes,
    isLoading,
    isError,
    errorMessage,
    refetch: () => {
      void refetch();
    },
  };
}
