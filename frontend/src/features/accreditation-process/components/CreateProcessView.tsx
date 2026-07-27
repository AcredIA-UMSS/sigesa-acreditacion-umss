// frontend/src/features/accreditation-process/components/CreateProcessView.tsx
import React, { useMemo } from 'react';
import { useList1 } from '../../../api/endpoints/program-catalog-controller/program-catalog-controller';
import { CreateProcessForm, type CareerOption, type TemplateOption } from './CreateProcessForm';
import { useCreateAccreditationProcess } from '../hooks/useCreateAccreditationProcess';
import { SEED_TEMPLATES } from '../constants/seedCatalog';
import type { CreateProcessRequestDto } from '../../../api/model';

export const CreateProcessView: React.FC = () => {
  const { submitProcess, isPending, errorMessage, successData } = useCreateAccreditationProcess();
  const { data: programsResponse, isLoading: isLoadingPrograms, isError: isProgramsError } = useList1();

  const careers = useMemo<CareerOption[]>(() => {
    const programs = programsResponse?.data ?? [];
    return programs
      .filter((program) => program.id && program.name)
      .map((program) => ({
        id: program.id as string,
        name: program.name as string,
      }));
  }, [programsResponse]);

  const templates = useMemo<TemplateOption[]>(
    () => SEED_TEMPLATES.map((template) => ({ ...template })),
    [],
  );

  const handleFormSubmit = (payload: CreateProcessRequestDto) => {
    submitProcess(payload);
  };

  if (isLoadingPrograms) {
    return <div className="p-8 text-center text-gray-500 font-medium">Cargando catálogos...</div>;
  }

  if (isProgramsError || careers.length === 0) {
    return (
      <div className="p-8 text-center text-danger text-body-md">
        No se pudieron cargar las carreras. Verifique que el backend esté en ejecución e inicie sesión nuevamente.
      </div>
    );
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
