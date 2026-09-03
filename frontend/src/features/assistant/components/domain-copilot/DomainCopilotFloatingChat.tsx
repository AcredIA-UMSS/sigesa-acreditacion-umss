import { useEffect, useRef, useState, type ReactNode } from 'react';
import { createPortal } from 'react-dom';
import {
  Bot,
  History,
  Loader2,
  Minimize2,
  Send,
  Trash2,
} from 'lucide-react';
import type { AssistantDemoScenario, ChatMessage } from '../../../../api/model/assistantTypes';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import { DOMAIN_COPILOT_PRESENTATION } from '../../lib/domainCopilotPresentation';
import { useCopilotConversationArchive } from '../../lib/useCopilotConversationArchive';
import type { DomainCopilotKind } from '../../types/domainCopilotKind';
import { CopilotConversationHistoryPanel } from './CopilotConversationHistoryPanel';
import { CopilotMessageBubble } from './CopilotMessageBubble';

export interface DomainCopilotFloatingState {
  messages: ChatMessage[];
  draft: string;
  setDraft: (value: string) => void;
  sendMessage: () => Promise<void>;
  clearConversation: () => void;
  messagesContainerRef: React.RefObject<HTMLDivElement | null>;
  sampleQuestions: AssistantDemoScenario[];
  isAssistantEnabled: boolean;
  isStatusError: boolean;
  isStatusLoading: boolean;
  isSending: boolean;
  errorMessage?: string | null;
}

export interface DomainCopilotFloatingChatProps {
  kind: DomainCopilotKind;
  archiveContextKey: string;
  title: string;
  subtitle?: string;
  readOnly?: boolean;
  placeholder?: string;
  emptyStateMessage?: string;
  sampleQuestionSanitizer?: (question: string) => string;
  copilot: DomainCopilotFloatingState;
  debugModal?: ReactNode;
  onOpenActionHistory?: () => void;
  actionHistoryCount?: number;
}

