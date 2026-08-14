import { useEffect, useState } from 'react';
import { useList1 } from '../../../api/endpoints/programas/programas';
import type { ProgramSummaryResponse } from '../../../api/model';

const DEBOUNCE_MS = 300;

export function useProgramSearch(searchTerm: string, enabled = true) {
  const [debouncedQuery, setDebouncedQuery] = useState(searchTerm.trim());

  useEffect(() => {
    const timer = window.setTimeout(() => setDebouncedQuery(searchTerm.trim()), DEBOUNCE_MS);
    return () => window.clearTimeout(timer);
  }, [searchTerm]);

  const query = useList1(
    { q: debouncedQuery || undefined },
    {
      query: {
        enabled,
        staleTime: 60_000,
        select: (response) => response.data ?? [],
      },
    },
  );

  return {
    programs: (query.data ?? []) as ProgramSummaryResponse[],
    isLoading: query.isLoading || query.isFetching,
    isError: query.isError,
  };
}
