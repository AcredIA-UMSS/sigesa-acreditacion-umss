import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { TextInput } from '../../../components/ui/TextInput';
import type { ObservationDraftInput } from '../hooks/useProcessObservations';
import { COMPLIANCE_OPTIONS, KIND_OPTIONS, PRIORITY_OPTIONS } from '../model/observationView';

interface ObservationComposerProps {
  mode: 'create' | 'edit';
  draft: ObservationDraftInput;
  evidences: { value: string; label: string }[];
  isSaving: boolean;
  isSending: boolean;
  canSend: boolean;
  onChange: (draft: ObservationDraftInput) => void;
  onSave: () => void;
  onSend: () => void;
}

export function ObservationComposer({
  mode,
  draft,
  evidences,
  isSaving,
  isSending,
  canSend,
  onChange,
  onSave,
  onSend,
}: ObservationComposerProps) {
  return (
    <form
      className="grid gap-4 rounded-xl border border-gray-200 bg-gray-50 p-4"
      onSubmit={(event) => {
        event.preventDefault();
        onSave();
      }}
    >
      <div>
        <h3 className="text-heading-sm font-semibold text-primary-800">
          {mode === 'edit' ? 'Editar borrador' : 'Nueva observación'}
        </h3>
        <p className="mt-1 text-body-md text-gray-600">
          La descripción es obligatoria. La recomendación se exige al enviar la retroalimentación.
        </p>
      </div>
      <Select
        id="observation-evidence"
        label="Evidencia e indicador"
        requiredMark
        options={[{ value: '', label: 'Seleccione una evidencia' }, ...evidences]}
        value={draft.evidenceId}
        onChange={(event) => onChange({ ...draft, evidenceId: event.target.value })}
      />
      <label className="block space-y-1" htmlFor="observation-description">
        <span className="block text-label-md text-gray-700">
          Descripción
          <span className="ml-0.5 text-secondary">*</span>
        </span>
        <textarea
          id="observation-description"
          rows={4}
          value={draft.description}
          onChange={(event) => onChange({ ...draft, description: event.target.value })}
          className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none transition-colors focus:border-primary-500"
        />
      </label>
      <div className="grid gap-4 md:grid-cols-3">
        <Select
          id="observation-kind"
          label="Tipo"
          options={[...KIND_OPTIONS]}
          value={draft.kind}
          onChange={(event) => onChange({ ...draft, kind: event.target.value })}
        />
        <Select
          id="observation-priority"
          label="Prioridad"
          options={[...PRIORITY_OPTIONS]}
          value={draft.priority}
          onChange={(event) => onChange({ ...draft, priority: event.target.value })}
        />
        <Select
          id="observation-compliance"
          label="Cumplimiento"
          options={[...COMPLIANCE_OPTIONS]}
          value={draft.compliance}
          onChange={(event) => onChange({ ...draft, compliance: event.target.value })}
        />
      </div>
      <TextInput
        id="observation-recommendation"
        label="Recomendación"
        value={draft.recommendation}
        onChange={(event) => onChange({ ...draft, recommendation: event.target.value })}
      />
      <div className="flex flex-wrap gap-3">
        <Button type="submit" isLoading={isSaving}>Guardar borrador</Button>
        {canSend && (
          <Button type="button" variant="secondary" isLoading={isSending} onClick={onSend}>
            Enviar al coordinador
          </Button>
        )}
      </div>
    </form>
  );
}
