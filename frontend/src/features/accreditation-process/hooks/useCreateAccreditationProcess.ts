// frontend/src/features/accreditation-process/hooks/useCreateAccreditationProcess.ts
import { useState } from 'react';
import { useCreateProcess } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación'; 
import type { CreateProcessRequestDto, ProcessResponseDto } from '../../../api/model';

interface UseCreateAccreditationProcessReturn {
  submitProcess: (data: CreateProcessRequestDto) => void;
  isPending: boolean;
  errorMessage: string | null;
  successData: ProcessResponseDto | null;
  resetState: () => void;
}

export const useCreateAccreditationProcess = (): UseCreateAccreditationProcessReturn => {
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successData, setSuccessData] = useState<ProcessResponseDto | null>(null);

  const { mutate, isPending } = useCreateProcess({
    mutation: {
        onSuccess: (response) => {
            if (response.status === 201) {
              setErrorMessage(null);
              // 👇 Aquí agregamos "as ProcessResponseDto" para calmar a TypeScript
              setSuccessData(response.data as ProcessResponseDto);
            } else if (response.status === 409) {
              setErrorMessage('La carrera seleccionada ya cuenta con un proceso de acreditación ACTIVO.');
            } else if (response.status === 403) {
              setErrorMessage('No tienes permisos suficientes (Se requiere rol de Jefe de Departamento).');
            } else if (response.status === 404) {
              setErrorMessage('La plantilla seleccionada no se encuentra disponible.');
            } else {
              setErrorMessage('Ocurrió un error inesperado al inicializar el proceso.');
            }
          },
      onError: (error: any) => {
        // Orval típicamente envuelve el error de Axios. Evaluamos el status HTTP.
        const status = error?.response?.status;
        
        if (status === 409) {
          setErrorMessage('La carrera seleccionada ya cuenta con un proceso de acreditación ACTIVO.');
        } else if (status === 403) {
          setErrorMessage('No tienes permisos suficientes (Se requiere rol de Jefe de Departamento).');
        } else if (status === 404) {
          setErrorMessage('La plantilla seleccionada no se encuentra disponible.');
        } else {
          setErrorMessage('Ocurrió un error inesperado al inicializar el proceso.');
        }
      },
    },
  });

  const submitProcess = (data: CreateProcessRequestDto) => {
    setErrorMessage(null);
    setSuccessData(null);
    mutate({ data });
  };

  const resetState = () => {
    setErrorMessage(null);
    setSuccessData(null);
  };

  return { submitProcess, isPending, errorMessage, successData, resetState };
};