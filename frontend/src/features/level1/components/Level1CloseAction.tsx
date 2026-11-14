import { CheckCircle2, Lock } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import {
  closeLevel1,
  isLevel1ClosureBlockedError,
  type PendingIndicatorItem,
} from '../api/level1WorkflowApi';

export type Level1CloseActionProps = {
  processId: string;
  level1Id?: string;
  level1Name: string;
  level1Label?: string;
  level1Status?: string;
  onCompleted: () => void;
  onNavigateToIndicator?: (indicatorId: string) => void;
};

export function Level1CloseAction({
  processId,
  level1Id,
  level1Name,
  level1Label = 'Nivel 1',
  level1Status,
  onCompleted,
  onNavigateToIndicator,
}: Level1CloseActionProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingIndicators, setPendingIndicators] = useState<PendingIndicatorItem[]>([]);
  const [success, setSuccess] = useState<string | null>(null);

  if (!level1Id || level1Status === 'COMPLETADA') {
    return null;
  }

  const closeLabel = level1Label.toLowerCase().startsWith('nivel')
    ? 'Cerrar nivel 1'
    : `Cerrar ${level1Label.toLowerCase()}`;

  const handleClose = async () => {
    const confirmed = window.confirm(
      `¿Confirma el cierre de «${level1Name}»? Todos los indicadores del subárbol deben estar aprobados.`,
    );
    if (!confirmed) return;

    setIsSubmitting(true);
    setError(null);
    setPendingIndicators([]);
    setSuccess(null);

    try {
      await closeLevel1(processId, level1Id);
      setSuccess(`«${level1Name}» completado correctamente.`);
      onCompleted();
    } catch (err) {
      if (isLevel1ClosureBlockedError(err)) {
        setError(err.message);
        setPendingIndicators(err.pendingIndicators);
      } else {
        setError(err instanceof Error ? err.message : 'No se pudo cerrar el Nivel 1');
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="border-t border-gray-100 bg-gray-50 px-5 py-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <p className="text-label-md font-semibold uppercase text-gray-700">
            Cierre de {level1Label.toLowerCase()}
          </p>
          <p className="text-body-md text-gray-600">
            Disponible cuando todos los indicadores del subárbol estén en estado APROBADO.
          </p>
        </div>
        <Button
          type="button"
          disabled={isSubmitting}
          isLoading={isSubmitting}
          onClick={() => void handleClose()}
        >
          <CheckCircle2 size={16} aria-hidden />
          {closeLabel}
        </Button>
      </div>

      {error && (
        <div className="mt-3 rounded-lg border border-danger/30 bg-danger/10 p-3" role="alert">
          <p className="flex items-center gap-2 text-body-md font-medium text-danger">
            <Lock size={16} aria-hidden />
            {error}
          </p>
          {pendingIndicators.length > 0 && (
            <ul className="mt-2 space-y-1">
              {pendingIndicators.map((item) => (
                <li
                  key={item.indicatorId}
                  className="flex flex-wrap items-center gap-2 text-body-md text-gray-700"
                >
                  <span>
                    {item.order != null ? `${item.order}. ` : ''}
                    {item.code && (
                      <span className="mr-1 font-mono text-label-md">{item.code}</span>
                    )}
                    {item.name} — <span className="font-medium">{item.status}</span>
                  </span>
                  {onNavigateToIndicator && (
                    <button
                      type="button"
                      className="text-body-md font-medium text-primary-600 hover:text-primary-800"
                      onClick={() => onNavigateToIndicator(item.indicatorId)}
                    >
                      Ir a indicador
                    </button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}

      {success && (
        <p className="mt-3 text-body-md text-success" role="status">
          {success}
        </p>
      )}
    </div>
  );
}
