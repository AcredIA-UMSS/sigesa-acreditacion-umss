// frontend/src/features/accreditation-process/components/CreateProcessForm.tsx
import React, { useState } from 'react';
import type { CreateProcessRequestDto } from '../../../api/model';

// Interfaces ficticias basadas en supuestos catálogos
export interface CareerOption {
  id: string;
  name: string;
}

export interface TemplateOption {
  id: string;
  name: string;
  type: string; // Ej: CEUB, ARCU-SUR
}

interface CreateProcessFormProps {
  careers: CareerOption[];
  templates: TemplateOption[];
  onSubmit: (data: CreateProcessRequestDto) => void;
  isLoading: boolean;
  errorMessage: string | null;
}

export const CreateProcessForm: React.FC<CreateProcessFormProps> = ({
  careers,
  templates,
  onSubmit,
  isLoading,
  errorMessage,
}) => {
  const [selectedCareer, setSelectedCareer] = useState<string>('');
  const [selectedTemplate, setSelectedTemplate] = useState<string>('');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedCareer || !selectedTemplate) return;

    onSubmit({
      career_id: selectedCareer,
      template_id: selectedTemplate,
    });
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4 p-6 bg-white rounded-lg shadow-sm border border-gray-200">
      <h2 className="text-xl font-semibold text-gray-800">Iniciar Proceso de Acreditación</h2>
      <p className="text-sm text-gray-500 mb-4">
        Selecciona una carrera y una plantilla base. Se clonará la estructura de fases y subfases automáticamente.
      </p>

      {errorMessage && (
        <div className="p-3 bg-red-50 text-red-700 text-sm rounded-md border border-red-200">
          {errorMessage}
        </div>
      )}

      <div className="flex flex-col gap-1">
        <label htmlFor="careerSelect" className="text-sm font-medium text-gray-700">Carrera</label>
        <select
          id="careerSelect"
          value={selectedCareer}
          onChange={(e) => setSelectedCareer(e.target.value)}
          disabled={isLoading}
          className="p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
          required
        >
          <option value="" disabled>-- Seleccione una carrera --</option>
          {careers.map((career) => (
             <option key={career.id} value={career.id}>{career.name}</option>
          ))}
        </select>
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="templateSelect" className="text-sm font-medium text-gray-700">Plantilla Normativa</label>
        <select
          id="templateSelect"
          value={selectedTemplate}
          onChange={(e) => setSelectedTemplate(e.target.value)}
          disabled={isLoading}
          className="p-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
          required
        >
          <option value="" disabled>-- Seleccione una plantilla --</option>
          {templates.map((template) => (
             <option key={template.id} value={template.id}>
               {template.name} ({template.type})
             </option>
          ))}
        </select>
      </div>

      <div className="mt-4 flex justify-end">
        <button
          type="submit"
          disabled={isLoading || !selectedCareer || !selectedTemplate}
          className="px-4 py-2 bg-blue-600 text-white text-sm font-medium rounded-md hover:bg-blue-700 disabled:bg-blue-300 transition-colors"
        >
          {isLoading ? 'Iniciando proceso...' : 'Iniciar Proceso'}
        </button>
      </div>
    </form>
  );
};