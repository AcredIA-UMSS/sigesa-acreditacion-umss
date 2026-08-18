import { useEffect, useRef, useState } from 'react';
import {
  Bot,
  ChevronDown,
  ChevronUp,
  ClipboardList,
  Loader2,
  MessageSquare,
  Send,
  Trash2,
  User,
} from 'lucide-react';
import type { AssistantDemoScenario, ChatMessage } from '../../../api/model/assistantTypes';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import {
  useEvidenceCopilot,
  type EvidenceAgentAction,
} from '../hooks/useEvidenceCopilot';

export function EvidenceCopilotPanel({ programId }: { programId?: string }) {
  const copilot = useEvidenceCopilot(programId);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [mobileOpen, setMobileOpen] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(true);

  useEffect(() => {
    if (!copilot.isSending && mobileOpen) {
      textareaRef.current?.focus();
    }
  }, [copilot.isSending, mobileOpen]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      void copilot.sendMessage();
    }
  };

  if (copilot.isForbidden) {
    return null;
  }

  if (copilot.isStatusLoading) {
    return (
      <aside className="rounded-xl border border-gray-200 bg-body p-4 shadow-sm">
        <p className="text-body-md text-gray-600">Cargando copiloto…</p>
      </aside>
    );
  }

  if (!copilot.isAssistantEnabled) {
    return null;
  }

  const panelBody = (
    <>
      {copilot.isStatusError && (
        <div className="px-4 pt-4">
          <Alert variant="error">
            No se pudo conectar con el asistente. Verifique el backend.
          </Alert>
        </div>
      )}

      {copilot.errorMessage && (
        <div className="px-4 pt-4">
          <Alert variant="error">{copilot.errorMessage}</Alert>
        </div>
      )}

      <ActionHistoryPanel
        actions={copilot.actionHistory}
        open={historyOpen}
        onToggle={() => setHistoryOpen((value) => !value)}
      />

      <div className="flex min-h-56 flex-1 flex-col overflow-hidden lg:min-h-[280px]">
        <div className="flex items-center justify-between border-b border-gray-100 px-4 py-2">
          <div className="flex items-center gap-2 text-label-md font-medium text-gray-700">
            <MessageSquare size={14} className="text-primary-600" />
            Chat
          </div>
          <Button
            variant="ghost"
            onClick={copilot.clearConversation}
            disabled={
              (copilot.messages.length === 0 &&
                copilot.actionHistory.length === 0) ||
              copilot.isSending
            }
            className="px-2! py-1!"
          >
            <Trash2 size={14} />
          </Button>
        </div>

        <div className="flex-1 space-y-3 overflow-y-auto px-4 py-3">
          {copilot.messages.length === 0 ? (
            <EmptyState
              sampleQuestions={copilot.sampleQuestions}
              onSampleSelect={(question) => copilot.setDraft(question)}
            />
          ) : (
            copilot.messages.map((message) => (
              <MessageBubble key={message.id} message={message} />
            ))
          )}
          {copilot.isSending && <TypingIndicator />}
          <div ref={copilot.messagesEndRef} />
        </div>

        <div className="border-t border-gray-200 p-4">
          <div className="flex flex-col gap-2">
            <textarea
              ref={textareaRef}
              value={copilot.draft}
              onChange={(event) => copilot.setDraft(event.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ej.: ¿Qué evidencias de mi carrera están pendientes de revisión?"
              rows={2}
              disabled={copilot.isSending}
              className="resize-none rounded-lg border border-gray-300 px-3 py-2 text-body-md text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:bg-gray-100"
            />
            <Button
              onClick={() => void copilot.sendMessage()}
              isLoading={copilot.isSending}
              disabled={!copilot.draft.trim() || copilot.isSending}
              className="w-full"
            >
              <Send size={14} />
              Enviar
            </Button>
          </div>
        </div>
      </div>
    </>
  );

  return (
    <>
      <aside className="overflow-hidden rounded-xl border border-primary-200 bg-body shadow-sm xl:hidden">
        <button
          type="button"
          onClick={() => setMobileOpen((open) => !open)}
          className="flex w-full items-center justify-between px-4 py-4 text-left"
        >
          <div>
            <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
              Copiloto documental
            </p>
            <p className="mt-1 text-body-md font-medium text-gray-900">
              Control de evidencias [CC/TD]
            </p>
          </div>
          {mobileOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
        </button>
        {mobileOpen && panelBody}
      </aside>

      <aside className="hidden max-h-[calc(100vh-8rem)] flex-col rounded-xl border border-primary-200 bg-body shadow-sm xl:sticky xl:top-8 xl:flex">
        <header className="border-b border-gray-200 px-4 py-4">
          <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
            Copiloto documental
          </p>
          <h2 className="mt-1 text-heading-sm font-semibold text-gray-900">
            AGENTE DE EVIDENCIAS
          </h2>
          <p className="mt-1 text-body-md text-gray-600">
            Listar pendientes, detalle y completitud vía chat · historial de
            acciones
          </p>
        </header>
        {panelBody}
      </aside>
    </>
  );
}

function ActionHistoryPanel({
  actions,
  open,
  onToggle,
}: {
  actions: EvidenceAgentAction[];
  open: boolean;
  onToggle: () => void;
}) {
  return (
    <section className="border-b border-gray-100 px-4 py-3" aria-label="Historial del agente">
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center justify-between text-left"
      >
        <div className="flex items-center gap-2 text-label-md font-medium text-gray-800">
          <ClipboardList size={14} className="text-primary-600" aria-hidden />
          Historial de acciones
          <span className="rounded-full bg-primary-50 px-2 py-0.5 text-label-md text-primary-700">
            {actions.length}
          </span>
        </div>
        {open ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
      </button>

      {open && (
        <div className="mt-3 max-h-40 space-y-2 overflow-y-auto">
          {actions.length === 0 ? (
            <p className="text-body-md text-gray-500">
              Aún no hay acciones. Cada respuesta del agente se registrará aquí
              (tool, camino y fuentes).
            </p>
          ) : (
            [...actions].reverse().map((action) => (
              <article
                key={action.id}
                className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-body-md text-gray-800"
              >
                <div className="flex items-start justify-between gap-2">
                  <p className="font-medium text-gray-900">{action.summary}</p>
                  <StatusBadge status={action.status} />
                </div>
                <p className="mt-1 text-label-md text-gray-600">
                  {formatTime(action.at)} · Pregunta: «
                  {truncate(action.userPrompt, 60)}»
                </p>
                <p className="mt-1 text-label-md text-gray-600">
                  Tool: {action.toolId ?? '—'} · Camino: {action.path}
                  {action.llmInvoked ? ' · LLM' : ''}
                </p>
                {action.sourceTables.length > 0 && (
                  <p className="mt-0.5 text-label-md text-gray-500">
                    Fuentes: {action.sourceTables.join(', ')}
                  </p>
                )}
              </article>
            ))
          )}
        </div>
      )}
    </section>
  );
}

function StatusBadge({ status }: { status: EvidenceAgentAction['status'] }) {
  const styles =
    status === 'ok'
      ? 'bg-success/10 text-success'
      : status === 'error'
        ? 'bg-danger/10 text-danger'
        : 'bg-warning/20 text-gray-800';
  const label =
    status === 'ok' ? 'OK' : status === 'error' ? 'Error' : 'Fuera de alcance';
  return (
    <span className={`shrink-0 rounded-full px-2 py-0.5 text-label-md font-medium ${styles}`}>
      {label}
    </span>
  );
}

function EmptyState({
  sampleQuestions,
  onSampleSelect,
}: {
  sampleQuestions: AssistantDemoScenario[];
  onSampleSelect: (question: string) => void;
}) {
  const sanitized = sampleQuestions.map((scenario) => ({
    ...scenario,
    sampleQuestion: sanitizeSampleQuestion(scenario.sampleQuestion),
  }));

  return (
    <div className="py-4 text-center">
      <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-primary-50 text-primary-600">
        <Bot size={20} />
      </div>
      <p className="text-body-md text-gray-700">
        Consulte la documentación subida por el coordinador. Solo lectura en
        esta fase.
      </p>
      <ul className="mt-4 space-y-2 text-left">
        {sanitized.map((scenario) => (
          <li key={scenario.number}>
            <button
              type="button"
              onClick={() => onSampleSelect(scenario.sampleQuestion)}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-body-md text-gray-800 transition-colors hover:border-primary-300 hover:bg-primary-50"
            >
              {scenario.sampleQuestion}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}

/** Quita placeholders técnicos residuales (<indicatorId>, UUIDs) en chips demo. */
function sanitizeSampleQuestion(question: string): string {
  return question
    .replaceAll('<indicatorId>', 'seleccionado')
    .replaceAll(
      /\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b/gi,
      'seleccionado',
    )
    .replaceAll(/\s{2,}/g, ' ')
    .trim();
}

function MessageBubble({ message }: { message: ChatMessage }) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-2 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      <div
        className={`flex h-7 w-7 shrink-0 items-center justify-center rounded-full ${
          isUser ? 'bg-primary-600 text-body' : 'bg-secondary-50 text-secondary-600'
        }`}
      >
        {isUser ? <User size={14} /> : <Bot size={14} />}
      </div>
      <div
        className={`max-w-[90%] rounded-xl px-3 py-2 text-body-md leading-relaxed ${
          isUser
            ? 'bg-primary-600 text-body'
            : 'border border-gray-200 bg-gray-50 text-gray-900'
        }`}
      >
        <p className="whitespace-pre-wrap">{message.content}</p>
        {!isUser && message.metadata && (
          <div className="mt-2 border-t border-gray-200 pt-2 text-label-md text-gray-600">
            <p>
              <span className="font-medium">Tool:</span>{' '}
              {message.metadata.toolId ?? '—'}
            </p>
            <p className="mt-0.5">
              <span className="font-medium">Fuentes:</span>{' '}
              {message.metadata.sourceTables.length > 0
                ? message.metadata.sourceTables.join(', ')
                : '—'}
            </p>
            <p className="mt-0.5">
              <span className="font-medium">Camino:</span>{' '}
              {message.metadata.path}
              {message.metadata.llmInvoked ? ' · LLM invocado' : ''}
            </p>
          </div>
        )}
      </div>
    </div>
  );
}

function TypingIndicator() {
  return (
    <div className="flex items-center gap-2 text-body-md text-gray-600">
      <Loader2 size={14} className="animate-spin" />
      Consultando evidencias…
    </div>
  );
}

function formatTime(iso: string): string {
  try {
    return new Date(iso).toLocaleTimeString('es-BO', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    });
  } catch {
    return iso;
  }
}

function truncate(value: string, max: number): string {
  if (value.length <= max) return value;
  return `${value.slice(0, max).trim()}…`;
}
