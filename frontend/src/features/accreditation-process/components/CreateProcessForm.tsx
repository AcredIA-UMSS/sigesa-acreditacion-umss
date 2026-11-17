import React, { useState } from 'react';
import type { CreateProcessRequestDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { CareerAutocomplete } from './CareerAutocomplete';
import { usePublishedTemplates } from '../hooks/usePublishedTemplates';

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
  const {
    templates,
    isLoading: isLoadingTemplates,
    isError: isTemplatesError,
    refetch: refetchTemplates,
  } = usePublishedTemplates();

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

  const formDisabled = isLoading || isLoadingTemplates;
  const hasTemplates = templates.length > 0;

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-4 rounded-lg border border-gray-300 bg-body p-6 shadow-sm"
    >
      <div>
        <h2 className="text-heading-md text-gray-900">Iniciar Proceso de Acreditación</h2>
        <p className="mt-1 text-body-md text-gray-600">
          Seleccione una carrera UMSS y una plantilla normativa publicada. El sistema clonará la
          jerarquía N1→N2→N3→Indicador (o fases/subfases legacy) automáticamente.
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
        disabled={formDisabled}
      />

      <div className="flex flex-col gap-1">
        <label htmlFor="templateSelect" className="text-label-md text-gray-700">
          Plantilla normativa
        </label>
        <select
          id="templateSelect"
          value={selectedTemplate}
          onChange={(event) => setSelectedTemplate(event.target.value)}
          disabled={formDisabled || !hasTemplates}
          className="rounded-lg border border-gray-300 bg-body p-3 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
          required
        >
          <option value="" disabled>
            {isLoadingTemplates
              ? 'Cargando plantillas publicadas…'
              : hasTemplates
                ? '-- Seleccione una plantilla --'
                : 'No hay plantillas publicadas disponibles'}
          </option>
          {templates.map((template) => (
            <option key={template.id} value={template.id}>
              {template.name}
            </option>
          ))}
        </select>
        {isTemplatesError && (
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <p className="text-body-md text-danger">No se pudieron cargar las plantillas.</p>
            <button
              type="button"
              onClick={() => void refetchTemplates()}
              className="text-body-md font-medium text-primary-600 hover:text-primary-800"
            >
              Reintentar
            </button>
          </div>
        )}
        {!isLoadingTemplates && !isTemplatesError && !hasTemplates && (
          <p className="mt-1 text-body-md text-gray-600">
            Publique una plantilla en{' '}
            <span className="font-medium text-primary-700">Admin → Plantillas</span> para habilitarla
            aquí.
          </p>
        )}
      </div>

      <div className="mt-2 flex justify-end">
        <Button
          type="submit"
          disabled={formDisabled || !selectedCareer || !selectedTemplate || !hasTemplates}
        >
          {isLoading ? 'Iniciando proceso...' : 'Iniciar Proceso'}
        </Button>
      </div>
    </form>
  );
};
