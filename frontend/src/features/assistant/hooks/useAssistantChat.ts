import { useCallback, useEffect, useRef, useState } from 'react';
import {
  useAssistantStatus,
  useSendChatMessage,
} from '../../../api/endpoints/assistant-controller/assistant-controller';
import type {
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import { ASSISTANT_COPILOT_DEBUG_ACTIONS_ENABLED } from '../../../lib/config/assistantCopilotDebug';
import type {
  CopilotAgentAction,
  CopilotAgentActionStatus,
  CopilotAgentActionStep,
} from '../types/copilotAgentAction';
import { mapAssistantError } from './mapAssistantError';
import { mapAssistantResponseMetadata } from '../lib/mapAssistantResponseMetadata';
import { recordToolTraceInAction } from '../lib/recordToolTraceInAction';

function createMessage(
  role: ChatMessage['role'],
  content: string,
  metadata?: AssistantMessageMetadata,
): ChatMessage {
  return {
    id: crypto.randomUUID(),
    role,
    content,
    createdAt: new Date().toISOString(),
    metadata,
  };
}

function summarizeGeneralAction(input: {
  toolId: string | null;
  path: AssistantResolutionPath | 'ERROR';
  status: CopilotAgentActionStatus;
  reply?: string;
  stepCount?: number;
}): string {
  if (input.status === 'error') {
    return 'Falló la consulta al asistente.';
  }
  if (input.path === 'OUT_OF_SCOPE') {
    return 'Consulta fuera de alcance del asistente.';
  }
  if ((input.stepCount ?? 0) > 1) {
    return `Encadenó ${input.stepCount} tools (${input.toolId ?? 'multi-tool'}).`;
  }
  switch (input.toolId) {
    case 'list_process_phases':
      return 'Listó fases del proceso activo.';
    case 'list_process_structure':
      return 'Consultó estructura completa (fases y subfases).';
    case 'list_users':
      return 'Listó usuarios del sistema.';
    case 'search_normative_docs':
      return 'Consultó fragmentos normativos indexados.';
    default:
      if (input.reply && input.reply.length > 0) {
        return input.reply.length > 90
          ? `${input.reply.slice(0, 90).trim()}…`
          : input.reply;
      }
      return input.path === 'KEYWORD' ? 'Respuesta vía catálogo KEYWORD.' : 'Respondió vía LLM.';
  }
}

export function useAssistantChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [actionHistory, setActionHistory] = useState<CopilotAgentAction[]>([]);
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useAssistantStatus();
  const chatMutation = useSendChatMessage();

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    if (actionModalOpen) return;
    scrollToBottom();
  }, [actionModalOpen, messages, scrollToBottom]);

  const appendActionStep = useCallback(
    (actionId: string, step: Omit<CopilotAgentActionStep, 'id'>) => {
      setActionHistory((prev) =>
        prev.map((action) =>
          action.id === actionId
            ? {
                ...action,
                steps: [...action.steps, { ...step, id: crypto.randomUUID() }],
              }
            : action,
        ),
      );
    },
    [],
  );

  const finalizeAction = useCallback(
    (
      actionId: string,
      update: Partial<
        Pick<
          CopilotAgentAction,
          'summary' | 'toolId' | 'path' | 'sourceTables' | 'llmInvoked' | 'status'
        >
      >,
    ) => {
      setActionHistory((prev) =>
        prev.map((action) => (action.id === actionId ? { ...action, ...update } : action)),
      );
    },
    [],
  );

  const sendMessage = useCallback(async () => {
    const trimmed = draft.trim();
    if (!trimmed || chatMutation.isPending) return;

    const userMessage = createMessage('user', trimmed);
    setMessages((prev) => [...prev, userMessage]);
    setDraft('');

    const actionId = crypto.randomUUID();
    setActionModalOpen(true);
    setActionHistory((prev) => [
      ...prev,
      {
        id: actionId,
        at: new Date().toISOString(),
        userPrompt: trimmed,
        summary: 'Procesando mensaje…',
        toolId: null,
        path: 'PENDING',
        sourceTables: [],
        llmInvoked: false,
        status: 'pending',
        steps: [
          {
            id: crypto.randomUUID(),
            label: 'Mensaje enviado al backend (/assistant/chat)',
            kind: 'info',
          },
          {
            id: crypto.randomUUID(),
            label: 'Contexto: agent=general (validación SQLi/XSS en servidor)',
            kind: 'info',
          },
          {
            id: crypto.randomUUID(),
            label: 'Resolviendo intención (KEYWORD / LLM / RAG / OUT_OF_SCOPE)',
            kind: 'pending',
          },
        ],
      },
    ]);

    try {
      const history = messages.map(({ role, content }) => ({ role, content }));
      const response = await chatMutation.mutateAsync({
        message: trimmed,
        history,
      });

      recordToolTraceInAction(appendActionStep, actionId, response);
      appendActionStep(actionId, {
        label: 'Respuesta formateada entregada al chat',
        kind: 'success',
      });
      finalizeAction(actionId, {
        summary: summarizeGeneralAction({
          toolId: response.toolId ?? null,
          path: response.path ?? 'LLM',
          status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
          reply: response.reply,
          stepCount: response.steps?.length ?? 0,
        }),
        toolId: response.toolId ?? null,
        path: response.path ?? 'LLM',
        sourceTables: response.sourceTables ?? [],
        llmInvoked: response.llmInvoked ?? false,
        status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
      });

      setMessages((prev) => [
        ...prev,
        createMessage('assistant', response.reply, mapAssistantResponseMetadata(response)),
      ]);
    } catch {
      appendActionStep(actionId, {
        label: 'Error al contactar al asistente o entrada rechazada por seguridad',
        kind: 'error',
      });
      finalizeAction(actionId, {
        summary: 'Falló la consulta al asistente.',
        path: 'ERROR',
        status: 'error',
      });
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(trimmed);
    }
  }, [appendActionStep, chatMutation, draft, finalizeAction, messages]);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setDraft('');
    setActionHistory([]);
    setActionModalOpen(false);
    chatMutation.reset();
  }, [chatMutation]);

  return {
    messages,
    draft,
    setDraft,
    sendMessage,
    clearConversation,
    messagesContainerRef,
    messagesEndRef,
    actionHistory,
    actionModalOpen,
    setActionModalOpen,
    showDevBadge: ASSISTANT_COPILOT_DEBUG_ACTIONS_ENABLED,
    model: statusQuery.data?.model ?? '—',
    llmEnabled: statusQuery.data?.llmEnabled ?? false,
    capabilities: statusQuery.data?.capabilities ?? [],
    demoScenarios: statusQuery.data?.demoScenarios ?? [],
    isAssistantEnabled: statusQuery.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
  };
}
