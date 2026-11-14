import { useState } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { isApiError } from '../../../lib/api/apiError';
import { EvidencePreviewDialog } from '../../evidence/components/EvidencePreviewDialog';
import { useEvidencePreview } from '../../evidence/hooks/useEvidencePreview';
import { useProcessObservations, type EvidenceOption, type ProcessObservation } from '../hooks/useProcessObservations';
import { evidenceLabel, labelOf, STATUS_OPTIONS } from '../model/observationView';

interface ProcessEvidenceTraceProps {
  processId: string;
  mode: 'technician' | 'coordinator';
}

export function ProcessEvidenceTrace({ processId, mode }: ProcessEvidenceTraceProps) {
  const { list, evidences, act, create } = useProcessObservations(processId);
  const preview = useEvidencePreview();
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const items = evidences.data ?? [];
  const observations = list.data ?? [];

  async function send(observation: ProcessObservation) {
    setError(null);
    setNotice(null);
    try {
      await act.mutateAsync({ observationId: observation.id, action: 'submit' });
      setNotice('La observación quedó enviada al coordinador, sobre la evidencia que cargó.');
    } catch (caught) {
      setError(isApiError(caught) ? caught.message : 'No se pudo enviar la observación al coordinador.');
    }
  }

  return (
    <div>
      <p className="text-label-md font-medium uppercase text-gray-600">
        {mode === 'technician' ? 'Evidencias del coordinador' : 'Seguimiento del técnico'}
      </p>
      {evidences.isLoading && <p className="mt-2 text-body-md text-gray-600">Cargando evidencias…</p>}
      {evidences.isError && <p className="mt-2 text-body-md text-danger">No se pudieron cargar las evidencias.</p>}
      {!evidences.isLoading && items.length === 0 && (
        <p className="mt-2 text-body-md text-gray-600">
          El coordinador todavía no cargó evidencias en Cargar evidencia para esta carrera.
        </p>
      )}
      <ul className="mt-2 space-y-3">
        {items.map((item) => (
          <EvidenceTraceRow
            key={item.evidenceId}
            item={item}
            observations={observations.filter((observation) => observation.evidenceId === item.evidenceId)}
            mode={mode}
            pendingId={act.isPending ? act.variables?.observationId : undefined}
            onReview={item.documentAssetId
              ? () => preview.open(item.documentAssetId ?? '', item.originalFilename ?? evidenceLabel(item))
              : undefined}
            onSend={(observation) => void send(observation)}
            onRegister={mode === 'technician'
              ? async (text) => {
                setError(null);
                setNotice(null);
                try {
                  await create.mutateAsync({
                    evidenceId: item.evidenceId,
                    description: text,
                    kind: 'CONTENIDO',
                    priority: 'MEDIA',
                    compliance: 'NO_CUMPLE',
                    recommendation: text,
                  });
                  setNotice('Revisión registrada sobre la evidencia que subió el coordinador.');
                  return true;
                } catch (caught) {
                  setError(isApiError(caught) ? caught.message : 'No se pudo registrar la revisión.');
                  return false;
                }
              }
              : undefined}
            registering={create.isPending && create.variables?.evidenceId === item.evidenceId}
          />
        ))}
      </ul>
      {notice && <div className="mt-3"><Alert variant="success">{notice}</Alert></div>}
      {error && <div className="mt-3"><Alert variant="error">{error}</Alert></div>}
      <EvidencePreviewDialog
        isOpen={preview.isOpen}
        title={preview.title}
        objectUrl={preview.objectUrl}
        contentType={preview.contentType}
        isLoading={preview.isLoading}
        error={preview.error}
        onClose={preview.close}
        onDownload={preview.download}
      />
    </div>
  );
}

function EvidenceTraceRow({
  item,
  observations,
  mode,
  pendingId,
  onReview,
  onSend,
  onRegister,
  registering,
}: {
  item: EvidenceOption;
  observations: ProcessObservation[];
  mode: 'technician' | 'coordinator';
  pendingId?: string;
  onReview?: () => void;
  onSend: (observation: ProcessObservation) => void;
  onRegister?: (text: string) => Promise<boolean>;
  registering?: boolean;
}) {
  const [draft, setDraft] = useState('');
  const visible = mode === 'coordinator'
    ? observations.filter((observation) => observation.status !== 'BORRADOR')
    : observations;
  return (
    <li className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-3">
      <p className="text-body-md font-medium text-gray-900">{evidenceLabel(item)}</p>
      <p className="mt-1 text-label-md text-gray-600">
        Cargada por el coordinador en Cargar evidencia
        {item.folioCode ? ` · Folio ${item.folioCode}` : ''}
      </p>
      <div className="mt-2 flex flex-wrap gap-2">
        {onReview && (
          <button type="button" onClick={onReview} className="rounded-full border border-gray-300 bg-body px-2.5 py-0.5 text-label-md font-medium text-gray-700 hover:bg-gray-50">
            Visualizar
          </button>
        )}
      </div>
      {mode === 'technician' && onRegister && (
        <form
          className="mt-3 space-y-2"
          onSubmit={(event) => {
            event.preventDefault();
            const text = draft.trim();
            if (text.length < 10) return;
            void onRegister(text).then((saved) => {
              if (saved) setDraft('');
            });
          }}
        >
          <label className="block space-y-1" htmlFor={`review-${item.evidenceId}`}>
            <span className="block text-label-md text-gray-700">Revisión del técnico</span>
            <textarea
              id={`review-${item.evidenceId}`}
              rows={3}
              value={draft}
              onChange={(event) => setDraft(event.target.value)}
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors focus:border-primary-500"
            />
          </label>
          <Button type="submit" disabled={registering || draft.trim().length < 10} isLoading={registering}>
            Registrar revisión
          </Button>
        </form>
      )}
      {visible.length === 0 && (
        <p className="mt-2 text-body-md text-gray-600">
          {mode === 'coordinator'
            ? 'El técnico todavía no envió seguimiento sobre esta evidencia.'
            : 'Sin observación del técnico sobre esta evidencia.'}
        </p>
      )}
      <ul className="mt-2 space-y-2">
        {visible.map((observation) => (
          <li key={observation.id} className="rounded-lg border border-gray-200 bg-body px-3 py-2">
            <p className="text-label-md text-gray-600">
              Técnico · {labelOf(STATUS_OPTIONS, observation.status)}
            </p>
            <p className="mt-1 text-body-md text-gray-800">{observation.description}</p>
            {mode === 'technician' && observation.status === 'BORRADOR' && (
              <Button
                type="button"
                className="mt-2"
                disabled={pendingId === observation.id}
                isLoading={pendingId === observation.id}
                onClick={() => onSend(observation)}
              >
                Enviar al coordinador
              </Button>
            )}
          </li>
        ))}
      </ul>
    </li>
  );
}
