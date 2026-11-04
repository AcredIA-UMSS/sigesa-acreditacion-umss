import { useCallback, useMemo, useState } from 'react';
import {
  downloadPublicCertificate,
  useGetPublicProgram,
  useListPublicPrograms,
} from '../../../api/endpoints/public-portal/public-portal';
import type { PublicProgramStatusResponse, PublicProgramSummaryResponse } from '../../../api/model';
import { isApiError } from '../../../lib/api/apiError';

const ANON = { auth: false as const };

function triggerBlobDownload(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  window.URL.revokeObjectURL(url);
}

function errorMessage(error: unknown): string | null {
  if (error == null) {
    return null;
  }
  if (isApiError(error)) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return 'No se pudo completar la consulta.';
}

export function usePublicPortal() {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedSlug, setSelectedSlug] = useState<string | null>(null);
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);

  const listQuery = useListPublicPrograms({ request: ANON });
  const statusQuery = useGetPublicProgram(selectedSlug ?? '', {
    query: { enabled: selectedSlug != null && selectedSlug.length > 0 },
    request: ANON,
  });

  const programs = listQuery.data?.data;

  const filteredPrograms = useMemo(() => {
    const source: PublicProgramSummaryResponse[] = programs ?? [];
    const needle = searchQuery.trim().toLowerCase();
    if (needle.length === 0) {
      return source;
    }
    return source.filter((program) => (program.name ?? '').toLowerCase().includes(needle));
  }, [programs, searchQuery]);

  const status: PublicProgramStatusResponse | null =
    statusQuery.data?.status === 200 ? (statusQuery.data.data ?? null) : null;

  const selectProgram = useCallback((slug: string) => {
    setSelectedSlug(slug);
    setDownloadError(null);
  }, []);

  const downloadCertificate = useCallback(async () => {
    if (selectedSlug == null) {
      return;
    }
    setIsDownloading(true);
    setDownloadError(null);
    try {
      const response = await downloadPublicCertificate(selectedSlug, ANON);
      if (response.status !== 200 || !(response.data instanceof Blob)) {
        throw new Error('No se pudo descargar el certificado.');
      }
      triggerBlobDownload(response.data, `certificado-${selectedSlug}.pdf`);
    } catch (error) {
      setDownloadError(errorMessage(error) ?? 'No se pudo descargar el certificado.');
    } finally {
      setIsDownloading(false);
    }
  }, [selectedSlug]);

  return {
    searchQuery,
    setSearchQuery,
    filteredPrograms,
    selectedSlug,
    selectProgram,
    status,
    isListLoading: listQuery.isLoading,
    isStatusLoading: statusQuery.isFetching,
    listError: errorMessage(listQuery.error),
    statusError: errorMessage(statusQuery.error),
    downloadCertificate,
    isDownloading,
    downloadError,
  };
}
