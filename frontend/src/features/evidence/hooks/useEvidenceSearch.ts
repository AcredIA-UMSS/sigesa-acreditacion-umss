import { useCallback, useEffect, useState } from 'react';
import {
  fetchEvidenceSearch,
  type EvidenceSearchHit,
  type EvidenceSearchParams,
} from '../api/fetchEvidenceSearch';

export type UseEvidenceSearchOptions = {
  processId: string;
  programId?: string;
  enabled?: boolean;
};

export function useEvidenceSearch({ processId, programId, enabled = true }: UseEvidenceSearchOptions) {
  const [query, setQuery] = useState('');
  const [level1Id, setLevel1Id] = useState('');
  const [indicatorId, setIndicatorId] = useState('');
  const [results, setResults] = useState<EvidenceSearchHit[]>([]);
  const [total, setTotal] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const search = useCallback(async () => {
    if (!enabled || !processId) {
      return;
    }
    setIsSearching(true);
    setError(null);
    try {
      const params: EvidenceSearchParams = {
        processId,
        programId,
        q: query,
        level1Id: level1Id || undefined,
        indicatorId: indicatorId || undefined,
        page: 0,
        size: 20,
      };
      const page = await fetchEvidenceSearch(params);
      setResults(page.items);
      setTotal(page.total);
      setHasSearched(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al buscar evidencias');
      setResults([]);
      setTotal(0);
    } finally {
      setIsSearching(false);
    }
  }, [enabled, processId, programId, query, level1Id, indicatorId]);

  useEffect(() => {
    setIndicatorId('');
  }, [level1Id]);

  const reset = useCallback(() => {
    setQuery('');
    setLevel1Id('');
    setIndicatorId('');
    setResults([]);
    setTotal(0);
    setHasSearched(false);
    setError(null);
  }, []);

  return {
    query,
    setQuery,
    level1Id,
    setLevel1Id,
    indicatorId,
    setIndicatorId,
    results,
    total,
    isSearching,
    hasSearched,
    error,
    search,
    reset,
  };
}
