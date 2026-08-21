import { useState } from 'react';
import { AlertTriangle, X, FileText, CheckCircle2 } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { Alert } from '../../../components/ui/Alert';

export interface EvidenceItemInfo {
  evidenceId?: string;
  versionNumber?: number;
  contentHash?: string;
  description?: string;
}

interface RejectIndicatorModalProps {
  isOpen: boolean;
  indicatorId: string;
  indicatorCode?: string;
  programId?: string;
  evidences: EvidenceItemInfo[];
  isLoading?: boolean;
  onClose: () => void;
  onSubmit: (justification: string) => void;
}

export function RejectIndicatorModal({
  isOpen,
  indicatorId,
  indicatorCode,
  programId,
  evidences,
  isLoading = false,
  onClose,
  onSubmit,
}: RejectIndicatorModalProps) {
  const [justification, setJustification] = useState('');
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  if (!isOpen) {
    return null;
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (justification.trim().length < 15) {
      setErrorMsg('El comentario u observación debe tener al menos 15 caracteres.');
      return;
    }
    setErrorMsg(null);
    onSubmit(justification.trim());
  };

  return (
    <div className="fixed inset-0 z-[70] flex items-center justify-center p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/60 backdrop-blur-[2px]"
        aria-label="Cerrar modal"
        onClick={isLoading ? undefined : onClose}
        disabled={isLoading}
      />

      <div
        role="dialog"
        aria-modal="true"
        className="relative z-10 w-full max-w-lg rounded-2xl border border-gray-200 bg-white p-6 shadow-2xl space-y-5"
      >
        <button
          type="button"
          onClick={onClose}
          disabled={isLoading}
          className="absolute right-4 top-4 rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition-colors"
          aria-label="Cerrar"
        >
          <X size={18} />
        </button>

        {/* Header */}
        <div className="flex items-start gap-3.5 pr-8">
          <div className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-danger/10 text-danger">
            <AlertTriangle size={22} />
          </div>
          <div>
            <h2 className="text-heading-md font-bold text-primary-900">
              Observar / Rechazar Indicador
            </h2>
            <p className="mt-1 text-body-sm text-gray-600">
              Indicador: <span className="font-semibold text-primary-800">{indicatorCode || indicatorId.substring(0, 8)}</span>
              {programId && <span className="ml-2 text-xs bg-primary-50 text-primary-700 px-2 py-0.5 rounded font-mono">Prog: {programId.substring(0, 8)}</span>}
            </p>
          </div>
        </div>

        {/* Set of evidences associated with this indicator */}
        <div className="bg-gray-50 rounded-xl p-3.5 border border-gray-200/80 space-y-2">
          <span className="text-xs font-semibold uppercase tracking-wider text-gray-500 block">
            Conjunto de Evidencias del Indicador ({evidences.length})
          </span>
          <div className="max-h-36 overflow-y-auto space-y-1.5 pr-1">
            {evidences.length === 0 ? (
              <p className="text-xs text-gray-500 italic">Sin registros de evidencias adjuntos.</p>
            ) : (
              evidences.map((ev, idx) => (
                <div key={ev.evidenceId || idx} className="flex items-center justify-between bg-white px-3 py-1.5 rounded-lg border border-gray-200/60 text-xs">
                  <div className="flex items-center gap-2 truncate pr-2">
                    <FileText size={14} className="text-secondary shrink-0" />
                    <span className="font-medium text-gray-800 truncate">
                      {ev.description || `Evidencia de respaldo #${idx + 1}`}
                    </span>
                  </div>
                  {ev.versionNumber && (
                    <span className="shrink-0 text-[10px] font-mono text-gray-500 bg-gray-100 px-1.5 py-0.5 rounded">
                      v{ev.versionNumber}
                    </span>
                  )}
                </div>
              ))
            )}
          </div>
        </div>

        {errorMsg && (
          <Alert variant="error" title="Error de validación">
            {errorMsg}
          </Alert>
        )}

        {/* Comment Form */}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label htmlFor="modal-justification" className="block text-body-sm font-semibold text-gray-800 mb-1.5">
              Observaciones / Comentario de Rechazo <span className="text-danger">*</span>
            </label>
            <textarea
              id="modal-justification"
              rows={3}
              value={justification}
              onChange={(e) => setJustification(e.target.value)}
              className="w-full rounded-xl border border-gray-300 p-3 text-body-sm text-gray-900 focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20 outline-none transition-all placeholder:text-gray-400 resize-none"
              placeholder="Ingresa el motivo detallado de las observaciones encontradas en el conjunto de evidencias de este indicador..."
              required
            />
          </div>

          <div className="flex items-center justify-end gap-3 pt-2 border-t border-gray-100">
            <Button type="button" variant="ghost" onClick={onClose} disabled={isLoading}>
              Cancelar
            </Button>
            <Button
              type="submit"
              variant="secondary"
              className="bg-danger hover:bg-danger-600 text-white flex items-center gap-1.5"
              isLoading={isLoading}
            >
              <CheckCircle2 size={16} />
              Confirmar Rechazo / Observación
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
}
