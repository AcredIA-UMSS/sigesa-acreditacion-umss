import { useCallback, useEffect, useState } from 'react';
import {
  fetchNormativeIndicatorEvidences,
  fetchNormativeIndicatorSubsanationEligibility,
  type NormativeIndicatorEvidenceItem,
  type NormativeIndicatorSubsanationEligibility,
} from '../api/normativeIndicatorEvidenceApi';

export function useNormativeIndicatorCollaboration(
  indicatorId?: string,
  canSubsanateRole = false,
) {
  const [evidences, setEvidences] = useState<NormativeIndicatorEvidenceItem[]>([]);
  const [eligibility, setEligibility] = useState<NormativeIndicatorSubsanationEligibility | null>(
    null,
  );
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    if (!indicatorId) {
      setEvidences([]);
      setEligibility(null);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [evidenceItems, eligibilityResult] = await Promise.all([
        fetchNormativeIndicatorEvidences(indicatorId),
        canSubsanateRole
          ? fetchNormativeIndicatorSubsanationEligibility(indicatorId).catch(() => ({
              canSubsanate: false,
              reason: 'No se pudo verificar elegibilidad.',
            }))
          : Promise.resolve(null),
      ]);
      setEvidences(evidenceItems);
      setEligibility(eligibilityResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudieron cargar las evidencias.');
    } finally {
      setIsLoading(false);
    }
  }, [indicatorId, canSubsanateRole]);

  useEffect(() => {
    void reload();
  }, [reload]);

  return {
    evidences,
    eligibility,
    hasOpenObservation: Boolean(eligibility?.openObservationId),
    isLoading,
    error,
    reload,
  };
}
