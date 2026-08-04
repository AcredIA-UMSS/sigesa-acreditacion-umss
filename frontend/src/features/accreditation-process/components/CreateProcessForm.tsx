import React, { useState } from 'react';
import type { CreateProcessRequestDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { CareerAutocomplete } from './CareerAutocomplete';
import { SEED_TEMPLATES } from '../constants/seedCatalog';

export interface CareerOption {
  id: string;
  name: string;
  code?: string;
}

export interface TemplateOption {
  id: string;
  name: string;
  type: string;
}

interface CreateProcessFormProps {
  onSubmit: (data: CreateProcessRequestDto) => void;
  isLoading: boolean;
  errorMessage: string | null;
}

export const CreateProcessForm: React.FC<CreateProcessFormProps> = ({
  onSubmit,
  isLoading,
  errorMessage,
}) => {
  const [selectedCareer, setSelectedCareer] = useState<CareerOption | null>(null);
  const [selectedTemplate, setSelectedTemplate] = useState<string>('');

  const templates: TemplateOption[] = SEED_TEMPLATES.map((template) => ({ ...template }));

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!selectedCareer || !selectedTemplate) {
      return;
    }

    onSubmit({
      career_id: selectedCareer.id,
      template_id: selectedTemplate,
    });
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-4 rounded-lg border border-gray-300 bg-body p-6 shadow-sm"
    >
      <div>
        <h2 className="text-heading-md text-gray-900">Iniciar Proceso de Acreditación</h2>
        <p className="mt-1 text-body-md text-gray-600">
          Seleccione una carrera UMSS y una plantilla normativa CEUB o ARCU-SUR. El sistema clonará
          la estructura de fases y subfases automáticamente.
        </p>
      </div>

      {errorMessage && (
        <div className="rounded-md border border-danger/30 bg-danger/10 p-3 text-body-md text-danger">
          {errorMessage}
        </div>
      )}

      <CareerAutocomplete
        value={selectedCareer}
        onChange={setSelectedCareer}
        disabled={isLoading}
      />

      <div className="flex flex-col gap-1">
        <label htmlFor="templateSelect" className="text-label-md text-gray-700">
          Plantilla normativa
        </label>
        <select
          id="templateSelect"
          value={selectedTemplate}
          onChange={(event) => setSelectedTemplate(event.target.value)}
          disabled={isLoading}
          className="rounded-lg border border-gray-300 bg-body p-3 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
          required
        >
          <option value="" disabled>
            -- Seleccione CEUB o ARCU-SUR --
          </option>
          {templates.map((template) => (
            <option key={template.id} value={template.id}>
              {template.name} ({template.type})
            </option>
          ))}
        </select>
      </div>

      <div className="mt-2 flex justify-end">
        <Button type="submit" disabled={isLoading || !selectedCareer || !selectedTemplate}>
          {isLoading ? 'Iniciando proceso...' : 'Iniciar Proceso'}
        </Button>
      </div>
    </form>
  );
};
