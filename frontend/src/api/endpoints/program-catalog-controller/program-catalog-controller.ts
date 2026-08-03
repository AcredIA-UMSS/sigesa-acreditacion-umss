/**
 * Catálogo de programas — extendido con búsqueda `q` (FSD-UC-003).
 * Regenerar con `pnpm run generate:api` cuando el OpenAPI del backend esté actualizado.
 */
import { useQuery } from '@tanstack/react-query';
import type {
  DataTag,
  DefinedInitialDataOptions,
  DefinedUseQueryResult,
  QueryClient,
  QueryFunction,
  QueryKey,
  UndefinedInitialDataOptions,
  UseQueryOptions,
  UseQueryResult,
} from '@tanstack/react-query';

import type { ProgramSummaryResponse } from '../../model';

import { customFetch } from '../../../lib/api/customFetch';

const withQueryKey = <T extends object, K>(query: T, queryKey: K): T & { queryKey: K } => {
  const result = { queryKey } as T & { queryKey: K };
  for (const key of Object.keys(query)) {
    if (key === 'queryKey') continue;
    Object.defineProperty(result, key, {
      enumerable: true,
      configurable: true,
      get: () => (query as Record<string, unknown>)[key],
    });
  }
  return result;
};

export type List1Params = {
  q?: string;
};

export type list1Response200 = {
  data: ProgramSummaryResponse[];
  status: 200;
};

export type list1ResponseSuccess = list1Response200 & {
  headers: Headers;
};

export type list1Response = list1ResponseSuccess;

export const getList1Url = (params?: List1Params) => {
  const searchParams = new URLSearchParams();
  if (params?.q) {
    searchParams.set('q', params.q);
  }
  const queryString = searchParams.toString();
  return queryString ? `/api/v1/programs?${queryString}` : `/api/v1/programs`;
};

export const list1 = async (params?: List1Params, options?: RequestInit): Promise<list1Response> => {
  return customFetch<list1Response>(getList1Url(params), {
    ...options,
    method: 'GET',
  });
};

export const getList1QueryKey = (params?: List1Params) => {
  return [`/api/v1/programs`, params?.q ?? ''] as const;
};

export const getList1QueryOptions = <TData = Awaited<ReturnType<typeof list1>>, TError = unknown>(
  params?: List1Params,
  options?: { query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof list1>>, TError, TData>> },
) => {
  const { query: queryOptions } = options ?? {};
  const queryKey = queryOptions?.queryKey ?? getList1QueryKey(params);

  const queryFn: QueryFunction<Awaited<ReturnType<typeof list1>>> = ({ signal }) =>
    list1(params, { signal });

  return { queryKey, queryFn, ...queryOptions } as UseQueryOptions<
    Awaited<ReturnType<typeof list1>>,
    TError,
    TData
  > & { queryKey: DataTag<QueryKey, TData, TError> };
};

export type List1QueryResult = NonNullable<Awaited<ReturnType<typeof list1>>>;
export type List1QueryError = unknown;

export function useList1<TData = Awaited<ReturnType<typeof list1>>, TError = unknown>(
  params?: List1Params,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof list1>>, TError, TData>> &
      Pick<
        DefinedInitialDataOptions<
          Awaited<ReturnType<typeof list1>>,
          TError,
          Awaited<ReturnType<typeof list1>>
        >,
        'initialData'
      >;
  },
  queryClient?: QueryClient,
): DefinedUseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
export function useList1<TData = Awaited<ReturnType<typeof list1>>, TError = unknown>(
  params?: List1Params,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof list1>>, TError, TData>> &
      Pick<
        UndefinedInitialDataOptions<
          Awaited<ReturnType<typeof list1>>,
          TError,
          Awaited<ReturnType<typeof list1>>
        >,
        'initialData'
      >;
  },
  queryClient?: QueryClient,
): UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };
export function useList1<TData = Awaited<ReturnType<typeof list1>>, TError = unknown>(
  params?: List1Params,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof list1>>, TError, TData>>;
  },
  queryClient?: QueryClient,
): UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> };

export function useList1<TData = Awaited<ReturnType<typeof list1>>, TError = unknown>(
  params?: List1Params,
  options?: {
    query?: Partial<UseQueryOptions<Awaited<ReturnType<typeof list1>>, TError, TData>>;
  },
  queryClient?: QueryClient,
): UseQueryResult<TData, TError> & { queryKey: DataTag<QueryKey, TData, TError> } {
  const queryOptions = getList1QueryOptions(params, options);
  const query = useQuery(queryOptions, queryClient) as UseQueryResult<TData, TError> & {
    queryKey: DataTag<QueryKey, TData, TError>;
  };
  return withQueryKey(query, queryOptions.queryKey);
}
