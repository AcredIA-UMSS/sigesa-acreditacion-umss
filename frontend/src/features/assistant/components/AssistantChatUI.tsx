import { useEffect, useRef } from 'react';
import { Bot, Loader2, MessageSquare, Send, Trash2, User } from 'lucide-react';
import type {
  AssistantDemoScenario,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';

export type AssistantChatUIProps = {
  messages: ChatMessage[];
  draft: string;
  onDraftChange: (value: string) => void;
  onSend: () => void;
  onClear: () => void;
  onSampleSelect: (question: string) => void;
  model: string;
  llmEnabled: boolean;
  capabilities: string[];
  demoScenarios: AssistantDemoScenario[];
  isAssistantEnabled: boolean;
  isStatusError: boolean;
  isStatusLoading: boolean;
  isSending: boolean;
  errorMessage: string | null;
  messagesEndRef: React.RefObject<HTMLDivElement | null>;
};

export function AssistantChatUI({
  messages,
  draft,
  onDraftChange,
  onSend,
  onClear,
  onSampleSelect,
  model,
  llmEnabled,
  capabilities,
  demoScenarios,
  isAssistantEnabled,
  isStatusError,
  isStatusLoading,
  isSending,
  errorMessage,
  messagesEndRef,
}: AssistantChatUIProps) {
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    if (!isSending) {
      textareaRef.current?.focus();
    }
  }, [isSending]);

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      onSend();
    }
  };

  return (
    <main className="flex flex-1 flex-col overflow-hidden bg-gray-50">
      <header className="border-b border-gray-200 bg-body px-8 py-6">
        <div className="flex items-start justify-between gap-4">
          <div>
            <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
              SIGESA · Ayuda
            </p>
            <h1 className="mt-1 text-heading-lg font-semibold text-gray-900">
              Asistente virtual
            </h1>
            <p className="mt-2 max-w-2xl text-body-md text-gray-600">
              Tool calling SIGESA: la respuesta la produce el código. El LLM solo elige la
              herramienta cuando la pregunta no coincide con el catálogo de palabras clave.
            </p>
          </div>
          <div className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-right">
            <p className="text-label-md text-gray-500">Modelo (fijado)</p>
            <p className="text-body-md font-medium text-gray-900">
              {isStatusLoading ? 'Cargando…' : model}
            </p>
            <p className="mt-2 text-label-md text-gray-500">IA (LLM)</p>
            <p className="text-body-md font-medium text-gray-900">
              {isStatusLoading ? '—' : llmEnabled ? 'Encendida' : 'Apagada'}
            </p>
          </div>
        </div>
      </header>

      <div className="flex flex-1 gap-6 overflow-hidden px-8 py-6">
        <aside className="hidden w-80 shrink-0 flex-col gap-4 overflow-y-auto lg:flex">
          <DemoPanel demoScenarios={demoScenarios} onSampleSelect={onSampleSelect} />
          <CapabilitiesPanel capabilities={capabilities} />
        </aside>

        <div className="flex min-w-0 flex-1 flex-col overflow-hidden">
          {!isStatusLoading && isStatusError && (
            <div className="mb-4">
              <Alert variant="error">
                No se pudo consultar el estado del asistente. Verifique que el backend esté activo e
                inicie sesión nuevamente.
              </Alert>
            </div>
          )}

          {!isStatusLoading && !isStatusError && !isAssistantEnabled && (
            <div className="mb-4">
              <Alert variant="warning">
                El asistente está deshabilitado en el servidor. Active{' '}
                <code className="text-code">SIGESA_ASSISTANT_ENABLED=true</code>.
              </Alert>
            </div>
          )}

          {errorMessage && (
            <div className="mb-4">
              <Alert variant="error">{errorMessage}</Alert>
            </div>
          )}

          <section className="flex min-h-0 flex-1 flex-col rounded-xl border border-gray-200 bg-body shadow-sm">
            <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4">
              <div className="flex items-center gap-2 text-body-md font-medium text-gray-800">
                <MessageSquare size={18} className="text-primary-600" />
                Conversación
              </div>
              <Button
                variant="ghost"
                onClick={onClear}
                disabled={messages.length === 0 || isSending}
                className="!px-3 !py-2"
              >
                <Trash2 size={16} />
                Limpiar
              </Button>
            </div>

            <div className="flex-1 space-y-4 overflow-y-auto px-5 py-6">
              {messages.length === 0 ? (
                <EmptyState demoScenarios={demoScenarios} onSampleSelect={onSampleSelect} />
              ) : (
                messages.map((message) => <MessageBubble key={message.id} message={message} />)
              )}
              {isSending && <TypingIndicator />}
              <div ref={messagesEndRef} />
            </div>

            <div className="border-t border-gray-200 px-5 py-4">
              <div className="flex gap-3">
                <textarea
                  ref={textareaRef}
                  value={draft}
                  onChange={(event) => onDraftChange(event.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Escriba su consulta… (Enter para enviar, Shift+Enter para nueva línea)"
                  rows={3}
                  disabled={isSending}
                  className="min-h-[88px] flex-1 resize-none rounded-lg border border-gray-300 px-4 py-3 text-body-md text-gray-900 outline-none transition-colors placeholder:text-gray-400 focus:border-primary-500 focus:ring-2 focus:ring-primary-100 disabled:bg-gray-100"
                />
                <Button
                  onClick={onSend}
                  isLoading={isSending}
                  disabled={!draft.trim() || isSending}
                  className="self-end"
                >
                  <Send size={16} />
                  Enviar
                </Button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </main>
  );
}

function DemoPanel({
  demoScenarios,
  onSampleSelect,
}: {
  demoScenarios: AssistantDemoScenario[];
  onSampleSelect: (question: string) => void;
}) {
  return (
    <div className="rounded-xl border border-gray-200 bg-body p-4 shadow-sm">
      <h2 className="text-heading-sm font-semibold text-gray-900">Escenarios demo</h2>
      <ul className="mt-3 space-y-3">
        {demoScenarios.map((scenario) => (
          <li key={scenario.number} className="rounded-lg border border-gray-200 bg-gray-50 p-3">
            <p className="text-label-md font-medium text-primary-600">
              {scenario.number}. {scenario.title}
            </p>
            <p className="mt-1 text-body-md text-gray-700">{scenario.sampleQuestion}</p>
            <p className="mt-2 text-label-md text-gray-500">Camino esperado: {scenario.expectedPath}</p>
            <Button
              variant="ghost"
              className="mt-2 !px-2 !py-1"
              onClick={() => onSampleSelect(scenario.sampleQuestion)}
            >
              Usar pregunta
            </Button>
          </li>
        ))}
      </ul>
    </div>
  );
}

function CapabilitiesPanel({ capabilities }: { capabilities: string[] }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-body p-4 shadow-sm">
      <h2 className="text-heading-sm font-semibold text-gray-900">Capacidades</h2>
      <ul className="mt-3 list-disc space-y-2 pl-5 text-body-md text-gray-700">
        {capabilities.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </div>
  );
}

function EmptyState({
  demoScenarios,
  onSampleSelect,
}: {
  demoScenarios: AssistantDemoScenario[];
  onSampleSelect: (question: string) => void;
}) {
  return (
    <div className="flex h-full min-h-[240px] flex-col items-center justify-center text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-primary-50 text-primary-600">
        <Bot size={28} />
      </div>
      <h2 className="text-heading-sm font-semibold text-gray-900">
        Demostración tool calling
      </h2>
      <p className="mt-2 max-w-md text-body-md text-gray-600">
        Pruebe los cuatro escenarios de la tarea. Cada respuesta muestra herramienta, tablas fuente
        y camino (KEYWORD / LLM / OUT_OF_SCOPE).
      </p>
      <div className="mt-6 grid w-full max-w-xl gap-2 text-left">
        {demoScenarios.slice(0, 2).map((scenario) => (
          <button
            key={scenario.number}
            type="button"
            onClick={() => onSampleSelect(scenario.sampleQuestion)}
            className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-body-md text-gray-800 transition-colors hover:border-primary-300 hover:bg-primary-50"
          >
            {scenario.number}. {scenario.sampleQuestion}
          </button>
        ))}
      </div>
    </div>
  );
}

function MessageBubble({ message }: { message: ChatMessage }) {
  const isUser = message.role === 'user';

  return (
    <div className={`flex gap-3 ${isUser ? 'flex-row-reverse' : 'flex-row'}`}>
      <div
        className={`flex h-9 w-9 shrink-0 items-center justify-center rounded-full ${
          isUser ? 'bg-primary-600 text-body' : 'bg-secondary-50 text-secondary-600'
        }`}
      >
        {isUser ? <User size={18} /> : <Bot size={18} />}
      </div>
      <div
        className={`max-w-[75%] rounded-2xl px-4 py-3 text-body-md leading-relaxed ${
          isUser
            ? 'bg-primary-600 text-body'
            : 'border border-gray-200 bg-gray-50 text-gray-900'
        }`}
      >
        <p className="whitespace-pre-wrap">{message.content}</p>
        {!isUser && message.metadata && (
          <div className="mt-3 border-t border-gray-200 pt-3 text-label-md text-gray-600">
            <p>
              <span className="font-medium text-gray-800">Herramienta:</span>{' '}
              {message.metadata.toolId ?? '—'}
            </p>
            <p className="mt-1">
              <span className="font-medium text-gray-800">Fuente:</span>{' '}
              {message.metadata.sourceTables.length > 0
                ? message.metadata.sourceTables.join(', ')
                : '—'}
            </p>
            <p className="mt-1">
              <span className="font-medium text-gray-800">Camino:</span> {message.metadata.path}
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
    <div className="flex items-center gap-3">
      <div className="flex h-9 w-9 items-center justify-center rounded-full bg-secondary-50 text-secondary-600">
        <Bot size={18} />
      </div>
      <div className="flex items-center gap-2 rounded-2xl border border-gray-200 bg-gray-50 px-4 py-3 text-body-md text-gray-600">
        <Loader2 size={16} className="animate-spin" />
        El asistente está consultando el sistema…
      </div>
    </div>
  );
}
