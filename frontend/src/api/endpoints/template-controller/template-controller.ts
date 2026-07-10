import { useQuery } from '@tanstack/react-query';
import type { UseQueryOptions } from '@tanstack/react-query';
import type { TemplateSummaryResponse } from '../../model';
import { customFetch } from '../../../lib/api/customFetch';

type ListTemplatesResponse = { data: TemplateSummaryResponse[]; status: number; headers: Headers };

export const listTemplates = async (): Promise<ListTemplatesResponse> =>
  customFetch<ListTemplatesResponse>('/api/v1/templates', { method: 'GET' });

export const useListTemplates = (
  options?: { query?: Partial<UseQueryOptions<ListTemplatesResponse>> },
) =>
  useQuery({
    queryKey: ['listTemplates'],
    queryFn: () => listTemplates(),
    ...options?.query,
  });
