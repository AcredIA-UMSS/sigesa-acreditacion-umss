import { useId, useState } from 'react';
import { CheckCircle2, Loader2, Upload, X } from 'lucide-react';
import { createPortal } from 'react-dom';
import { Button } from '../../../components/ui/Button';
import { useLockBodyScroll } from '../../../lib/hooks/useLockBodyScroll';
import { mapUploadError } from '../hooks/mapUploadError';
import { uploadNormativeIndicatorEvidence } from '../api/normativeIndicatorEvidenceApi';

const ACCEPTED_EXTENSIONS = '.pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg';
const ACCEPTED_LABEL = 'PDF, Word, Excel, imagen o enlace externo — máx. 50 MB';

export type NormativeIndicatorEvidenceUploadModalProps = {
  isOpen: boolean;
  onClose: () => void;
  indicatorId: string;
  indicatorLabel: string;
  breadcrumb: string;
  canUpload: boolean;
  onUploaded?: () => void;
};

export function NormativeIndicatorEvidenceUploadModal({
  isOpen,
  onClose,
  indicatorId,
  indicatorLabel,
  breadcrumb,
  canUpload,
  onUploaded,
}: NormativeIndicatorEvidenceUploadModalProps) {
  const fieldId = useId();
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [externalUrl, setExternalUrl] = useState('');
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

    if (!canUpload) {
      setErrorMessage('La carga está reservada al coordinador [CC].');
      return;
    }

    const trimmedUrl = externalUrl.trim();
    if (!file && !trimmedUrl) {
      setErrorMessage('Seleccione un archivo o indique un enlace externo.');
      return;
    }
    if (!description.trim()) {
      setErrorMessage('Indique una descripción de la evidencia.');
      return;
    }

    setIsSubmitting(true);
    try {
      const result = await uploadNormativeIndicatorEvidence({
        indicatorId,
        description: description.trim(),
        file: file ?? undefined,
        externalUrl: trimmedUrl || undefined,
      });
      setSuccessMessage(`Evidencia cargada (v${result.version}). Estado: ${result.currentState}.`);
      setFile(null);
      setExternalUrl('');
      setDescription('');
      onUploaded?.();
    } catch (err) {
      setErrorMessage(
        mapUploadError(err instanceof Error ? err : new Error('Error al cargar')),
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen || typeof document === 'undefined') {
    return null;
  }

  return createPortal(
    <div className="fixed inset-0 z-80 flex items-center justify-center overflow-hidden p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar modal de carga"
        onClick={handleClose}
        disabled={isSubmitting}
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={`${fieldId}-title`}
        className="relative z-10 flex max-h-[90vh] min-h-0 w-full max-w-lg flex-col overflow-hidden rounded-xl border border-primary-200 bg-body shadow-xl"
      >
        <header className="flex items-start justify-between gap-3 border-b border-gray-200 px-5 py-4">
          <div>
            <h2 id={`${fieldId}-title`} className="text-heading-sm font-semibold text-gray-900">
              Cargar evidencia
            </h2>
            <p className="mt-1 text-body-md text-gray-600">Indicador «{indicatorLabel}»</p>
            <p className="mt-0.5 text-label-md text-gray-500">{breadcrumb}</p>
            <p className="mt-0.5 text-label-md text-gray-500">{ACCEPTED_LABEL}</p>
          </div>
          <button
            type="button"
            onClick={handleClose}
            disabled={isSubmitting}
            className="rounded-lg p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-800 disabled:cursor-not-allowed"
            aria-label="Cerrar"
          >
            <X size={18} />
          </button>
        </header>

        <div className="min-h-0 flex-1 space-y-4 overflow-y-auto overscroll-contain px-5 py-4">
          <div>
            <label
              htmlFor={`${fieldId}-description`}
              className="mb-1 block text-label-md text-gray-700"
            >
              Descripción <span className="text-secondary">*</span>
            </label>
            <textarea
              id={`${fieldId}-description`}
              rows={3}
              value={description}
              disabled={isSubmitting}
              placeholder={`Ej.: Documento de respaldo para «${indicatorLabel}»`}
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
              onChange={(event) => setDescription(event.target.value)}
            />
          </div>

          <div>
            <label htmlFor={`${fieldId}-external-url`} className="mb-1 block text-label-md text-gray-700">
              Enlace externo (opcional)
            </label>
            <input
              id={`${fieldId}-external-url`}
              type="url"
              value={externalUrl}
              disabled={isSubmitting}
              placeholder="https://..."
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
              onChange={(event) => setExternalUrl(event.target.value)}
            />
          </div>

          <label
            htmlFor={`${fieldId}-file`}
            className={`flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-4 py-8 transition-colors ${
              isSubmitting
                ? 'cursor-not-allowed border-gray-200 bg-gray-100 opacity-60'
                : file
                  ? 'border-primary-400 bg-body'
                  : 'border-gray-300 bg-gray-50 hover:border-primary-400 hover:bg-primary-50'
            }`}
          >
            <Upload size={28} className="mb-2 text-primary-600" aria-hidden />
            <span className="text-body-md font-medium text-primary-800">
              {file ? file.name : 'Seleccionar archivo (opcional si hay enlace)'}
            </span>
            <input
              id={`${fieldId}-file`}
              type="file"
              accept={ACCEPTED_EXTENSIONS}
              disabled={isSubmitting}
              className="sr-only"
              onChange={(event) => {
                setFile(event.target.files?.[0] ?? null);
                setErrorMessage(null);
                setSuccessMessage(null);
              }}
            />
          </label>

          {errorMessage && (
            <p className="text-body-md text-danger" role="alert">
              {errorMessage}
            </p>
          )}
          {successMessage && (
            <p className="flex items-center gap-1 text-body-md text-success" role="status">
              <CheckCircle2 size={16} aria-hidden />
              {successMessage}
            </p>
          )}
        </div>

        <footer className="flex gap-3 border-t border-gray-200 px-5 py-4">
          <Button variant="ghost" onClick={handleClose} disabled={isSubmitting} className="flex-1">
            Cancelar
          </Button>
          <Button
            type="button"
            onClick={() => void onSubmit()}
            disabled={isSubmitting}
            isLoading={isSubmitting}
            className="flex-1"
          >
            {isSubmitting ? (
              <Loader2 size={16} className="animate-spin" />
            ) : (
              <Upload size={16} />
            )}
            Subir evidencia
          </Button>
        </footer>
      </div>
    </div>,
    document.body,
  );
}
