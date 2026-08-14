import { customFetch } from '../../../lib/api/customFetch';

export type UploadableIndicatorDto = {
  indicatorId: string;
  code: string;
  title: string;
  criterionId: string;
  criterionCode: string;
  criterionTitle: string;
  currentState: string;
};

type UploadableIndicatorsHttpResponse = {
  data: UploadableIndicatorDto[];
  status: number;
  headers: Headers;
};

/** GET /api/v1/indicators/uploadable — indicadores PENDIENTE/OBSERVADO del [CC]. */
export async function fetchUploadableIndicators(): Promise<UploadableIndicatorDto[]> {
  const response = await customFetch<UploadableIndicatorsHttpResponse>(
    '/api/v1/indicators/uploadable',
    { method: 'GET' },
  );
  return Array.isArray(response.data) ? response.data : [];
}
