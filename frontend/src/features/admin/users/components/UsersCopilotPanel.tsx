import { useEffect, useRef, useState } from 'react';
import { Bot, ChevronDown, ChevronUp, Loader2, MessageSquare, Send, Trash2, User } from 'lucide-react';
import type { AssistantDemoScenario, ChatMessage } from '../../../../api/model/assistantTypes';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { useUsersCopilot } from '../hooks/useUsersCopilot';
import { CopilotAssistantMetadata } from '../../../assistant/components/CopilotAssistantMetadata';
import { UsersCopilotActionDebugModal } from './UsersCopilotActionDebugModal';

export function UsersCopilotPanel() {
  const copilot = useUsersCopilot();
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const [mobileOpen, setMobileOpen] = useState(false);

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

      <div className="flex min-h-56 flex-1 flex-col overflow-hidden lg:min-h-[280px]">
        <div className="flex items-center justify-between border-b border-gray-100 px-4 py-2">
          <div className="flex items-center gap-2 text-label-md font-medium text-gray-700">
            <MessageSquare size={14} className="text-primary-600" />
            Chat
          </div>
          <Button
            variant="ghost"
            onClick={copilot.clearConversation}
            disabled={copilot.messages.length === 0 || copilot.isSending}
            className="px-2! py-1!"
          >
            <Trash2 size={14} />
          </Button>
        </div>

        <div ref={copilot.messagesContainerRef} className="flex-1 space-y-3 overflow-y-auto px-4 py-3">
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
        </div>

        <div className="border-t border-gray-200 p-4">
          <div className="flex flex-col gap-2">
            <textarea
              ref={textareaRef}
              value={copilot.draft}
              onChange={(event) => copilot.setDraft(event.target.value)}
              onKeyDown={handleKeyDown}
              placeholder="Ej.: Lista los usuarios CC activos"
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
      {copilot.debugActionsEnabled && (
        <UsersCopilotActionDebugModal
          isOpen={copilot.debugModalOpen}
          isSending={copilot.isSending}
          actions={copilot.actionHistory}
          onClose={() => copilot.setDebugModalOpen(false)}
        />
      )}

      <aside className="overflow-hidden rounded-xl border border-primary-200 bg-body shadow-sm xl:hidden">
        <button
          type="button"
          onClick={() => setMobileOpen((open) => !open)}
          className="flex w-full items-center justify-between px-4 py-4 text-left"
        >
          <div>
            <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
              Copiloto de usuarios
            </p>
            <p className="mt-1 text-body-md font-medium text-gray-900">Gestión conversacional [JD]</p>
          </div>
          {mobileOpen ? <ChevronUp size={20} /> : <ChevronDown size={20} />}
        </button>
        {mobileOpen && panelBody}
      </aside>

      <aside className="hidden max-h-[calc(100vh-8rem)] flex-col rounded-xl border border-primary-200 bg-body shadow-sm xl:sticky xl:top-8 xl:flex">
        <header className="border-b border-gray-200 px-4 py-4">
          <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
            Copiloto de usuarios
          </p>
          <h2 className="mt-1 text-heading-sm font-semibold text-gray-900">Administración [JD]</h2>
          <p className="mt-1 text-body-md text-gray-600">
            Alta, detalle, estado y asignación vía chat
          </p>
        </header>
        {panelBody}
      </aside>
    </>
  );
}

function EmptyState({
  sampleQuestions,
  onSampleSelect,
}: {
  sampleQuestions: AssistantDemoScenario[];
  onSampleSelect: (question: string) => void;
}) {
  return (
    <div className="py-4 text-center">
      <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-primary-50 text-primary-600">
        <Bot size={20} />
      </div>
      <p className="text-body-md text-gray-700">
        Gestione usuarios con lenguaje natural. Las bajas requieren confirmación explícita.
      </p>
      <ul className="mt-4 space-y-2 text-left">
        {sampleQuestions.map((scenario) => (
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
          <CopilotAssistantMetadata metadata={message.metadata} compact />
        )}
      </div>
    </div>
  );
}

function TypingIndicator() {
  return (
    <div className="flex items-center gap-2 text-body-md text-gray-600">
      <Loader2 size={14} className="animate-spin" />
      Consultando SIGESA…
    </div>
  );
}
