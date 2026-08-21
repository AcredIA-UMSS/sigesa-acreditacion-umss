import { Activity, X } from 'lucide-react';
import { createPortal } from 'react-dom';
import { Button } from '../../../components/ui/Button';
import { useLockBodyScroll } from '../../../lib/hooks/useLockBodyScroll';
import type { CopilotAgentAction } from '../types/copilotAgentAction';

export interface AssistantCopilotActionDebugModalProps {
  isOpen: boolean;
  isSending: boolean;
  actions: CopilotAgentAction[];
  onClose: () => void;
  title: string;
  titleId: string;
  description?: string;
  showDevBadge?: boolean;
}

export function AssistantCopilotActionDebugModal({
  isOpen,
  isSending,
  actions,
  onClose,
  title,
  titleId,
  description = 'Trazabilidad en tiempo real de tools, camino y fuentes consultadas.',
  showDevBadge = true,
}: AssistantCopilotActionDebugModalProps) {
  useLockBodyScroll(isOpen);

  if (!isOpen || typeof document === 'undefined') {
    return null;
  }

  return createPortal(
    <div className="fixed inset-0 z-80 flex items-center justify-center overflow-hidden p-4">
      <button
        type="button"
        className="absolute inset-0 bg-primary-900/50 backdrop-blur-[2px]"
        aria-label="Cerrar trazabilidad del agente"
        onClick={onClose}
      />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        className="relative z-10 flex max-h-[85vh] min-h-0 w-full max-w-lg flex-col overflow-hidden rounded-xl border border-primary-200 bg-body shadow-xl"
      >
        <header className="flex items-start justify-between gap-3 border-b border-gray-200 px-5 py-4">
          <div>
            {showDevBadge && (
              <div className="flex items-center gap-2 text-label-md font-medium uppercase tracking-wide text-primary-600">
                <Activity size={14} aria-hidden />
                Modo desarrollo
              </div>
            )}
            <h2
              id={titleId}
              className={`${showDevBadge ? 'mt-1' : ''} text-heading-sm font-semibold text-gray-900`}
            >
              {title}
            </h2>
            <p className="mt-1 text-body-md text-gray-600">{description}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="rounded-lg p-1 text-gray-500 transition-colors hover:bg-gray-100 hover:text-gray-800"
            aria-label="Cerrar"
          >
            <X size={18} />
          </button>
        </header>

        <div className="min-h-0 flex-1 space-y-3 overflow-y-auto overscroll-contain px-5 py-4">
          {actions.length === 0 ? (
            <p className="text-body-md text-gray-600">
              Envíe un mensaje al copiloto para registrar la primera acción.
            </p>
          ) : (
            [...actions].reverse().map((action) => (
              <article
                key={action.id}
                className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-body-md text-gray-800"
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium text-gray-900">{action.summary}</p>
                  <StatusBadge status={action.status} />
                </div>
                <p className="mt-1 text-label-md text-gray-600">
                  {formatTime(action.at)} · «{truncate(action.userPrompt, 72)}»
                </p>
                <ul className="mt-2 space-y-1 text-label-md text-gray-600">
                  {action.steps.map((step) => (
                    <li key={step.id} className="flex items-start gap-2">
                      <span
                        className={`mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full ${
                          step.kind === 'error'
                            ? 'bg-danger'
                            : step.kind === 'pending'
                              ? 'bg-warning'
                              : 'bg-success'
                        }`}
                        aria-hidden
                      />
                      <span>{step.label}</span>
                    </li>
                  ))}
                </ul>
                {action.toolId && (
                  <p className="mt-2 text-label-md text-gray-600">
                    Tool: {action.toolId} · Camino: {action.path}
                    {action.llmInvoked ? ' · LLM' : ''}
                  </p>
                )}
                {action.sourceTables.length > 0 && (
                  <p className="mt-0.5 text-label-md text-gray-500">
                    Fuentes: {action.sourceTables.join(', ')}
                  </p>
                )}
              </article>
            ))
          )}
          {isSending && (
            <p className="text-body-md text-primary-600">Procesando solicitud…</p>
          )}
        </div>

        <footer className="border-t border-gray-200 px-5 py-4">
          <Button variant="ghost" onClick={onClose} className="w-full">
            Cerrar
          </Button>
        </footer>
      </div>
    </div>,
    document.body,
  );
}

function StatusBadge({ status }: { status: CopilotAgentAction['status'] }) {
  const styles =
    status === 'ok'
      ? 'bg-success/10 text-success'
      : status === 'error'
        ? 'bg-danger/10 text-danger'
        : status === 'pending'
          ? 'bg-warning/20 text-gray-800'
          : 'bg-warning/20 text-gray-800';
  const label =
    status === 'ok'
      ? 'OK'
      : status === 'error'
        ? 'Error'
        : status === 'pending'
          ? 'En curso'
          : 'Fuera de alcance';
  return (
    <span className={`shrink-0 rounded-full px-2 py-0.5 text-label-md font-medium ${styles}`}>
      {label}
    </span>
  );
}

function formatTime(iso: string): string {
  return new Date(iso).toLocaleTimeString('es-BO', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  });
}

function truncate(value: string, max: number): string {
  if (value.length <= max) return value;
  return `${value.slice(0, max).trim()}…`;
}
