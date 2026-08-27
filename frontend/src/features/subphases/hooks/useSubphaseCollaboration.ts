import { useCallback, useEffect, useState } from 'react';
import {
  addSubphaseObservation,
  fetchSubphaseEvidences,
  fetchSubphaseObservations,
  fetchSubphaseSubsanationEligibility,
  type SubphaseEvidenceItem,
  type SubphaseObservationItem,
  type SubphaseSubsanationEligibility,
} from '../api/subphaseApi';

export function useSubphaseCollaboration(subphaseId: string | undefined, canSubsanateRole = false) {
  const [evidences, setEvidences] = useState<SubphaseEvidenceItem[]>([]);
  const [observations, setObservations] = useState<SubphaseObservationItem[]>([]);
  const [eligibility, setEligibility] = useState<SubphaseSubsanationEligibility | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const reload = useCallback(async () => {
    if (!subphaseId) {
      setEvidences([]);
      setObservations([]);
      setEligibility(null);
      return;
    }
    setIsLoading(true);
    setError(null);
    try {
      const [evidenceItems, observationItems, eligibilityResult] = await Promise.all([
        fetchSubphaseEvidences(subphaseId),
        fetchSubphaseObservations(subphaseId),
        canSubsanateRole
          ? fetchSubphaseSubsanationEligibility(subphaseId).catch(() => ({
              canSubsanate: false,
              reason: 'No se pudo verificar elegibilidad.',
            }))
          : Promise.resolve(null),
      ]);
      setEvidences(evidenceItems);
      setObservations(observationItems);
      setEligibility(eligibilityResult);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar datos de subfase');
    } finally {
      setIsLoading(false);
    }
  }, [subphaseId, canSubsanateRole]);

  useEffect(() => {
    void reload();
  }, [reload]);

  const postObservation = useCallback(
    async (body: string) => {
      if (!subphaseId) return false;
      await addSubphaseObservation(subphaseId, body);
      await reload();
      return true;
    },
    [subphaseId, reload],
  );

  return {
    evidences,
    observations,
    eligibility,
    hasOpenObservation: observations.some((item) => item.status === 'OPEN'),
    isLoading,
    error,
    reload,
    postObservation,
  };
}
