import { useCallback, useEffect, useState } from 'react';
import { downloadDocumentAssetContent } from '../api/normativeIndicatorEvidenceApi';

export function useEvidencePreview() {
  const [assetId, setAssetId] = useState<string | null>(null);
  const [title, setTitle] = useState('');
  const [objectUrl, setObjectUrl] = useState<string | null>(null);
  const [contentType, setContentType] = useState<string | null>(null);
  const [filename, setFilename] = useState('evidencia');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const close = useCallback(() => {
    setAssetId(null);
    setTitle('');
    setError(null);
    setIsLoading(false);
    setContentType(null);
    setObjectUrl((current) => {
      if (current) {
        URL.revokeObjectURL(current);
      }
      return null;
    });
  }, []);

  const open = useCallback((documentAssetId: string, displayName: string) => {
    setAssetId(documentAssetId);
    setTitle(displayName);
    setFilename(displayName);
    setError(null);
  }, []);

  useEffect(() => {
    if (!assetId) {
      return undefined;
    }
    let cancelled = false;
    let createdUrl: string | null = null;
    setIsLoading(true);
    setObjectUrl((current) => {
      if (current) {
        URL.revokeObjectURL(current);
      }
      return null;
    });
    void downloadDocumentAssetContent(assetId)
      .then(({ blob, filename: savedName }) => {
        if (cancelled) {
          return;
        }
        createdUrl = URL.createObjectURL(blob);
        setObjectUrl(createdUrl);
        setContentType(blob.type || null);
        if (savedName) {
          setFilename(savedName);
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'No se pudo abrir la evidencia.');
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });
    return () => {
      cancelled = true;
      if (createdUrl) {
        URL.revokeObjectURL(createdUrl);
      }
    };
  }, [assetId]);

  const download = useCallback(() => {
    if (!objectUrl) {
      return;
    }
    const anchor = document.createElement('a');
    anchor.href = objectUrl;
    anchor.download = filename;
    anchor.click();
  }, [filename, objectUrl]);

  return {
    isOpen: assetId !== null,
    title,
    objectUrl,
    contentType,
    isLoading,
    error,
    open,
    close,
    download,
  };
}
