import { X } from 'lucide-react';
import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { isApiError } from '../../../lib/api/apiError';
import { useProcessObservations } from '../hooks/useProcessObservations';

interface IndicatorObservationModalProps {
  isOpen: boolean;
  processId: string;
  indicatorId: string;
  indicatorLabel: string;
  evidenceId: string | null;
  canWrite: boolean;
  onClose: () => void;
}

function formatWhen(value: string): string {
  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return value;
  }
  return parsed.toLocaleString('es-BO', { dateStyle: 'short', timeStyle: 'short' });
}

export function IndicatorObservationModal({
  isOpen,
  processId,
  indicatorId,
  indicatorLabel,
  evidenceId,
  canWrite,
  onClose,
}: IndicatorObservationModalProps) {
  const { list, create, act } = useProcessObservations(processId, indicatorId);
  const [description, setDescription] = useState('');
  const [recommendation, setRecommendation] = useState('');
  const [formError, setFormError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const isSaving = create.isPending || act.isPending;

  if (!isOpen) {
    return null;
  }

  const history = [...(list.data ?? [])].sort((a, b) => b.createdAt.localeCompare(a.createdAt));

  async function submitObservation() {
    setFormError(null);
    setNotice(null);
    if (!evidenceId) {
      setFormError('No hay documentación entregada en este indicador.');
      return;
    }
    if (description.trim().length < 10) {
      setFormError('La observación debe tener al menos 10 caracteres.');
      return;
    }
    if (!recommendation.trim()) {
      setFormError('La recomendación es obligatoria.');
      return;
    }
    try {
      const saved = await create.mutateAsync({
        evidenceId,
        description: description.trim(),
        kind: 'CONTENIDO',
        priority: 'MEDIA',
        compliance: 'NO_CUMPLE',
        recommendation: recommendation.trim(),
      });
      await act.mutateAsync({ observationId: saved.id, action: 'submit' });
      setDescription('');
      setRecommendation('');
      setNotice('Observación enviada. El coordinador fue notificado.');
    } catch (error) {
      setFormError(isApiError(error) ? error.message : 'No se pudo guardar la observación.');
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar modal"
        onClick={onClose}
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="indicator-observation-title"
        className="relative z-10 flex max-h-[90vh] w-full max-w-lg flex-col rounded-2xl border border-gray-200 bg-body shadow-2xl"
      >
        <div className="flex items-center justify-between border-b border-gray-100 px-6 py-4">
          <h2 id="indicator-observation-title" className="text-heading-md font-semibold text-primary-800">
            Observaciones del técnico
          </h2>
          <button
            type="button"
            className="rounded-lg p-2 text-gray-500 hover:bg-gray-100 hover:text-primary-700"
            aria-label="Cerrar"
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-4 overflow-y-auto px-6 py-5">
          <p className="text-body-md text-gray-600">
            Indicador <span className="font-medium text-primary-800">{indicatorLabel}</span>. Cada envío
            queda en el historial y notifica al coordinador.
          </p>

          {list.isError && (
            <p className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
              No se pudo cargar el historial.
            </p>
          )}

          <div>
            <h3 className="text-heading-sm font-semibold text-primary-800">Historial</h3>
            {list.isPending ? (
              <p className="mt-2 text-body-md text-gray-600">Cargando historial…</p>
            ) : history.length === 0 ? (
              <p className="mt-2 text-body-md text-gray-600">Aún no hay observaciones en este indicador.</p>
            ) : (
              <ol className="mt-2 max-h-48 space-y-2 overflow-y-auto">
                {history.map((item) => (
                  <li key={item.id} className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-3">
                    <p className="text-label-md text-gray-500">
                      {formatWhen(item.createdAt)} · {item.status}
                    </p>
                    <p className="mt-1 text-body-md text-primary-900">{item.description}</p>
                    {item.recommendation && (
                      <p className="mt-1 text-body-md text-gray-700">Recomendación: {item.recommendation}</p>
                    )}
                    {item.messages.length > 0 && (
                      <ul className="mt-2 space-y-1 border-t border-gray-200 pt-2">
                        {item.messages.map((message) => (
                          <li key={message.id} className="text-body-md text-gray-700">
                            <span className="font-medium">{message.authorRole}</span>: {message.body}
                          </li>
                        ))}
                      </ul>
                    )}
                  </li>
                ))}
              </ol>
            )}
          </div>

          {canWrite && (
            <div className="space-y-3">
              <label className="block text-label-md text-gray-700" htmlFor="td-observation-body">
                Nueva observación
                <span className="ml-0.5 text-secondary">*</span>
              </label>
              <textarea
                id="td-observation-body"
                rows={4}
                value={description}
                disabled={isSaving}
                className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500"
                placeholder="Describa el hallazgo (mínimo 10 caracteres)…"
                onChange={(event) => setDescription(event.target.value)}
              />
              <label className="block text-label-md text-gray-700" htmlFor="td-observation-rec">
                Recomendación
                <span className="ml-0.5 text-secondary">*</span>
              </label>
              <textarea
                id="td-observation-rec"
                rows={3}
                value={recommendation}
                disabled={isSaving}
                className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500"
                placeholder="Qué debe corregir el coordinador…"
                onChange={(event) => setRecommendation(event.target.value)}
              />
            </div>
          )}

          {formError && (
            <p className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
              {formError}
            </p>
          )}
          {notice && (
            <p className="rounded-lg border border-success/30 bg-success/10 px-4 py-3 text-body-md text-success">
              {notice}
            </p>
          )}
        </div>

        <div className="flex justify-end gap-3 border-t border-gray-100 px-6 py-4">
          <Button variant="ghost" onClick={onClose} disabled={isSaving}>
            Cerrar
          </Button>
          {canWrite && (
            <Button onClick={() => void submitObservation()} isLoading={isSaving} disabled={!evidenceId}>
              Enviar observación
            </Button>
          )}
        </div>
      </div>
    </div>
  );
}
