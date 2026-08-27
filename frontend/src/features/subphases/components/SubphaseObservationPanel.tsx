import { MessageSquare, FileText } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { EvidenceVersionHistoryPanel } from '../../evidence/components/EvidenceVersionHistoryPanel';
import type { SubphaseEvidenceItem, SubphaseObservationItem } from '../api/subphaseApi';

export type SubphaseObservationPanelProps = {
  subphaseId?: string;
  canObserve: boolean;
  observations: SubphaseObservationItem[];
  isLoading: boolean;
  onSubmitObservation: (body: string) => Promise<boolean>;
};

export function SubphaseObservationPanel({
  canObserve,
  observations,
  isLoading,
  onSubmitObservation,
}: SubphaseObservationPanelProps) {
  const [draft, setDraft] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async () => {
    if (!draft.trim()) {
      setError('Escriba una observación.');
      return;
    }
    setIsSubmitting(true);
    setError(null);
    try {
      const ok = await onSubmitObservation(draft.trim());
      if (ok) {
        setDraft('');
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo registrar la observación');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mt-4 rounded-lg border border-gray-200 bg-gray-50 p-4">
      <div className="mb-2 flex items-center gap-2 text-label-md font-semibold uppercase text-gray-700">
        <MessageSquare size={16} aria-hidden />
        Observaciones
      </div>

      {isLoading && observations.length === 0 ? (
        <p className="text-body-md text-gray-500">Cargando observaciones…</p>
      ) : observations.length === 0 ? (
        <p className="text-body-md text-gray-500">Sin observaciones registradas.</p>
      ) : (
        <ul className="mb-3 space-y-2">
          {observations.map((obs) => (
            <li
              key={obs.id}
              className="rounded-md border border-gray-200 bg-body px-3 py-2 text-body-md text-gray-800"
            >
              <div className="mb-1 flex flex-wrap items-center gap-2">
                <span
                  className={`rounded-full px-2 py-0.5 text-label-md font-medium ${
                    obs.status === 'OPEN'
                      ? 'bg-warning/20 text-gray-800'
                      : 'bg-success/15 text-gray-700'
                  }`}
                >
                  {obs.status === 'OPEN' ? 'Pendiente' : 'Resuelta'}
                </span>
              </div>
              <p className="whitespace-pre-wrap">{obs.body}</p>
              <p className="mt-1 text-label-md text-gray-500">
                {obs.authorRole} · {new Date(obs.createdAt).toLocaleString('es-BO')}
                {obs.status === 'RESOLVED' && obs.resolvedAt
                  ? ` · subsanada ${new Date(obs.resolvedAt).toLocaleString('es-BO')}`
                  : ''}
              </p>
            </li>
          ))}
        </ul>
      )}

      {canObserve && (
        <div className="space-y-2">
          <textarea
            rows={3}
            value={draft}
            disabled={isSubmitting}
            placeholder="Observación sobre la evidencia subida en esta subfase…"
            className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
            onChange={(event) => setDraft(event.target.value)}
          />
          {error && (
            <p className="text-body-md text-danger" role="alert">
              {error}
            </p>
          )}
          <Button
            type="button"
            disabled={isSubmitting}
            isLoading={isSubmitting}
            onClick={() => void handleSubmit()}
          >
            Registrar observación
          </Button>
        </div>
      )}
    </div>
  );
}

export type SubphaseEvidenceListProps = {
  evidences: SubphaseEvidenceItem[];
  isLoading: boolean;
  canSubsanate?: boolean;
  onSubsanate?: (evidenceId: string, filename: string) => void;
};

export function SubphaseEvidenceList({
  evidences,
  isLoading,
  canSubsanate = false,
  onSubsanate,
}: SubphaseEvidenceListProps) {
  return (
    <div className="mt-3 rounded-lg border border-primary-100 bg-primary-50/40 p-3">
      <div className="mb-2 flex items-center gap-2 text-label-md font-semibold uppercase text-primary-700">
        <FileText size={16} aria-hidden />
        Evidencias cargadas ({evidences.length})
      </div>
      {isLoading && evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Cargando evidencias…</p>
      ) : evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Aún no hay evidencias en esta subfase.</p>
      ) : (
        <ul className="space-y-2">
          {evidences.map((item) => (
            <li
              key={item.evidenceId}
              className="rounded-md border border-primary-100 bg-body px-3 py-2 text-body-md"
            >
              <p className="font-medium text-gray-900">{item.originalFilename}</p>
              <p className="text-body-md text-gray-600">{item.description}</p>
              <p className="mt-1 text-label-md text-gray-500">
                v{item.version} · {new Date(item.uploadedAt).toLocaleString('es-BO')}
              </p>
              <EvidenceVersionHistoryPanel
                evidenceId={item.evidenceId}
                filename={item.originalFilename}
              />
              {canSubsanate && onSubsanate && (
                <button
                  type="button"
                  className="mt-2 text-body-md font-medium text-secondary-600 underline decoration-secondary-400 underline-offset-2 transition-colors hover:text-secondary-800"
                  onClick={() => onSubsanate(item.evidenceId, item.originalFilename)}
                >
                  Subsanar evidencia
                </button>
              )}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
