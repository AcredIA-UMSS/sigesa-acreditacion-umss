import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { getListStagesQueryKey } from '../../../api/endpoints/stage-workflow/stage-workflow';
import type { StageDeliverableResponseDto } from '../../../api/model';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { customFetch } from '../../../lib/api/customFetch';
import { isApiError } from '../../../lib/api/apiError';
import { EvidencePreviewDialog } from '../../evidence/components/EvidencePreviewDialog';
import { useEvidencePreview } from '../../evidence/hooks/useEvidencePreview';
import { useProcessObservations } from '../../observations/hooks/useProcessObservations';
import { evidenceLabel } from '../../observations/model/observationView';
import { APPROVAL_LABEL, DELIVERABLE_LABEL } from './arcuPhaseGuide';

interface StageDeliverableFollowUpProps {
  processId: string;
  stageId: string;
  deliverables: StageDeliverableResponseDto[];
}

export function StageDeliverableFollowUp({ processId, stageId, deliverables }: StageDeliverableFollowUpProps) {
  const [openId, setOpenId] = useState<string | null>(null);
  const [drafts, setDrafts] = useState<Record<string, string>>({});
  const [notice, setNotice] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const preview = useEvidencePreview();
  const trace = useProcessObservations(processId);
  const uploaded = trace.evidences.data ?? [];
  const queryClient = useQueryClient();
  const save = useMutation({
    mutationFn: (input: { deliverableId: string; text: string }) =>
      customFetch(
        `/api/v1/processes/${processId}/stages/${stageId}/deliverables/${input.deliverableId}/feedback`,
        {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ technicalObservations: input.text }),
        },
      ),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: getListStagesQueryKey(processId) }),
  });

  async function register(deliverable: StageDeliverableResponseDto) {
    const id = deliverable.id;
    if (!id) return;
    const text = (drafts[id] ?? deliverable.technicalObservations ?? '').trim();
    if (text.length < 10) {
      setError('La retroalimentación debe tener al menos 10 caracteres.');
      setNotice(null);
      return;
    }
    setError(null);
    setNotice(null);
    try {
      await save.mutateAsync({ deliverableId: id, text });
      setNotice('Retroalimentación registrada. El seguimiento del documento quedó actualizado.');
    } catch (caught) {
      setError(isApiError(caught) ? caught.message : 'No se pudo registrar la retroalimentación.');
    }
  }

  return (
    <div className="mt-3">
      <p className="text-label-md font-medium uppercase text-gray-600">Entregables del proceso</p>
      <ul className="mt-2 space-y-3">
        {deliverables.map((deliverable) => {
          const id = deliverable.id ?? deliverable.deliverableCode ?? '';
          const label = DELIVERABLE_LABEL[deliverable.deliverableCode ?? ''] ?? deliverable.deliverableCode;
          const status = APPROVAL_LABEL[deliverable.approvalStatus ?? ''] ?? deliverable.approvalStatus ?? 'Pendiente';
          const open = openId === id;
          const text = drafts[id] ?? deliverable.technicalObservations ?? '';
          const source = uploaded.find((item) => item.documentAssetId && item.documentAssetId === deliverable.documentAssetId);
          return (
            <li key={id} className="rounded-lg border border-gray-200 bg-body px-3 py-3">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <span className="text-body-md text-gray-800">{label}</span>
                <span className="text-label-md text-gray-600">{status}</span>
              </div>
              {deliverable.folioCode && (
                <p className="mt-1 text-label-md text-gray-600">Folio {deliverable.folioCode}</p>
              )}
              {source && (
                <p className="mt-1 text-label-md text-gray-600">
                  Evidencia del coordinador en Cargar evidencia: {evidenceLabel(source)}
                </p>
              )}
              {deliverable.technicalObservations && (
                <p className="mt-2 text-body-md text-gray-800">
                  <span className="font-semibold">Seguimiento: </span>
                  {deliverable.technicalObservations}
                </p>
              )}
              <div className="mt-3 flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={() => {
                    if (!deliverable.documentAssetId) {
                      setError('El coordinador todavía no cargó este documento.');
                      setNotice(null);
                      return;
                    }
                    setError(null);
                    preview.open(deliverable.documentAssetId, label ?? 'Documento');
                  }}
                  className={actionClass(false)}
                >
                  Revisar
                </button>
                <button
                  type="button"
                  aria-expanded={open}
                  onClick={() => {
                    setOpenId(open ? null : id);
                    setError(null);
                    setNotice(null);
                  }}
                  className={actionClass(open)}
                >
                  Retroalimentación
                </button>
              </div>
              {open && (
                <form
                  className="mt-3 space-y-3 border-t border-gray-200 pt-3"
                  onSubmit={(event) => {
                    event.preventDefault();
                    void register(deliverable);
                  }}
                >
                  <label className="block space-y-1" htmlFor={`feedback-${id}`}>
                    <span className="block text-label-md text-gray-700">Observación del técnico</span>
                    <textarea
                      id={`feedback-${id}`}
                      rows={3}
                      value={text}
                      onChange={(event) => setDrafts((current) => ({ ...current, [id]: event.target.value }))}
                      className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors focus:border-primary-500"
                    />
                  </label>
                  <Button type="submit" disabled={save.isPending} isLoading={save.isPending && save.variables?.deliverableId === id}>
                    Registrar retroalimentación
                  </Button>
                </form>
              )}
            </li>
          );
        })}
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

function actionClass(active: boolean): string {
  return `shrink-0 rounded-full border px-2.5 py-0.5 text-label-md font-medium ${
    active
      ? 'border-primary-300 bg-primary-100 text-primary-800'
      : 'border-gray-300 bg-body text-gray-700 hover:bg-gray-50'
  }`;
}
