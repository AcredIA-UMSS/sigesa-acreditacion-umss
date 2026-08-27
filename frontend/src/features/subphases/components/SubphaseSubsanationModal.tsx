import { useId, useState } from 'react';
import { CheckCircle2, Loader2, RefreshCw, X } from 'lucide-react';
import { createPortal } from 'react-dom';
import { Button } from '../../../components/ui/Button';
import { useLockBodyScroll } from '../../../lib/hooks/useLockBodyScroll';
import { mapUploadError } from '../../evidence/hooks/mapUploadError';
import { subsanateSubphaseEvidence } from '../api/subphaseApi';

const ACCEPTED_EXTENSIONS = '.pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg';
const ACCEPTED_LABEL = 'PDF, Word, Excel o imagen — máx. 50 MB';

export type SubphaseSubsanationModalProps = {
  isOpen: boolean;
  onClose: () => void;
  subphaseId: string;
  subphaseName: string;
  evidenceId: string;
  evidenceFilename: string;
  observationId: string;
  onSubsanated?: () => void;
};

export function SubphaseSubsanationModal({
  isOpen,
  onClose,
  subphaseId,
  subphaseName,
  evidenceId,
  evidenceFilename,
  observationId,
  onSubsanated,
}: SubphaseSubsanationModalProps) {
  const fieldId = useId();
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  useLockBodyScroll(isOpen);

  const handleClose = () => {
    if (isSubmitting) return;
    setErrorMessage(null);
    setSuccessMessage(null);
    onClose();
  };

  const onSubmit = async () => {
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!file) {
      setErrorMessage('Seleccione el archivo corregido.');
      return;
    }
    if (!description.trim()) {
      setErrorMessage('Indique una descripción de la subsanación.');
      return;
    }

    setIsSubmitting(true);
    try {
      await subsanateSubphaseEvidence({
        subphaseId,
        evidenceId,
        observationId,
        description: description.trim(),
        file,
      });
      setSuccessMessage('Evidencia subsanada correctamente.');
      onSubsanated?.();
      setTimeout(() => handleClose(), 1200);
    } catch (err) {
      setErrorMessage(
        mapUploadError(err instanceof Error ? err : new Error('No se pudo subsanar la evidencia')),
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return createPortal(
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-gray-900/50 p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby={`${fieldId}-title`}
    >
      <div className="w-full max-w-lg rounded-xl border border-gray-200 bg-body shadow-lg">
        <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4">
          <div>
            <h2 id={`${fieldId}-title`} className="text-heading-sm text-gray-900">
              Subsanar evidencia
            </h2>
            <p className="text-body-md text-gray-600">
              {subphaseName} · {evidenceFilename}
            </p>
          </div>
          <button
            type="button"
            onClick={handleClose}
            disabled={isSubmitting}
            className="rounded-md p-1 text-gray-500 hover:bg-gray-100"
            aria-label="Cerrar"
          >
            <X size={20} />
          </button>
        </div>

        <div className="space-y-4 px-5 py-4">
          <p className="rounded-md border border-warning/30 bg-warning/10 px-3 py-2 text-body-md text-gray-800">
            Solo puede subsanar <strong>una vez</strong> por observación pendiente del equipo técnico.
            La versión anterior quedará en el historial como metadatos (sin archivo).
          </p>

          <div>
            <label htmlFor={`${fieldId}-description`} className="mb-1 block text-label-md text-gray-700">
              Descripción de la corrección
            </label>
            <textarea
              id={`${fieldId}-description`}
              rows={3}
              value={description}
              disabled={isSubmitting}
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
              onChange={(event) => setDescription(event.target.value)}
            />
          </div>

          <div>
            <label htmlFor={`${fieldId}-file`} className="mb-1 block text-label-md text-gray-700">
              Archivo corregido
            </label>
            <input
              id={`${fieldId}-file`}
              type="file"
              accept={ACCEPTED_EXTENSIONS}
              disabled={isSubmitting}
              className="block w-full text-body-md text-gray-700 file:mr-3 file:rounded-md file:border-0 file:bg-primary-50 file:px-3 file:py-2 file:text-label-md file:font-medium file:text-primary-700"
              onChange={(event) => setFile(event.target.files?.[0] ?? null)}
            />
            <p className="mt-1 text-label-md text-gray-500">{ACCEPTED_LABEL}</p>
          </div>

          {errorMessage && (
            <p className="text-body-md text-danger" role="alert">
              {errorMessage}
            </p>
          )}
          {successMessage && (
            <p className="flex items-center gap-2 text-body-md text-success">
              <CheckCircle2 size={16} aria-hidden />
              {successMessage}
            </p>
          )}
        </div>

        <div className="flex justify-end gap-2 border-t border-gray-200 px-5 py-4">
          <Button type="button" variant="ghost" disabled={isSubmitting} onClick={handleClose}>
            Cancelar
          </Button>
          <Button
            type="button"
            disabled={isSubmitting}
            isLoading={isSubmitting}
            onClick={() => void onSubmit()}
          >
            {isSubmitting ? (
              <>
                <Loader2 size={16} className="animate-spin" aria-hidden />
                Subsanando…
              </>
            ) : (
              <>
                <RefreshCw size={16} aria-hidden />
                Subsanar evidencia
              </>
            )}
          </Button>
        </div>
      </div>
    </div>,
    document.body,
  );
}
