import { useMemo, useState } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { isApiError } from '../../../lib/api/apiError';
import {
  useProcessObservations,
  type ObservationDraftInput,
  type ProcessObservation,
} from '../hooks/useProcessObservations';
import {
  countObservations,
  evidenceLabel,
  matchesFilters,
  technicianLabel,
} from '../model/observationView';
import { ObservationCatalog } from './ObservationCatalog';
import { ObservationComposer } from './ObservationComposer';
import { ObservationFilters } from './ObservationFilters';
import { ObservationSummary } from './ObservationSummary';
import { ObservationThread } from './ObservationThread';

const EMPTY_DRAFT: ObservationDraftInput = {
  evidenceId: '',
  description: '',
  kind: 'CONTENIDO',
  priority: 'MEDIA',
  compliance: 'NO_CUMPLE',
  recommendation: '',
};

interface ObservationsPanelProps {
  processId: string;
  role: string;
}

export function ObservationsPanel({ processId, role }: ObservationsPanelProps) {
  const isTechnician = role === 'TD';
  const isCoordinator = role === 'CC';
  const [status, setStatus] = useState('');
  const [priority, setPriority] = useState('');
  const [evidenceId, setEvidenceId] = useState('');
  const [authorId, setAuthorId] = useState('');
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [draft, setDraft] = useState<ObservationDraftInput>(EMPTY_DRAFT);
  const [comment, setComment] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const { list, evidences, create, update, act } = useProcessObservations(processId);

  const items = list.data ?? [];
  const evidenceOptions = evidences.data ?? [];
  const evidenceById = useMemo(
    () => new Map((evidences.data ?? []).map((item) => [item.evidenceId, item])),
    [evidences.data],
  );
  const counts = useMemo(() => countObservations(list.data ?? []), [list.data]);
  const visible = useMemo(
    () => (list.data ?? []).filter((item) => matchesFilters(item, { status, priority, evidenceId, authorId })),
    [list.data, status, priority, evidenceId, authorId],
  );
  const authors = useMemo(() => {
    const ids = [...new Set((list.data ?? []).map((item) => item.authorId))];
    return ids.map((id) => ({ value: id, label: technicianLabel(id) }));
  }, [list.data]);
  const evidenceChoices = evidenceOptions.map((item) => ({
    value: item.evidenceId,
    label: evidenceLabel(item),
  }));
  const selected = items.find((item) => item.id === selectedId) ?? null;

  function labelFor(id: string): string {
    return evidenceLabel(evidenceById.get(id));
  }

  function selectObservation(item: ProcessObservation) {
    setSelectedId(item.id);
    setComment('');
    setFormError(null);
    if (item.status === 'BORRADOR') {
      setDraft({
        evidenceId: item.evidenceId,
        description: item.description,
        kind: item.kind,
        priority: item.priority,
        compliance: item.compliance,
        recommendation: item.recommendation ?? '',
      });
    }
  }

  async function run(action: () => Promise<unknown>, success: string) {
    setFormError(null);
    setNotice(null);
    try {
      await action();
      setNotice(success);
    } catch (error) {
      setFormError(isApiError(error) ? error.message : 'No se pudo completar la acción.');
    }
  }

  function validateDraft(forSubmit: boolean): string | null {
    if (!draft.evidenceId) return 'Seleccione la evidencia.';
    if (draft.description.trim().length < 10) return 'La descripción debe tener al menos 10 caracteres.';
    if (forSubmit && !draft.recommendation.trim()) return 'La recomendación es obligatoria para enviar.';
    return null;
  }

  function saveDraft() {
    const error = validateDraft(false);
    if (error) {
      setFormError(error);
      setNotice(null);
      return;
    }
    const saving = selected?.status === 'BORRADOR'
      ? update.mutateAsync({ observationId: selected.id, draft })
      : create.mutateAsync(draft);
    void run(async () => {
      const saved = await saving;
      setSelectedId(saved.id);
      setDraft(EMPTY_DRAFT);
    }, 'Observación guardada.');
  }

  function sendDraft() {
    const error = validateDraft(true);
    if (error || !selected || selected.status !== 'BORRADOR') {
      setFormError(error ?? 'Guarde el borrador antes de enviarlo.');
      setNotice(null);
      return;
    }
    void run(async () => {
      await update.mutateAsync({ observationId: selected.id, draft });
      await act.mutateAsync({ observationId: selected.id, action: 'submit' });
      setDraft(EMPTY_DRAFT);
    }, 'Retroalimentación enviada al coordinador.');
  }

  function requireComment(action: () => Promise<unknown>, success: string) {
    if (!comment.trim()) {
      setFormError('El comentario es obligatorio.');
      setNotice(null);
      return;
    }
    void run(action, success);
  }

  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm" data-testid="process-observations">
      <div className="mb-6">
        <h2 className="text-heading-lg font-semibold text-primary-800">Observaciones</h2>
        <p className="mt-1 text-body-md text-gray-600">
          {isTechnician
            ? 'Registre el cumplimiento de cada evidencia y envíe la retroalimentación al coordinador.'
            : 'Revise las observaciones del técnico, responda y marque el seguimiento.'}
        </p>
      </div>

      <div className="space-y-4">
        <ObservationSummary counts={counts} activeStatus={status} onSelect={setStatus} />
        <ObservationFilters
          status={status}
          priority={priority}
          evidenceId={evidenceId}
          authorId={authorId}
          evidences={evidenceChoices}
          authors={authors}
          onStatus={setStatus}
          onPriority={setPriority}
          onEvidence={setEvidenceId}
          onAuthor={setAuthorId}
        />
      </div>

      {notice && <div className="mt-4"><Alert variant="success">{notice}</Alert></div>}
      {formError && <div className="mt-4"><Alert variant="error">{formError}</Alert></div>}
      {list.isError && (
        <div className="mt-4">
          <Alert variant="error">No se pudieron cargar las observaciones.</Alert>
        </div>
      )}
      {isTechnician && evidenceOptions.length === 0 && !evidences.isLoading && (
        <div className="mt-4">
          <Alert variant="info">
            Este proceso todavía no tiene evidencias. Puede consultar la estructura, pero el registro queda disponible cuando exista un documento cargado.
          </Alert>
        </div>
      )}

      <div className="mt-6 grid gap-6 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
        <ObservationCatalog
          items={visible}
          selectedId={selectedId}
          isLoading={list.isLoading}
          evidenceLabelFor={labelFor}
          onSelect={selectObservation}
        />
        <div className="space-y-4">
          {isTechnician && (
            <ObservationComposer
              mode={selected?.status === 'BORRADOR' ? 'edit' : 'create'}
              draft={draft}
              evidences={evidenceChoices}
              isSaving={create.isPending || update.isPending}
              isSending={act.isPending}
              canSend={selected?.status === 'BORRADOR'}
              onChange={setDraft}
              onSave={saveDraft}
              onSend={sendDraft}
            />
          )}
          {selected && (
            <ObservationThread
              observation={selected}
              evidenceLabel={labelFor(selected.evidenceId)}
              isTechnician={isTechnician}
              isCoordinator={isCoordinator}
              comment={comment}
              isActing={act.isPending}
              onComment={setComment}
              onReview={() => void run(
                () => act.mutateAsync({ observationId: selected.id, action: 'review' }),
                'Observación en revisión.',
              )}
              onReply={() => requireComment(
                () => act.mutateAsync({ observationId: selected.id, action: 'reply', body: comment }),
                'Respuesta registrada.',
              )}
              onAttend={() => requireComment(
                () => act.mutateAsync({ observationId: selected.id, action: 'attend', body: comment }),
                'Observación atendida.',
              )}
              onClose={() => requireComment(
                () => act.mutateAsync({ observationId: selected.id, action: 'close', body: comment }),
                'Observación cerrada.',
              )}
            />
          )}
          {!selected && isCoordinator && (
            <p className="rounded-xl border border-dashed border-gray-300 px-4 py-8 text-center text-body-md text-gray-600">
              Seleccione una observación para ver el indicador, la evidencia y el historial.
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
