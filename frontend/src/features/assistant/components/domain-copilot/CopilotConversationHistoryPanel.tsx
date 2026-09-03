import { History, MessageSquare, Trash2 } from 'lucide-react';
import type { CopilotConversationArchive } from '../../types/domainCopilotKind';
import { Button } from '../../../../components/ui/Button';
import { CopilotMessageBubble } from './CopilotMessageBubble';
import type { ChatMessage } from '../../../../api/model/assistantTypes';

interface CopilotConversationHistoryPanelProps {
  currentMessages: ChatMessage[];
  archives: CopilotConversationArchive[];
  onClose: () => void;
  onRemoveArchive: (archiveId: string) => void;
}

function formatWhen(iso: string): string {
  try {
    return new Intl.DateTimeFormat('es-BO', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export function CopilotConversationHistoryPanel({
  currentMessages,
  archives,
  onClose,
  onRemoveArchive,
}: CopilotConversationHistoryPanelProps) {
  return (
    <div className="flex h-full flex-col">
      <div className="flex items-center justify-between border-b border-gray-200 px-4 py-3">
        <div className="flex items-center gap-2 text-body-md font-semibold text-gray-900">
          <History size={16} className="text-primary-600" />
          Historial
        </div>
        <Button variant="ghost" className="px-2! py-1!" onClick={onClose}>
          Volver al chat
        </Button>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto px-4 py-3">
        <section>
          <h3 className="mb-2 flex items-center gap-2 text-label-md font-medium uppercase tracking-wide text-gray-600">
            <MessageSquare size={12} />
            Conversación actual
          </h3>
          {currentMessages.length === 0 ? (
            <p className="rounded-lg border border-dashed border-gray-300 px-3 py-4 text-body-md text-gray-600">
              Aún no hay mensajes en esta sesión.
            </p>
          ) : (
            <div className="space-y-3 rounded-lg border border-gray-200 bg-gray-50 p-3">
              {currentMessages.map((message) => (
                <CopilotMessageBubble key={message.id} message={message} />
              ))}
            </div>
          )}
        </section>

        <section>
          <h3 className="mb-2 text-label-md font-medium uppercase tracking-wide text-gray-600">
            Conversaciones anteriores
          </h3>
          {archives.length === 0 ? (
            <p className="rounded-lg border border-dashed border-gray-300 px-3 py-4 text-body-md text-gray-600">
              Las conversaciones archivadas al limpiar el chat aparecerán aquí.
            </p>
          ) : (
            <ul className="space-y-2">
              {archives.map((archive) => (
                <li
                  key={archive.id}
                  className="rounded-lg border border-gray-200 bg-body p-3 shadow-sm"
                >
                  <div className="flex items-start justify-between gap-2">
                    <div className="min-w-0">
                      <p className="truncate text-body-md font-medium text-gray-900">
                        {archive.preview}
                      </p>
                      <p className="mt-1 text-label-md text-gray-500">
                        {formatWhen(archive.updatedAt)} · {archive.messages.length} mensajes
                      </p>
                    </div>
                    <Button
                      variant="ghost"
                      className="shrink-0 px-2! py-1!"
                      aria-label="Eliminar conversación archivada"
                      onClick={() => onRemoveArchive(archive.id)}
                    >
                      <Trash2 size={14} />
                    </Button>
                  </div>
                  <details className="mt-2">
                    <summary className="cursor-pointer text-body-md text-primary-600 hover:text-primary-800">
                      Ver mensajes
                    </summary>
                    <div className="mt-2 space-y-2 border-t border-gray-100 pt-2">
                      {archive.messages.map((message, index) => (
                        <div
                          key={`${archive.id}-${index}`}
                          className={`rounded-lg px-2 py-1 text-body-md ${
                            message.role === 'user'
                              ? 'bg-primary-50 text-primary-900'
                              : 'bg-gray-100 text-gray-900'
                          }`}
                        >
                          <p className="text-label-md font-medium text-gray-600">
                            {message.role === 'user' ? 'Usted' : 'Asistente'}
                          </p>
                          <p className="whitespace-pre-wrap">{message.content}</p>
                        </div>
                      ))}
                    </div>
                  </details>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
