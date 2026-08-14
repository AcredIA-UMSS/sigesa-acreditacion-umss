import { useEffect, useState } from 'react';
import { useAuth } from '../../../lib/auth/useAuth';
import {
  fetchUploadableIndicators,
  type UploadableIndicatorDto,
} from '../api/fetchUploadableIndicators';

export type UploadableIndicatorsState = {
  indicators: UploadableIndicatorDto[];
  isLoading: boolean;
  errorMessage: string | null;
  isEmpty: boolean;
  reload: () => void;
};

export function useUploadableIndicators(): UploadableIndicatorsState {
  const { isAuthenticated, session } = useAuth();
  const [indicators, setIndicators] = useState<UploadableIndicatorDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [reloadToken, setReloadToken] = useState(0);

  useEffect(() => {
    if (!isAuthenticated || !session?.accessToken) {
      setIndicators([]);
      setIsLoading(false);
      setErrorMessage(null);
      return;
    }

    let cancelled = false;
    setIsLoading(true);
    setErrorMessage(null);

    fetchUploadableIndicators()
      .then((items) => {
        if (!cancelled) {
          setIndicators(items);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setIndicators([]);
          setErrorMessage(
            err instanceof Error
              ? err.message
              : 'No se pudieron cargar los indicadores.',
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [reloadToken, isAuthenticated, session?.accessToken]);

  return {
    indicators,
    isLoading,
    errorMessage,
    isEmpty: !isLoading && !errorMessage && indicators.length === 0,
    reload: () => setReloadToken((n) => n + 1),
  };
}
