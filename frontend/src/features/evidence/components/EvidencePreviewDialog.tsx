import { useEffect } from 'react';
import { FileText, X } from 'lucide-react';
import { Button } from '../../../components/ui/Button';

export type EvidencePreviewDialogProps = {
  isOpen: boolean;
  title: string;
  objectUrl: string | null;
  contentType: string | null;
  isLoading: boolean;
  error: string | null;
  onClose: () => void;
  onDownload: () => void;
};

function canPreview(contentType: string | null, title: string): boolean {
  if (contentType?.includes('pdf') || title.toLowerCase().endsWith('.pdf')) {
    return true;
  }
  if (contentType?.startsWith('image/')) {
    return true;
  }
  return /\.(png|jpe?g)$/i.test(title);
}

export function EvidencePreviewDialog({
  isOpen,
  title,
  objectUrl,
  contentType,
  isLoading,
  error,
  onClose,
  onDownload,
}: EvidencePreviewDialogProps) {
  useEffect(() => {
    if (!isOpen) {
      return undefined;
    }
    const onKey = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };
    window.addEventListener('keydown', onKey);
    return () => window.removeEventListener('keydown', onKey);
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  const previewable = Boolean(objectUrl) && canPreview(contentType, title);
  const isImage = contentType?.startsWith('image/') || /\.(png|jpe?g)$/i.test(title);

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar visor de evidencia"
        onClick={onClose}
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="evidence-preview-title"
        className="relative z-10 flex max-h-[90vh] w-full max-w-4xl flex-col rounded-2xl border border-gray-200 bg-body shadow-2xl"
      >
        <div className="flex items-start justify-between gap-4 border-b border-gray-200 px-6 py-4">
          <div className="flex min-w-0 items-start gap-3">
            <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-primary-50 text-primary-700">
              <FileText size={22} aria-hidden />
            </div>
            <h2
              id="evidence-preview-title"
              className="truncate text-heading-md font-semibold text-primary-800"
            >
              {title}
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-primary-700"
            aria-label="Cerrar"
          >
            <X size={18} />
          </button>
        </div>

        <div className="min-h-64 flex-1 overflow-auto p-4">
          {isLoading && <p className="text-body-md text-gray-500">Cargando evidencia…</p>}
          {error && (
            <p className="text-body-md text-danger" role="alert">
              {error}
            </p>
          )}
          {!isLoading && !error && previewable && objectUrl && isImage && (
            <img src={objectUrl} alt={title} className="mx-auto max-h-[70vh] max-w-full rounded-md" />
          )}
          {!isLoading && !error && previewable && objectUrl && !isImage && (
            <iframe
              title={title}
              src={objectUrl}
              className="h-[70vh] w-full rounded-md border border-gray-200"
            />
          )}
          {!isLoading && !error && !previewable && (
            <p className="text-body-md text-gray-600">
              Este tipo de archivo no se previsualiza en el navegador. Descárguelo para abrirlo.
            </p>
          )}
        </div>

        <div className="flex justify-end gap-3 border-t border-gray-200 px-6 py-4">
          <Button variant="ghost" onClick={onClose}>
            Cerrar
          </Button>
          <Button variant="primary" onClick={onDownload} disabled={!objectUrl || isLoading}>
            Descargar
          </Button>
        </div>
      </div>
    </div>
  );
}
