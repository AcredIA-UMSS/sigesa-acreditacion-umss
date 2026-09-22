import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import { Plus, RefreshCw } from 'lucide-react';
import {
  getListProcessesQueryKey,
  useDeleteProcess,
} from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import type { ProcessSummaryResponseDto } from '../../../api/model';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { useAuth } from '../../../lib/auth/useAuth';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useProcessList } from '../hooks/useProcessList';
import { ProcessListTable } from './ProcessListTable';

export function ProcessListView() {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const { processes, isLoading, isError, errorMessage, refetch } = useProcessList();
  const isJd = session?.role === 'JD';

  const [actionError, setActionError] = useState<string | null>(null);
  const [processToDelete, setProcessToDelete] = useState<ProcessSummaryResponseDto | null>(null);

  const { mutateAsync: deleteProcess, isPending: isDeleting } = useDeleteProcess({
    mutation: {
      onSuccess: async () => {
        await queryClient.invalidateQueries({ queryKey: getListProcessesQueryKey() });
      },
    },
  });

  const handleConfirmDelete = async () => {
    if (!processToDelete?.id) {
      return;
    }

    setActionError(null);
    try {
      const response = await deleteProcess({ processId: processToDelete.id });
      if (response.status !== 204) {
        setActionError('No se pudo eliminar el proceso.');
        return;
      }
      setProcessToDelete(null);
    } catch (error) {
      setActionError(getApiErrorMessage(error, 'No se pudo eliminar el proceso.'));
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-heading-xl font-bold text-primary-800">Procesos de acreditación</h1>
          <p className="mt-1 text-body-md text-gray-600">
            Consulta procesos CEUB / ARCU-SUR según su rol y alcance de carrera. Haga clic en una
            fila para ver el detalle.
          </p>
        </div>
        <div className="flex flex-wrap items-center gap-3">
          <Button variant="ghost" onClick={refetch} isLoading={isLoading}>
            <RefreshCw size={16} />
            Actualizar
          </Button>
          {isJd && (
            <Link
              to="/procesos/nuevo"
              className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-3 text-label-md font-medium text-body transition-colors hover:bg-primary-500"
            >
              <Plus size={16} />
              Nuevo proceso
            </Link>
          )}
        </div>
      </div>

      {actionError && <Alert variant="error">{actionError}</Alert>}

      {isLoading && (
        <div className="rounded-2xl border border-gray-200 bg-body p-12 text-center">
          <p className="text-body-md text-gray-600">Cargando procesos…</p>
        </div>
      )}

      {isError && !isLoading && (
        <div className="rounded-2xl border border-danger/30 bg-danger/10 p-6">
          <p className="text-body-md text-danger">{errorMessage}</p>
          <Button className="mt-4" variant="secondary" onClick={refetch}>
            Reintentar
          </Button>
        </div>
      )}

      {!isLoading && !isError && (
        <ProcessListTable
          processes={processes}
          canDelete={isJd}
          isDeleteBusy={isDeleting}
          onDeleteRequest={setProcessToDelete}
        />
      )}

      <ConfirmDialog
        isOpen={processToDelete !== null}
        title="Eliminar proceso"
        description={
          processToDelete
            ? `¿Eliminar el proceso de ${processToDelete.careerName ?? 'esta carrera'}? Los procesos activos requieren no tener evidencias; los cerrados (desactivados) siempre pueden archivarse.`
            : ''
        }
        confirmLabel="Eliminar"
        isLoading={isDeleting}
        onClose={() => setProcessToDelete(null)}
        onConfirm={() => {
          void handleConfirmDelete();
        }}
      />
    </div>
  );
}
