import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';

export type FodaQuadrantValue = 'STRENGTH' | 'WEAKNESS' | 'OPPORTUNITY' | 'THREAT';
export type ComplianceValue = 'COMPLIES' | 'PARTIAL' | 'DOES_NOT_COMPLY';

export type IndicatorFodaValues = {
  fodaQuadrant: FodaQuadrantValue;
  compliance: ComplianceValue;
  synthesis: string;
};

export type IndicatorFodaFormProps = {
  canEdit: boolean;
  initialValues?: Partial<IndicatorFodaValues>;
  isLoading?: boolean;
  isSaving?: boolean;
  errorMessage?: string;
  successMessage?: string;
  onSave: (values: IndicatorFodaValues) => void;
};

const quadrantOptions = [
  { value: 'STRENGTH', label: 'Fortaleza' },
  { value: 'WEAKNESS', label: 'Debilidad' },
  { value: 'OPPORTUNITY', label: 'Oportunidad' },
  { value: 'THREAT', label: 'Amenaza' },
];

const complianceOptions = [
  { value: 'COMPLIES', label: 'Cumple' },
  { value: 'PARTIAL', label: 'Parcial' },
  { value: 'DOES_NOT_COMPLY', label: 'No cumple' },
];

export function IndicatorFodaForm({
  canEdit,
  initialValues,
  isLoading = false,
  isSaving = false,
  errorMessage,
  successMessage,
  onSave,
}: IndicatorFodaFormProps) {
  const [fodaQuadrant, setFodaQuadrant] = useState<FodaQuadrantValue>(
    initialValues?.fodaQuadrant ?? 'STRENGTH',
  );
  const [compliance, setCompliance] = useState<ComplianceValue>(
    initialValues?.compliance ?? 'PARTIAL',
  );
  const [synthesis, setSynthesis] = useState(initialValues?.synthesis ?? '');

  if (isLoading) {
    return <p className="text-body-md text-gray-500">Cargando análisis FODA…</p>;
  }

  if (!canEdit) {
    return (
      <div className="rounded-lg border border-gray-200 bg-gray-50 p-3">
        <p className="text-label-md font-medium uppercase text-gray-500">Análisis FODA</p>
        <dl className="mt-2 space-y-1 text-body-md text-gray-700">
          <div>
            <dt className="text-label-md text-gray-500">Cuadrante</dt>
            <dd>{quadrantOptions.find((o) => o.value === initialValues?.fodaQuadrant)?.label ?? 'Sin registrar'}</dd>
          </div>
          <div>
            <dt className="text-label-md text-gray-500">Cumplimiento</dt>
            <dd>{complianceOptions.find((o) => o.value === initialValues?.compliance)?.label ?? 'Sin registrar'}</dd>
          </div>
          <div>
            <dt className="text-label-md text-gray-500">Síntesis</dt>
            <dd>{initialValues?.synthesis?.trim() ? initialValues.synthesis : 'Sin síntesis'}</dd>
          </div>
        </dl>
      </div>
    );
  }

  return (
    <form
      className="space-y-3 rounded-lg border border-primary-100 bg-primary-50/40 p-3"
      onSubmit={(event) => {
        event.preventDefault();
        onSave({ fodaQuadrant, compliance, synthesis: synthesis.trim() });
      }}
    >
      <p className="text-label-md font-semibold uppercase text-primary-700">Análisis FODA</p>
      <Select
        label="Cuadrante FODA"
        requiredMark
        options={quadrantOptions}
        value={fodaQuadrant}
        onChange={(event) => setFodaQuadrant(event.target.value as FodaQuadrantValue)}
      />
      <Select
        label="Juicio de cumplimiento"
        requiredMark
        options={complianceOptions}
        value={compliance}
        onChange={(event) => setCompliance(event.target.value as ComplianceValue)}
      />
      <div className="space-y-1">
        <label htmlFor="foda-synthesis" className="block text-label-md text-gray-700">
          Síntesis
          <span className="ml-0.5 text-secondary">*</span>
        </label>
        <textarea
          id="foda-synthesis"
          required
          rows={4}
          value={synthesis}
          onChange={(event) => setSynthesis(event.target.value)}
          className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900 outline-none focus:border-primary-500"
        />
      </div>
      {errorMessage && <p className="text-label-md text-danger">{errorMessage}</p>}
      {successMessage && <p className="text-label-md text-success">{successMessage}</p>}
      <Button type="submit" variant="primary" isLoading={isSaving}>
        Guardar FODA
      </Button>
    </form>
  );
}
