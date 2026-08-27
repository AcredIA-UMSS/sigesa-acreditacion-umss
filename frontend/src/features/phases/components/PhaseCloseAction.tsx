import { CheckCircle2, Lock } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import {
  closePhase,
  isPhaseClosureBlockedError,
  type PendingSubphaseItem,
} from '../api/phaseWorkflowApi';

export type PhaseCloseActionProps = {
  processId: string;
  phaseId?: string;
  phaseName: string;
  phaseStatus?: string;
  onCompleted: () => void;
  onNavigateToSubphase?: (subphaseId: string) => void;
};

export function PhaseCloseAction({
  processId,
  phaseId,
  phaseName,
  phaseStatus,
  onCompleted,
  onNavigateToSubphase,
}: PhaseCloseActionProps) {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pendingSubphases, setPendingSubphases] = useState<PendingSubphaseItem[]>([]);
  const [success, setSuccess] = useState<string | null>(null);

  if (!phaseId || phaseStatus === 'COMPLETADA') {
    return null;
  }

  const handleClose = async () => {
    const confirmed = window.confirm(
      `¿Confirma el cierre de la fase «${phaseName}»? Todas las subfases deben estar aprobadas.`,
    );
    if (!confirmed) return;

    setIsSubmitting(true);
    setError(null);
    setPendingSubphases([]);
    setSuccess(null);

    try {
      await closePhase(processId, phaseId);
      setSuccess(`Fase «${phaseName}» completada correctamente.`);
      onCompleted();
    } catch (err) {
      if (isPhaseClosureBlockedError(err)) {
        setError(err.message);
        setPendingSubphases(err.pendingSubphases);
      } else {
        setError(err instanceof Error ? err.message : 'No se pudo cerrar la fase');
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
            Cierre de fase
          </p>
          <p className="text-body-md text-gray-600">
            Disponible cuando todas las subfases estén en estado APROBADO.
          </p>
        </div>
        <Button
          type="button"
          disabled={isSubmitting}
          isLoading={isSubmitting}
          onClick={() => void handleClose()}
        >
          <CheckCircle2 size={16} aria-hidden />
          Cerrar fase
        </Button>
      </div>

      {error && (
        <div className="mt-3 rounded-lg border border-danger/30 bg-danger/10 p-3" role="alert">
          <p className="flex items-center gap-2 text-body-md font-medium text-danger">
            <Lock size={16} aria-hidden />
            {error}
          </p>
          {pendingSubphases.length > 0 && (
            <ul className="mt-2 space-y-1">
              {pendingSubphases.map((item) => (
                <li key={item.subphaseId} className="flex flex-wrap items-center gap-2 text-body-md text-gray-700">
                  <span>
                    {item.order != null ? `${item.order}. ` : ''}
                    {item.name} — <span className="font-medium">{item.status}</span>
                  </span>
                  {onNavigateToSubphase && (
                    <button
                      type="button"
                      className="text-body-md font-medium text-primary-600 hover:text-primary-800"
                      onClick={() => onNavigateToSubphase(item.subphaseId)}
                    >
                      Ir a subfase
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
