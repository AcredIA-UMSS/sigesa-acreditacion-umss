import { CheckCircle2, XCircle } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { approveNormativeIndicator, rejectNormativeIndicator } from '../api/indicatorWorkflowApi';

export type NormativeIndicatorReviewActionsProps = {
  indicatorId: string;
  indicatorLabel: string;
  indicatorStatus?: string;
  hasEvidences: boolean;
  onCompleted: () => void;
};

export function NormativeIndicatorReviewActions({
  indicatorId,
  indicatorLabel,
  indicatorStatus,
  hasEvidences,
  onCompleted,
}: NormativeIndicatorReviewActionsProps) {
  const [justification, setJustification] = useState('');
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const hasOpenObservation = indicatorStatus === 'OBSERVADO';
  const canAct =
    hasEvidences &&
    !hasOpenObservation &&
    (indicatorStatus === 'SUBIDO' || indicatorStatus === 'SUBSANADO' || !indicatorStatus);

  const handleReject = async () => {
    if (justification.trim().length < 20) {
      setError('La justificación debe tener al menos 20 caracteres.');
      return;
    }
    setIsSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      await rejectNormativeIndicator(indicatorId, justification.trim());
      setSuccess(`Indicador «${indicatorLabel}» rechazado. El coordinador fue notificado para subsanar.`);
      setShowRejectForm(false);
      setJustification('');
      onCompleted();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo rechazar el indicador');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleApprove = async () => {
    setIsSubmitting(true);
    setError(null);
    setSuccess(null);
    try {
      await approveNormativeIndicator(indicatorId);
      setSuccess(`Indicador «${indicatorLabel}» aprobado correctamente.`);
      onCompleted();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'No se pudo aprobar el indicador');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mt-4 rounded-lg border border-gray-200 bg-body p-4">
      <p className="mb-2 text-label-md font-semibold uppercase text-gray-700">
        Revisión técnica del indicador
      </p>

      {!hasEvidences && (
        <p className="text-body-md text-gray-600">
          No hay evidencias cargadas. La aprobación o el rechazo requieren al menos una evidencia
          en el indicador.
        </p>
      )}

      {hasOpenObservation && (
        <p className="text-body-md text-warning">
          Hay una observación pendiente de subsanación. Resuélvala antes de aprobar.
        </p>
      )}

      {canAct && (
        <div className="flex flex-wrap gap-2">
          <Button
            type="button"
            variant="secondary"
            disabled={isSubmitting}
            isLoading={isSubmitting}
            onClick={() => {
              setShowRejectForm((prev) => !prev);
              setError(null);
            }}
          >
            <XCircle size={16} aria-hidden />
            Rechazar
          </Button>
          <Button
            type="button"
            disabled={isSubmitting}
            isLoading={isSubmitting}
            onClick={() => void handleApprove()}
          >
            <CheckCircle2 size={16} aria-hidden />
            Aprobar
          </Button>
        </div>
      )}

      {showRejectForm && canAct && (
        <div className="mt-3 space-y-2">
          <textarea
            rows={4}
            value={justification}
            disabled={isSubmitting}
            placeholder="Justificación del rechazo (mínimo 20 caracteres)…"
            className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500"
            onChange={(event) => setJustification(event.target.value)}
          />
          <Button type="button" variant="secondary" disabled={isSubmitting} onClick={() => void handleReject()}>
            Confirmar rechazo
          </Button>
        </div>
      )}

      {error && (
        <p className="mt-2 text-body-md text-danger" role="alert">
          {error}
        </p>
      )}
      {success && (
        <p className="mt-2 text-body-md text-success" role="status">
          {success}
        </p>
      )}
    </div>
  );
}
