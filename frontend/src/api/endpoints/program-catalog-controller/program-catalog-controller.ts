import { useQuery } from '@tanstack/react-query';
import type { QueryClient, QueryFunction, UseQueryOptions, UseQueryResult } from '@tanstack/react-query';
import type { ProgramSummaryResponse } from '../../model';
import { customFetch } from '../../../lib/api/customFetch';

export type listProgramsResponse200 = {
  data: ProgramSummaryResponse[];
  status: 200;
};

export const getListProgramsUrl = () => `/api/v1/programs`;

export const listPrograms = async (options?: RequestInit): Promise<listProgramsResponse200> => {
  const res = await customFetch<ProgramSummaryResponse[]>(getListProgramsUrl(), {
    ...options,
    method: 'GET',
  });

  return { data: res.data, status: res.status as 200 };
};

export const getListProgramsQueryKey = () => ['listPrograms'] as const;

export const getListProgramsQueryOptions = <TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof listPrograms>>, TError>>; fetch?: RequestInit },
) => {
  const queryKey = getListProgramsQueryKey();
  const queryFn: QueryFunction<Awaited<ReturnType<typeof listPrograms>>> = () =>
    listPrograms(options?.fetch);

  return { queryKey, queryFn, ...options?.query } as UseQueryOptions<
    Awaited<ReturnType<typeof listPrograms>>,
    TError
  >;
};

export const useListPrograms = <TError = unknown>(
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof listPrograms>>, TError>>; fetch?: RequestInit },
  queryClient?: QueryClient,
): UseQueryResult<Awaited<ReturnType<typeof listPrograms>>, TError> => {
  return useQuery(getListProgramsQueryOptions(options), queryClient);
};
