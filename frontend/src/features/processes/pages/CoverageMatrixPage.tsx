import { useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  getCoverageMatrixPdf,
  useGetCoverageMatrix,
} from '../../../api/endpoints/coverage-matrix/coverage-matrix';
import { Sidebar } from '../../../components/layout/Sidebar';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { CoverageMatrixView } from '../components/CoverageMatrixView';

function triggerBlobDownload(blob: Blob, filename: string): void {
  const url = window.URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();
  window.URL.revokeObjectURL(url);
}

export function CoverageMatrixPage() {
  const { processId } = useParams<{ processId: string }>();
  const [isDownloading, setIsDownloading] = useState(false);
  const [downloadError, setDownloadError] = useState<string | null>(null);
  const query = useGetCoverageMatrix(processId ?? '', {
    query: { enabled: Boolean(processId) },
  });

  if (!processId) {
    return null;
  }

  const errorMessage = downloadError
    ?? (query.isError ? getApiErrorMessage(query.error, 'No se pudo cargar la matriz.') : null);

  const handleDownloadPdf = async () => {
    setIsDownloading(true);
    setDownloadError(null);
    try {
      const response = await getCoverageMatrixPdf(processId);
      const payload = response.data;
      const blob = payload instanceof Blob
        ? payload
        : new Blob([payload ?? new Uint8Array()], { type: 'application/pdf' });
      triggerBlobDownload(blob, `matriz-cobertura-${processId}.pdf`);
    } catch (err) {
      setDownloadError(getApiErrorMessage(err, 'No se pudo descargar el PDF.'));
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="processes" />
      <main data-app-scroll className="flex-1 overflow-y-auto px-8 py-8">
        <CoverageMatrixView
          processId={processId}
          matrix={query.data?.data}
          isLoading={query.isLoading}
          errorMessage={errorMessage}
          isDownloading={isDownloading}
          onDownloadPdf={() => {
            void handleDownloadPdf();
          }}
        />
      </main>
    </div>
  );
}