export function DomainCopilotFloatingChat({
  kind,
  archiveContextKey,
  title,
  subtitle,
  readOnly = false,
  placeholder,
  emptyStateMessage,
  sampleQuestionSanitizer,
  copilot,
  debugModal,
  onOpenActionHistory,
  actionHistoryCount = 0,
}: DomainCopilotFloatingChatProps) {
  const presentation = DOMAIN_COPILOT_PRESENTATION[kind];
  const [isOpen, setIsOpen] = useState(false);
  const [historyOpen, setHistoryOpen] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);
  const { archives, archiveCount, refreshArchives, archiveCurrentConversation, removeArchive } =
    useCopilotConversationArchive(kind, archiveContextKey, title, subtitle);

  useEffect(() => {
    if (isOpen && !copilot.isSending && !historyOpen) {
      textareaRef.current?.focus();
    }
  }, [copilot.isSending, historyOpen, isOpen]);

  useEffect(() => {
    if (historyOpen) {
      refreshArchives();
    }
  }, [historyOpen, refreshArchives]);

  if (copilot.isStatusLoading || !copilot.isAssistantEnabled) {
    return null;
  }

  const handleClear = () => {
    archiveCurrentConversation(copilot.messages);
    copilot.clearConversation();
    setHistoryOpen(false);
  };

  const handleKeyDown = (event: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      void copilot.sendMessage();
    }
  };

  const sampleQuestions = readOnly
    ? copilot.sampleQuestions.filter(
        (scenario) => !scenario.title.toLowerCase().includes('edición'),
      )
    : copilot.sampleQuestions;

  const sanitizedSamples = sampleQuestionSanitizer
    ? sampleQuestions.map((scenario) => ({
        ...scenario,
        sampleQuestion: sampleQuestionSanitizer(scenario.sampleQuestion),
      }))
    : sampleQuestions;

  const widget = (
    <>
      {debugModal}

      {!isOpen && (
        <button
          type="button"
          onClick={() => setIsOpen(true)}
          className="fixed bottom-6 right-6 z-50 flex h-14 w-14 items-center justify-center rounded-full bg-primary-600 text-body shadow-lg transition-transform hover:scale-105 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-primary-300"
          aria-label={presentation.fabAriaLabel}
        >
          <Bot size={24} />
          <span className="absolute -right-1 -top-1 rounded-full bg-secondary px-1.5 py-0.5 text-label-md font-semibold text-body">
            {presentation.contextLabel}
          </span>
        </button>
      )}

      {isOpen && (
        <section
          className="fixed bottom-6 right-6 z-50 flex w-[min(100vw-2rem,24rem)] flex-col overflow-hidden rounded-2xl border border-primary-200 bg-body shadow-2xl"
          style={{ height: 'min(32rem, calc(100vh - 5rem))' }}
          aria-label={title}
        >
          <header className="flex shrink-0 items-start justify-between gap-2 border-b border-gray-200 bg-primary-50 px-4 py-3">
            <div className="min-w-0">
              <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
                {presentation.contextLabel}
                {readOnly ? ' · Solo lectura' : ''}
              </p>
              <h2 className="truncate text-heading-sm font-semibold text-gray-900">{title}</h2>
              {subtitle && (
                <p className="mt-0.5 truncate text-body-md text-gray-600">{subtitle}</p>
              )}
            </div>
            <div className="flex shrink-0 items-center gap-1">
              <Button
                variant="ghost"
                className="px-2! py-1!"
                aria-label="Historial de conversaciones"
                onClick={() => setHistoryOpen((open) => !open)}
              >
                <History size={16} />
                {(archiveCount > 0 || copilot.messages.length > 0) && (
                  <span className="ml-1 text-label-md">{archiveCount + (copilot.messages.length > 0 ? 1 : 0)}</span>
                )}
              </Button>
              <Button
                variant="ghost"
                className="px-2! py-1!"
                aria-label="Limpiar conversación"
                disabled={copilot.messages.length === 0 || copilot.isSending}
                onClick={handleClear}
              >
                <Trash2 size={16} />
              </Button>
              <Button
                variant="ghost"
                className="px-2! py-1!"
                aria-label="Minimizar copiloto"
                onClick={() => {
                  setIsOpen(false);
                  setHistoryOpen(false);
                }}
              >
                <Minimize2 size={16} />
              </Button>
            </div>
          </header>

          {copilot.isStatusError && (
            <div className="shrink-0 px-4 pt-3">
              <Alert variant="error">
                No se pudo conectar con el asistente. Verifique el backend.
              </Alert>
            </div>
          )}

          {copilot.errorMessage && (
            <div className="shrink-0 px-4 pt-3">
              <Alert variant="error">{copilot.errorMessage}</Alert>
            </div>
          )}

          {historyOpen ? (
            <CopilotConversationHistoryPanel
              currentMessages={copilot.messages}
              archives={archives}
              onClose={() => setHistoryOpen(false)}
              onRemoveArchive={removeArchive}
            />
          ) : (
            <>
              {onOpenActionHistory && (
                <div className="shrink-0 border-b border-gray-100 px-4 py-2">
                  <button
                    type="button"
                    onClick={onOpenActionHistory}
                    className="text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2 hover:text-primary-800"
                  >
                    Historial de acciones
                    {actionHistoryCount > 0 ? ` (${actionHistoryCount})` : ''}
                  </button>
                </div>
              )}

              <div
                ref={copilot.messagesContainerRef}
                className="min-h-0 flex-1 space-y-3 overflow-y-auto px-4 py-3"
              >
                {copilot.messages.length === 0 ? (
                  <EmptyState
                    message={
                      emptyStateMessage ??
                      'Pregunte al asistente sobre esta sección del sistema.'
                    }
                    sampleQuestions={sanitizedSamples}
                    onSampleSelect={(question) => copilot.setDraft(question)}
                  />
                ) : (
                  copilot.messages.map((message) => (
                    <CopilotMessageBubble key={message.id} message={message} />
                  ))
                )}
                {copilot.isSending && (
                  <div className="flex items-center gap-2 text-body-md text-gray-600">
                    <Loader2 size={14} className="animate-spin" />
                    {presentation.typingLabel}
                  </div>
                )}
              </div>

              <div className="shrink-0 border-t border-gray-200 p-3">
                <div className="flex flex-col gap-2">
                  <textarea
                    ref={textareaRef}
                    value={copilot.draft}
                    onChange={(event) => copilot.setDraft(event.target.value)}
                    onKeyDown={handleKeyDown}
                    placeholder={placeholder ?? 'Escriba su consulta…'}
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
            </>
          )}
        </section>
      )}
    </>
  );

  if (typeof document === 'undefined') {
    return widget;
  }

  return createPortal(widget, document.body);
}

function EmptyState({
  message,
  sampleQuestions,
  onSampleSelect,
}: {
  message: string;
  sampleQuestions: AssistantDemoScenario[];
  onSampleSelect: (question: string) => void;
}) {
  return (
    <div className="py-2 text-center">
      <div className="mx-auto mb-3 flex h-10 w-10 items-center justify-center rounded-full bg-primary-50 text-primary-600">
        <Bot size={20} />
      </div>
      <p className="text-body-md text-gray-700">{message}</p>
      {sampleQuestions.length > 0 && (
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
      )}
    </div>
  );
}
