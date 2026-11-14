import { useGetTemplate } from '../../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import type { TemplateDetailResponseDto } from '../../../../api/model';
import { isApiError } from '../../../../lib/api/apiError';

interface UseTemplateDetailReturn {
  template: TemplateDetailResponseDto | null;
  isLoading: boolean;
  isError: boolean;
  isNotFound: boolean;
  errorMessage: string | null;
  refetch: () => void;
}

export function useTemplateDetail(templateId: string | undefined): UseTemplateDetailReturn {
  const { data, isLoading, isError, error, refetch } = useGetTemplate(templateId ?? '', {
    query: {
      enabled: Boolean(templateId),
      retry: (failureCount: number, err: unknown) => {
        if (isApiError(err) && (err.status === 404 || err.status === 401)) {
          return false;
        }
        return failureCount < 2;
      },
    },
  });

  const template = data?.status === 200 ? data.data : null;
  const isNotFound = isApiError(error) && error.status === 404;

  let errorMessage: string | null = null;
  if (isError) {
    if (isApiError(error) && error.status === 401) {
      errorMessage = 'Sesión expirada o no autenticada. Inicie sesión nuevamente.';
    } else if (isNotFound) {
      errorMessage = 'Plantilla no encontrada o no tiene permiso para verla.';
    } else {
      errorMessage = isApiError(error)
        ? error.message
        : 'No se pudo cargar el detalle de la plantilla.';
    }
  }

  return {
    template,
    isLoading,
    isError,
    isNotFound,
    errorMessage,
    refetch: () => {
      void refetch();
    },
  };
}
