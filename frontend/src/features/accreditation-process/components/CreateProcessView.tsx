import React from 'react';
import { CreateProcessForm } from './CreateProcessForm';
import { useCreateAccreditationProcess } from '../hooks/useCreateAccreditationProcess';
import type { CreateProcessRequestDto } from '../../../api/model';
import { Link } from 'react-router-dom';

export const CreateProcessView: React.FC = () => {
  const { submitProcess, isPending, errorMessage, successData } = useCreateAccreditationProcess();

  const handleFormSubmit = (payload: CreateProcessRequestDto) => {
    submitProcess(payload);
  };

  if (successData) {
    return (
      <div className="mx-auto max-w-2xl py-8">
        <div className="rounded-lg border border-success/30 bg-success/10 p-6 shadow-sm">
          <h2 className="text-heading-md text-success">Proceso creado con éxito</h2>
          <p className="mt-2 text-body-md text-gray-700">
            El proceso de acreditación para la carrera seleccionada se inicializó correctamente.
          </p>
          <div className="mt-4 flex flex-wrap gap-3">
            {successData.id && (
              <Link
                to={`/procesos/${successData.id}`}
                className="inline-flex items-center justify-center rounded-lg bg-primary-600 px-4 py-3 text-label-md font-medium text-body transition-colors hover:bg-primary-500"
              >
                Ver detalle del proceso
              </Link>
            )}
            <Link
              to="/procesos"
              className="inline-flex items-center justify-center rounded-lg border border-primary-300 bg-transparent px-4 py-3 text-label-md font-medium text-primary-700 transition-colors hover:bg-primary-50"
            >
              Ir al listado
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl py-8">
      <CreateProcessForm
        onSubmit={handleFormSubmit}
        isLoading={isPending}
        errorMessage={errorMessage}
      />
    </div>
  );
};
