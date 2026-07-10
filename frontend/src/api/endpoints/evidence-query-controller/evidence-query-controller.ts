import { useQuery } from '@tanstack/react-query';
import type { UseQueryOptions } from '@tanstack/react-query';
import type { EvidenceSearchPageResponse, SearchEvidencesParams } from '../../model';
import { customFetch } from '../../../lib/api/customFetch';

type SearchEvidencesResponse = { data: EvidenceSearchPageResponse; status: number; headers: Headers };

const buildSearchUrl = (params?: SearchEvidencesParams) => {
  const search = new URLSearchParams();
  if (params?.programId) search.set('programId', params.programId);
  if (params?.phaseId !== undefined) search.set('phaseId', String(params.phaseId));
  if (params?.indicatorId) search.set('indicatorId', params.indicatorId);
  if (params?.q) search.set('q', params.q);
  if (params?.page !== undefined) search.set('page', String(params.page));
  if (params?.size !== undefined) search.set('size', String(params.size));
  const query = search.toString();
  return query ? `/api/v1/evidences/search?${query}` : '/api/v1/evidences/search';
};

export const searchEvidences = async (params?: SearchEvidencesParams): Promise<SearchEvidencesResponse> =>
  customFetch<SearchEvidencesResponse>(buildSearchUrl(params), { method: 'GET' });

export const useSearchEvidences = (
  params?: SearchEvidencesParams,
  options?: { query?: Partial<UseQueryOptions<SearchEvidencesResponse>> },
) =>
  useQuery({
    queryKey: ['searchEvidences', params],
    queryFn: () => searchEvidences(params),
    ...options?.query,
  });
