import { useQuery } from '@tanstack/react-query';
import type { UseQueryOptions } from '@tanstack/react-query';
import type { TemplateSummaryResponse } from '../../model';
import { customFetch } from '../../../lib/api/customFetch';

type ListTemplatesResponse = { data: TemplateSummaryResponse[]; status: number; headers: Headers };

export const listTemplates = async (): Promise<ListTemplatesResponse> =>
  customFetch<ListTemplatesResponse>('/api/v1/templates', { method: 'GET' });

export const useListTemplates = (
  options?: { query?: Partial<UseQueryOptions<ListTemplatesResponse>> & { enabled?: boolean } },
) =>
  useQuery({
    queryKey: ['listTemplates'],
    queryFn: () => listTemplates(),
    enabled: options?.query?.enabled ?? true,
    staleTime: 0,
    refetchOnMount: 'always',
    retry: 1,
    ...options?.query,
  });

/** Normaliza respuesta API (Jackson puede variar en boolean). */
export function isTemplateValidated(template: TemplateSummaryResponse): boolean {
  const candidate = template as TemplateSummaryResponse & { isValidated?: boolean };
  return candidate.validated === true || candidate.isValidated === true;
}

export function mapTemplateToOption(template: TemplateSummaryResponse) {
  const type = template.type ?? 'CEUB';
  const taxonomyVersion = template.taxonomyVersion ?? 'sin versión';
  return {
    id: template.id as string,
    label: `${type} — ${taxonomyVersion}`,
    type,
    taxonomyVersion,
    activePeriod: template.activePeriod,
  };
}
