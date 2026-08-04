import { Link } from 'react-router-dom';
import { Plus, RefreshCw } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { useAuth } from '../../../lib/auth/useAuth';
import { useProcessList } from '../hooks/useProcessList';
import { ProcessListTable } from './ProcessListTable';

export function ProcessListView() {
  const { session } = useAuth();
  const { processes, isLoading, isError, errorMessage, refetch } = useProcessList();
  const isJd = session?.role === 'JD';

  return (
    <div className="space-y-6">
      <div className="flex flex-col justify-between gap-4 md:flex-row md:items-center">
        <div>
          <h1 className="text-heading-xl font-bold text-primary-800">Procesos de acreditación</h1>
          <p className="mt-1 text-body-md text-gray-600">
            Consulta procesos CEUB / ARCU-SUR según su rol y alcance de carrera.
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

      {!isLoading && !isError && <ProcessListTable processes={processes} />}
    </div>
  );
}
