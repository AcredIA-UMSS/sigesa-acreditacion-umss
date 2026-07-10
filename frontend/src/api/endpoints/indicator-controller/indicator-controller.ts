import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import type { UseMutationOptions, UseQueryOptions } from '@tanstack/react-query';
import type {
  ApproveIndicatorResponse,
  IndicatorSummaryResponse,
  ListIndicatorsParams,
  RejectIndicatorRequest,
  RejectIndicatorResponse,
} from '../../model';
import { customFetch } from '../../../lib/api/customFetch';

type ListIndicatorsResponse = { data: IndicatorSummaryResponse[]; status: number; headers: Headers };
type ApproveResponse = { data: ApproveIndicatorResponse; status: number; headers: Headers };
type RejectResponse = { data: RejectIndicatorResponse; status: number; headers: Headers };

const buildIndicatorsUrl = (params?: ListIndicatorsParams) => {
  const search = new URLSearchParams();
  if (params?.programId) search.set('programId', params.programId);
  if (params?.phaseId !== undefined) search.set('phaseId', String(params.phaseId));
  const query = search.toString();
  return query ? `/api/v1/indicators?${query}` : '/api/v1/indicators';
};

export const listIndicators = async (params?: ListIndicatorsParams): Promise<ListIndicatorsResponse> =>
  customFetch<ListIndicatorsResponse>(buildIndicatorsUrl(params), { method: 'GET' });

export const useListIndicators = (
  params?: ListIndicatorsParams,
  options?: { query?: Partial<UseQueryOptions<ListIndicatorsResponse>> },
) =>
  useQuery({
    queryKey: ['listIndicators', params],
    queryFn: () => listIndicators(params),
    ...options?.query,
  });

export const approveIndicator = async (indicatorId: string): Promise<ApproveResponse> =>
  customFetch<ApproveResponse>(`/api/v1/indicators/${indicatorId}/approve`, { method: 'POST' });

export const rejectIndicator = async (
  indicatorId: string,
  data: RejectIndicatorRequest,
): Promise<RejectResponse> =>
  customFetch<RejectResponse>(`/api/v1/indicators/${indicatorId}/reject`, {
    method: 'POST',
    body: JSON.stringify(data),
  });

export const useApproveIndicator = (
  options?: { mutation?: UseMutationOptions<ApproveResponse, unknown, { indicatorId: string }> },
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ indicatorId }) => approveIndicator(indicatorId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['listIndicators'] });
    },
    ...options?.mutation,
  });
};

export const useRejectIndicator = (
  options?: {
    mutation?: UseMutationOptions<
      RejectResponse,
      unknown,
      { indicatorId: string; data: RejectIndicatorRequest }
    >;
  },
) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ indicatorId, data }) => rejectIndicator(indicatorId, data),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['listIndicators'] });
    },
    ...options?.mutation,
  });
};
