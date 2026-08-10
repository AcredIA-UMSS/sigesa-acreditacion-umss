import { useEffect, useRef } from 'react';
import { Bot, Loader2, MessageSquare, Send, Trash2, User } from 'lucide-react';
import type { ChatMessage } from '../../../api/model/assistantTypes';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';

export type AssistantChatUIProps = {
  messages: ChatMessage[];
  draft: string;
  onDraftChange: (value: string) => void;
  onSend: () => void;
  onClear: () => void;
  model: string;
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
  model,
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
              Consultas sobre acreditación, evidencias, indicadores y uso del sistema.
              Conectado a Open WebUI mediante el backend SIGESA.
            </p>
          </div>
          <div className="rounded-lg border border-gray-200 bg-gray-50 px-4 py-3 text-right">
            <p className="text-label-md text-gray-500">Modelo</p>
            <p className="text-body-md font-medium text-gray-900">
              {isStatusLoading ? 'Cargando…' : model}
            </p>
          </div>
        </div>
      </header>

      <div className="flex flex-1 flex-col overflow-hidden px-8 py-6">
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
              <EmptyState />
            ) : (
              messages.map((message) => (
                <MessageBubble key={message.id} message={message} />
              ))
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
    </main>
  );
}

function EmptyState() {
  return (
    <div className="flex h-full min-h-[240px] flex-col items-center justify-center text-center">
      <div className="mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-primary-50 text-primary-600">
        <Bot size={28} />
      </div>
      <h2 className="text-heading-sm font-semibold text-gray-900">
        ¿En qué puedo ayudarte?
      </h2>
      <p className="mt-2 max-w-md text-body-md text-gray-600">
        Pregunta sobre procesos de acreditación, carga de evidencias o navegación
        en SIGESA.
      </p>
      <ul className="mt-6 space-y-2 text-left text-body-md text-gray-700">
        <li>• ¿Cómo cargo una evidencia para un indicador?</li>
        <li>• ¿Qué estados puede tener un indicador?</li>
        <li>• ¿Cómo inicio un proceso de acreditación?</li>
      </ul>
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
        El asistente está escribiendo…
      </div>
    </div>
  );
}
