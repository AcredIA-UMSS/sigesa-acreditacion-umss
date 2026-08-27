import { History } from 'lucide-react';
import { useCallback, useState } from 'react';
import { Button } from '../../../components/ui/Button';
import {
  fetchEvidenceVersions,
  type EvidenceVersionHistoryItem,
} from '../api/fetchEvidenceVersions';

export type EvidenceVersionHistoryPanelProps = {
  evidenceId: string;
  filename: string;
};

export function EvidenceVersionHistoryPanel({
  evidenceId,
  filename,
}: EvidenceVersionHistoryPanelProps) {
  const [open, setOpen] = useState(false);
  const [versions, setVersions] = useState<EvidenceVersionHistoryItem[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const items = await fetchEvidenceVersions(evidenceId);
      setVersions(items);
      setOpen(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo cargar el historial');
    } finally {
      setIsLoading(false);
    }
  }, [evidenceId]);

  return (
    <div className="mt-2">
      <Button
        type="button"
        variant="ghost"
        className="h-auto px-0 py-0 text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2"
        disabled={isLoading}
        isLoading={isLoading}
        onClick={() => void load()}
      >
        <History size={14} aria-hidden />
        Ver historial de versiones
      </Button>

      {error && (
        <p className="mt-1 text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {open && (
        <div className="mt-2 rounded-md border border-gray-200 bg-body px-3 py-2">
          <p className="text-label-md font-semibold uppercase text-gray-700">
            Historial — {filename}
          </p>
          {versions.length === 0 ? (
            <p className="mt-1 text-body-md text-gray-500">Sin versiones registradas.</p>
          ) : (
            <ul className="mt-2 space-y-2">
              {versions.map((item) => (
                <li
                  key={item.versionId}
                  className={`rounded-md border px-3 py-2 text-body-md ${
                    item.current
                      ? 'border-success/40 bg-success/5'
                      : 'border-gray-200 bg-gray-50'
                  }`}
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <span className="font-medium text-gray-900">
                      Versión {item.version}
                      {item.current ? ' (vigente)' : ''}
                      {!item.blobAvailable ? ' — solo metadatos' : ''}
                    </span>
                    <span className="text-label-md text-gray-500">
                      {new Date(item.createdAt).toLocaleString('es-BO')}
                    </span>
                  </div>
                  {item.originalFilename && (
                    <p className="mt-1 text-label-md text-gray-600">{item.originalFilename}</p>
                  )}
                  <p className="mt-1 text-body-md text-gray-600">{item.description}</p>
                  {!item.blobAvailable && (
                    <p className="mt-1 text-label-md text-gray-500">
                      Archivo no disponible (historial liviano tras subsanación).
                    </p>
                  )}
                  {item.supersedesVersion != null && (
                    <p className="mt-1 text-label-md text-gray-500">
                      Reemplaza versión {item.supersedesVersion}
                    </p>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
