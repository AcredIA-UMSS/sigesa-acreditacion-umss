import { AlertTriangle, X } from 'lucide-react';
import { Button } from './Button';

interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  description: string;
  confirmLabel?: string;
  cancelLabel?: string;
  confirmVariant?: 'primary' | 'danger';
  isLoading?: boolean;
  onClose: () => void;
  onConfirm: () => void;
}

export function ConfirmDialog({
  isOpen,
  title,
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  confirmVariant = 'danger',
  isLoading = false,
  onClose,
  onConfirm,
}: ConfirmDialogProps) {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar diálogo"
        onClick={isLoading ? undefined : onClose}
        disabled={isLoading}
      />

      <div
        role="alertdialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        aria-describedby="confirm-dialog-description"
        className="relative z-10 w-full max-w-md rounded-2xl border border-gray-200 bg-body p-6 shadow-2xl"
      >
        <button
          type="button"
          onClick={onClose}
          disabled={isLoading}
          className="absolute right-4 top-4 rounded-lg p-2 text-gray-500 transition-colors hover:bg-gray-100 hover:text-primary-700 disabled:cursor-not-allowed"
          aria-label="Cerrar"
        >
          <X size={18} />
        </button>

        <div className="flex items-start gap-4 pr-8">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-danger/10 text-danger">
            <AlertTriangle size={22} />
          </div>
          <div>
            <h2 id="confirm-dialog-title" className="text-heading-md font-semibold text-primary-800">
              {title}
            </h2>
            <p id="confirm-dialog-description" className="mt-2 text-body-md text-gray-600">
              {description}
            </p>
          </div>
        </div>

        <div className="mt-6 flex justify-end gap-3">
          <Button variant="ghost" onClick={onClose} disabled={isLoading}>
            {cancelLabel}
          </Button>
          <Button variant={confirmVariant} onClick={onConfirm} isLoading={isLoading}>
            {confirmLabel}
          </Button>
        </div>
      </div>
    </div>
  );
}
