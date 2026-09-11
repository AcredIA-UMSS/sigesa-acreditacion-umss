import { useCallback, useEffect, useState } from 'react';
import {
  fetchEvidenceSearch,
  type EvidenceSearchHit,
  type EvidenceSearchParams,
} from '../api/fetchEvidenceSearch';

export type UseEvidenceSearchOptions = {
  processId?: string;
  programId?: string;
  enabled?: boolean;
};

export function useEvidenceSearch({ processId, programId, enabled = true }: UseEvidenceSearchOptions) {
  const [query, setQuery] = useState('');
  const [phaseId, setPhaseId] = useState('');
  const [subphaseId, setSubphaseId] = useState('');
  const [aiEnabled, setAiEnabled] = useState(false);
  const [results, setResults] = useState<EvidenceSearchHit[]>([]);
  const [total, setTotal] = useState(0);
  const [isSearching, setIsSearching] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const search = useCallback(async (customParams?: Partial<EvidenceSearchParams>) => {
    if (!enabled) {
      return;
    }
    setIsSearching(true);
    setError(null);
    try {
      const targetQuery = customParams?.q !== undefined ? customParams.q : query;
      const targetAi = customParams?.aiEnabled !== undefined ? customParams.aiEnabled : aiEnabled;

      const params: EvidenceSearchParams = {
        processId: processId || undefined,
        programId: programId || undefined,
        q: targetQuery,
        phaseId: phaseId || undefined,
        subphaseId: subphaseId || undefined,
        aiEnabled: targetAi,
        page: 0,
        size: 20,
      };
      const page = await fetchEvidenceSearch(params);
      setResults(Array.isArray(page?.items) ? page.items : []);
      setTotal(typeof page?.total === 'number' ? page.total : 0);
      setHasSearched(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al buscar evidencias');
      setResults([]);
      setTotal(0);
    } finally {
      setIsSearching(false);
    }
  }, [enabled, processId, programId, query, phaseId, subphaseId, aiEnabled]);

  useEffect(() => {
    if (!phaseId) {
      setSubphaseId('');
    }
  }, [phaseId]);

  const reset = useCallback(() => {
    setQuery('');
    setPhaseId('');
    setSubphaseId('');
    setAiEnabled(false);
    setResults([]);
    setTotal(0);
    setHasSearched(false);
    setError(null);
  }, []);

  return {
    query,
    setQuery,
    phaseId,
    setPhaseId,
    subphaseId,
    setSubphaseId,
    aiEnabled,
    setAiEnabled,
    results,
    total,
    isSearching,
    hasSearched,
    error,
    search,
    reset,
  };
}
