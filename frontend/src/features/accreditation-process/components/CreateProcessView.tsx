// frontend/src/features/accreditation-process/components/CreateProcessView.tsx
import React, { useState, useEffect } from 'react';
import { CreateProcessForm, type CareerOption, type TemplateOption } from './CreateProcessForm';
import { useCreateAccreditationProcess } from '../hooks/useCreateAccreditationProcess';
import type { CreateProcessRequestDto } from '../../../api/model';

export const CreateProcessView: React.FC = () => {
  const { submitProcess, isPending, errorMessage, successData } = useCreateAccreditationProcess();

  // ⚠️ TODO: Reemplazar por hooks de Orval (ej. useGetCareers) cuando el backend los exponga
  const [careers, setCareers] = useState<CareerOption[]>([]);
  const [templates, setTemplates] = useState<TemplateOption[]>([]);
  const [isLoadingData, setIsLoadingData] = useState(true);

  // Simulamos la carga de catálogos desde una API
  useEffect(() => {
    const fetchMocks = async () => {
      // Simulamos un retraso de red de 1 segundo
      await new Promise((resolve) => setTimeout(resolve, 1000));
      
      setCareers([
        { id: '123e4567-e89b-12d3-a456-426614174000', name: 'Ingeniería de Sistemas' },
        { id: '123e4567-e89b-12d3-a456-426614174001', name: 'Medicina' }
      ]);
      
      setTemplates([
        { id: '987e6543-e21b-34d3-b456-426614174111', name: 'CEUB 2026', type: 'CEUB' },
        { id: '987e6543-e21b-34d3-b456-426614174222', name: 'ARCU-SUR v2', type: 'ARCU-SUR' }
      ]);
      
      setIsLoadingData(false);
    };

    fetchMocks();
  }, []);

  const handleFormSubmit = (payload: CreateProcessRequestDto) => {
    // Al enviar, usamos un UUID ficticio si seleccionaron los mocks,
    // o enviamos directamente los datos seleccionados
    submitProcess(payload);
  };

  if (isLoadingData) {
    return <div className="p-8 text-center text-gray-500 font-medium">Cargando catálogos...</div>;
  }

  if (successData) {
    return (
      <div className="max-w-2xl mx-auto py-8">
        <div className="p-6 bg-green-50 border border-green-200 rounded-lg shadow-sm">
          <h2 className="text-xl font-semibold text-green-800 mb-2">¡Proceso Creado con Éxito!</h2>
          <p className="text-sm text-green-700">
            El proceso para la carrera se ha inicializado correctamente.
          </p>
          <button 
            onClick={() => window.location.reload()}
            className="mt-4 px-4 py-2 bg-green-600 text-white text-sm font-medium rounded-md hover:bg-green-700 transition-colors"
          >
            Volver al panel
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-2xl mx-auto py-8">
      <CreateProcessForm
        careers={careers}
        templates={templates}
        isLoading={isPending}
        errorMessage={errorMessage}
        onSubmit={handleFormSubmit}
      />
    </div>
  );
};