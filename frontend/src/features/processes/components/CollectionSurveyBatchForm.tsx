import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';

export type CollectionSurveyBatchFormProps = {
  canRegister: boolean;
  batches: Array<{ id?: string; audience?: string; responsesCount?: number }>;
  isSaving?: boolean;
  errorMessage?: string;
  successMessage?: string;
  onRegister: (values: { audience: string; responsesCount: number }) => void;
};

export function CollectionSurveyBatchForm({
  canRegister,
  batches,
  isSaving = false,
  errorMessage,
  successMessage,
  onRegister,
}: CollectionSurveyBatchFormProps) {
  const [audience, setAudience] = useState('ESTUDIANTES');
  const [responsesCount, setResponsesCount] = useState('1');

  return (
    <div className="mt-4 rounded-xl border border-primary-100 bg-body p-4">
      <h4 className="text-heading-sm font-semibold text-primary-800">Encuestas primarias</h4>
      <p className="mt-1 text-body-md text-gray-600">
        Se requiere al menos un lote registrado para la compuerta E2 → E3.
      </p>

      {batches.length > 0 ? (
        <ul className="mt-3 space-y-1">
          {batches.map((batch, index) => (
            <li key={batch.id ?? `${batch.audience}-${index}`} className="text-body-md text-gray-700">
              {batch.audience ?? 'Audiencia'} · {batch.responsesCount ?? 0} respuestas
            </li>
          ))}
        </ul>
      ) : (
        <p className="mt-3 text-body-md text-gray-500">Aún no hay lotes registrados.</p>
      )}

      {canRegister && (
        <form
          className="mt-4 grid gap-3 sm:grid-cols-[1fr_8rem_auto] sm:items-end"
          onSubmit={(event) => {
            event.preventDefault();
            const count = Number.parseInt(responsesCount, 10);
            onRegister({ audience: audience.trim(), responsesCount: Number.isNaN(count) ? 0 : count });
          }}
        >
          <TextInput
            label="Audiencia"
            requiredMark
            maxLength={32}
            value={audience}
            onChange={(event) => setAudience(event.target.value)}
          />
          <TextInput
            label="Respuestas"
            requiredMark
            type="number"
            min={1}
            value={responsesCount}
            onChange={(event) => setResponsesCount(event.target.value)}
          />
          <Button type="submit" variant="secondary" isLoading={isSaving}>
            Registrar lote
          </Button>
        </form>
      )}
      {errorMessage && <p className="mt-2 text-label-md text-danger">{errorMessage}</p>}
      {successMessage && <p className="mt-2 text-label-md text-success">{successMessage}</p>}
    </div>
  );
}
