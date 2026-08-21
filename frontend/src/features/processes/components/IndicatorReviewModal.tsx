import { X, FileText, CheckCircle2, Check, Layers, ShieldCheck } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import type { EvidenceItemInfo } from './RejectIndicatorModal';

export interface IndicatorReviewGroup {
  indicatorId: string;
  programId: string;
  criterionId: string;
  currentState: string;
  evidences: EvidenceItemInfo[];
}

interface IndicatorReviewModalProps {
  isOpen: boolean;
  indicator: IndicatorReviewGroup | null;
  isLoadingApprove?: boolean;
  isLoadingReject?: boolean;
  onClose: () => void;
  onApprove: (indicatorId: string) => void;
  onOpenRejectModal: (indicator: IndicatorReviewGroup) => void;
}

export function IndicatorReviewModal({
  isOpen,
  indicator,
  isLoadingApprove = false,
  isLoadingReject = false,
  onClose,
  onApprove,
  onOpenRejectModal,
}: IndicatorReviewModalProps) {
  if (!isOpen || !indicator) {
    return null;
  }

  const isPendingAction = isLoadingApprove || isLoadingReject;

  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4">
      {/* Backdrop */}
      <button
        type="button"
        className="absolute inset-0 bg-primary-950/60 backdrop-blur-[3px]"
        aria-label="Cerrar modal"
        onClick={isPendingAction ? undefined : onClose}
        disabled={isPendingAction}
      />

      {/* Modal Content */}
      <div
        role="dialog"
        aria-modal="true"
        className="relative z-10 w-full max-w-2xl rounded-2xl border border-gray-200 bg-white p-6 shadow-2xl space-y-6 max-h-[90vh] flex flex-col"
      >
        {/* Close Button */}
        <button
          type="button"
          onClick={onClose}
          disabled={isPendingAction}
          className="absolute right-4 top-4 rounded-lg p-2 text-gray-400 hover:bg-gray-100 hover:text-gray-700 transition-colors"
          aria-label="Cerrar"
        >
          <X size={20} />
        </button>

        {/* Modal Header */}
        <div className="flex items-start gap-4 pr-8 border-b border-gray-100 pb-4">
          <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-primary-50 text-primary-600">
            <Layers size={24} />
          </div>
          <div>
            <div className="flex items-center gap-2">
              <h2 className="text-heading-md font-bold text-primary-900">
                Detalle del Indicador: {indicator.indicatorId.substring(0, 8)}...
              </h2>
              <span
                className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                  indicator.currentState === 'SUBSANADO'
                    ? 'bg-warning/10 text-warning'
                    : indicator.currentState === 'APROBADO'
                    ? 'bg-success/10 text-success'
                    : 'bg-info/10 text-info'
                }`}
              >
                {indicator.currentState}
              </span>
            </div>
            <div className="mt-1 flex flex-wrap gap-2 text-xs text-gray-600">
              <span className="bg-gray-100 px-2 py-0.5 rounded font-mono text-gray-700">
                Programa: {indicator.programId.substring(0, 8)}
              </span>
              <span className="bg-gray-100 px-2 py-0.5 rounded font-mono text-gray-700">
                Criterio: {indicator.criterionId.substring(0, 8)}
              </span>
            </div>
          </div>
        </div>

        {/* Evidences List Container */}
        <div className="flex-1 overflow-y-auto space-y-4 pr-1">
          <div className="flex items-center justify-between">
            <h3 className="text-label-md font-bold uppercase tracking-wider text-primary-800 flex items-center gap-2">
              <FileText size={16} className="text-secondary" />
              Conjunto de Evidencias de Respaldo ({indicator.evidences.length})
            </h3>
            <span className="text-xs text-gray-500">
              Verifique todos los archivos antes de dictaminar
            </span>
          </div>

          {indicator.evidences.length === 0 ? (
            <div className="rounded-xl border border-dashed border-gray-300 p-8 text-center bg-gray-50">
              <p className="text-body-sm text-gray-500 italic">No se han registrado evidencias para este indicador.</p>
            </div>
          ) : (
            <div className="space-y-3">
              {indicator.evidences.map((ev, idx) => (
                <div
                  key={ev.evidenceId || idx}
                  className="rounded-xl border border-gray-200 bg-gray-50/60 p-4 hover:bg-white hover:shadow-sm transition-all space-y-2"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-start gap-3">
                      <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white border border-gray-200 text-secondary">
                        <FileText size={18} />
                      </div>
                      <div>
                        <h4 className="text-body-sm font-bold text-gray-900">
                          {ev.description || `Evidencia Documental #${idx + 1}`}
                        </h4>
                        <p className="text-xs text-gray-500 mt-0.5">
                          ID Evidencia: <span className="font-mono text-gray-700">{ev.evidenceId ? ev.evidenceId.substring(0, 8) : '—'}</span>
                        </p>
                      </div>
                    </div>
                    {ev.versionNumber && (
                      <span className="shrink-0 text-xs font-mono font-semibold text-primary-700 bg-primary-50 px-2 py-0.5 rounded border border-primary-100">
                        v{ev.versionNumber}
                      </span>
                    )}
                  </div>

                  {ev.contentHash && (
                    <div className="flex items-center gap-2 pt-2 border-t border-gray-200/60 text-[11px] text-gray-500 font-mono">
                      <ShieldCheck size={13} className="text-success shrink-0" />
                      <span className="truncate">Hash (SHA-256): {ev.contentHash}</span>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Guidance Box */}
        <div className="rounded-xl bg-primary-50/60 border border-primary-100 p-3.5 flex items-start gap-3 text-xs text-primary-800">
          <CheckCircle2 size={18} className="text-primary-600 shrink-0 mt-0.5" />
          <div>
            <span className="font-bold block text-primary-900">Dictamen sobre el Indicador:</span>
            Si todas las evidencias del indicador están verificadas y conformes, presione <strong>Aprobar Indicador</strong>. De lo contrario, presione <strong>Observar / Rechazar Indicador</strong> para enviar observaciones al coordinador.
          </div>
        </div>

        {/* Action Footer */}
        <div className="flex items-center justify-end gap-3 pt-3 border-t border-gray-100">
          <Button variant="ghost" onClick={onClose} disabled={isPendingAction}>
            Cerrar
          </Button>
          <Button
            variant="secondary"
            className="bg-danger hover:bg-danger-600 text-white flex items-center gap-1.5"
            onClick={() => onOpenRejectModal(indicator)}
            disabled={isPendingAction}
          >
            <X size={16} />
            Observar / Rechazar Indicador
          </Button>
          <Button
            variant="primary"
            className="bg-success hover:bg-success-600 text-white flex items-center gap-1.5"
            onClick={() => onApprove(indicator.indicatorId)}
            isLoading={isLoadingApprove}
            disabled={isPendingAction}
          >
            <Check size={16} />
            Aprobar Indicador
          </Button>
        </div>
      </div>
    </div>
  );
}
