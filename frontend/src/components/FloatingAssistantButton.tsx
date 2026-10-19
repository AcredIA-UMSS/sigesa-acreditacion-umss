import { CircleHelp, X } from 'lucide-react';
import { useEffect, useId, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { useLocation } from 'react-router-dom';
import { AssistantActionDebugModal } from '../features/assistant/components/AssistantActionDebugModal';
import { AssistantChatUI } from '../features/assistant/components/AssistantChatUI';
import { useAssistantChat } from '../features/assistant/hooks/useAssistantChat';

const HELP_PATH = '/ayuda';
const FOCUSABLE_SELECTOR =
  'button, [href], textarea, input, select, [tabindex]:not([tabindex="-1"])';

function FloatingAssistantChat({
  titleId,
  onClose,
  closeButtonRef,
}: {
  titleId: string;
  onClose: () => void;
  closeButtonRef: React.RefObject<HTMLButtonElement | null>;
}) {
  const chat = useAssistantChat();

  return (
    <>
      <AssistantActionDebugModal
        isOpen={chat.actionModalOpen}
        isSending={chat.isSending}
        actions={chat.actionHistory}
        onClose={() => chat.setActionModalOpen(false)}
        showDevBadge={chat.showDevBadge}
      />
      <div className="flex min-h-0 flex-1 flex-col">
        <header className="flex shrink-0 items-start justify-between gap-3 border-b border-gray-200 px-5 py-4">
          <div>
            <p className="text-label-md font-medium uppercase tracking-wide text-primary-600">
              SIGESA · Ayuda
            </p>
            <h2 id={titleId} className="text-heading-sm font-semibold text-gray-900">
              Asistente virtual
            </h2>
          </div>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            className="inline-flex h-10 w-10 items-center justify-center rounded-lg text-primary-800 hover:bg-primary-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
            aria-label="Cerrar asistente"
          >
            <X size={20} />
          </button>
        </header>
        <AssistantChatUI
          compact
          messages={chat.messages}
          draft={chat.draft}
          onDraftChange={chat.setDraft}
          onSend={() => void chat.sendMessage()}
          onClear={chat.clearConversation}
          onSampleSelect={chat.setDraft}
          onOpenActionHistory={() => chat.setActionModalOpen(true)}
          actionHistoryCount={chat.actionHistory.length}
          model={chat.model}
          llmEnabled={chat.llmEnabled}
          capabilities={chat.capabilities}
          demoScenarios={chat.demoScenarios}
          isAssistantEnabled={chat.isAssistantEnabled}
          isStatusError={chat.isStatusError}
          isStatusLoading={chat.isStatusLoading}
          isSending={chat.isSending}
          errorMessage={chat.errorMessage}
          messagesContainerRef={chat.messagesContainerRef}
          messagesEndRef={chat.messagesEndRef}
        />
      </div>
    </>
  );
}

export function FloatingAssistantButton() {
  const location = useLocation();
  const titleId = useId();
  const [isOpen, setIsOpen] = useState(false);
  const wasOpen = useRef(false);
  const triggerRef = useRef<HTMLButtonElement>(null);
  const dialogRef = useRef<HTMLDivElement>(null);
  const closeButtonRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!isOpen) {
      return;
    }
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    closeButtonRef.current?.focus();

    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        event.preventDefault();
        setIsOpen(false);
        return;
      }
      if (event.key !== 'Tab' || !dialogRef.current) {
        return;
      }
      const focusable = Array.from(
        dialogRef.current.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR),
      ).filter((node) => !node.hasAttribute('disabled') && node.tabIndex !== -1);
      if (focusable.length === 0) {
        return;
      }
      const first = focusable[0];
      const last = focusable[focusable.length - 1];
      const active = document.activeElement;
      if (event.shiftKey && active === first) {
        event.preventDefault();
        last.focus();
      } else if (!event.shiftKey && active === last) {
        event.preventDefault();
        first.focus();
      }
    };

    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.body.style.overflow = previousOverflow;
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [isOpen]);

  useEffect(() => {
    if (wasOpen.current && !isOpen) {
      triggerRef.current?.focus();
    }
    wasOpen.current = isOpen;
  }, [isOpen]);

  if (location.pathname === HELP_PATH) {
    return null;
  }

  const widget = (
    <>
      <button
        ref={triggerRef}
        type="button"
        onClick={() => setIsOpen(true)}
        className="fixed bottom-24 right-6 z-40 flex h-14 w-14 items-center justify-center rounded-full bg-primary-600 text-body shadow-lg transition-transform hover:scale-105 hover:bg-primary-700 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-300"
        aria-label="Abrir asistente"
        aria-haspopup="dialog"
        aria-expanded={isOpen}
      >
        <CircleHelp size={24} aria-hidden />
      </button>

      {isOpen
        ? (
            <div className="fixed inset-0 z-50 flex items-end justify-end p-4 sm:items-center sm:justify-center sm:p-6">
              <button
                type="button"
                className="absolute inset-0 bg-gray-900/40"
                aria-label="Cerrar asistente"
                tabIndex={-1}
                onClick={() => setIsOpen(false)}
              />
              <div
                ref={dialogRef}
                role="dialog"
                aria-modal="true"
                aria-labelledby={titleId}
                className="relative z-10 flex h-[min(36rem,calc(100vh-2rem))] w-full max-w-2xl flex-col overflow-hidden rounded-2xl border border-gray-200 bg-body shadow-2xl"
              >
                <FloatingAssistantChat
                  titleId={titleId}
                  onClose={() => setIsOpen(false)}
                  closeButtonRef={closeButtonRef}
                />
              </div>
            </div>
          )
        : null}
    </>
  );

  if (typeof document === 'undefined') {
    return widget;
  }

  return createPortal(widget, document.body);
}
