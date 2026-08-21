import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  useAssistantStatus,
  useSendChatMessage,
} from '../../../../api/endpoints/assistant-controller/assistant-controller';
import { getListQueryKey } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type {
  AssistantChatContextDto,
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../../api/model/assistantTypes';
import type {
  CopilotAgentAction,
  CopilotAgentActionStatus,
  CopilotAgentActionStep,
} from '../../../assistant/types/copilotAgentAction';
import { USERS_COPILOT_DEBUG_ACTIONS_ENABLED } from '../../../../lib/config/usersCopilotDebug';
import { mapAssistantError } from '../../../assistant/hooks/mapAssistantError';
import { mapAssistantResponseMetadata } from '../../../assistant/lib/mapAssistantResponseMetadata';
import { recordToolTraceInAction } from '../../../assistant/lib/recordToolTraceInAction';

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

function summarizeUsersAction(input: {
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
    return 'Consulta fuera de alcance del copiloto de usuarios.';
  }
  if ((input.stepCount ?? 0) > 1) {
    return `Encadenó ${input.stepCount} tools (${input.toolId ?? 'multi-tool'}).`;
  }
  switch (input.toolId) {
    case 'list_users':
      return 'Listó usuarios con filtros aplicados.';
    case 'get_user_detail':
      return 'Consultó el detalle de un usuario.';
    case 'create_user':
      return 'Operación de alta de usuario (preview o confirmación).';
    case 'manage_user_status':
      return 'Operación de activación/desactivación (preview o confirmación).';
    case 'manage_user_assignment':
      return 'Operación de asignación de carrera (preview o confirmación).';
    case 'set_user_status':
      return 'Cambio de estado de usuario (legacy).';
    default:
      if (input.reply && input.reply.length > 0) {
        return input.reply.length > 90
          ? `${input.reply.slice(0, 90).trim()}…`
          : input.reply;
      }
      return 'Respondió sin tool específica.';
  }
}

export function useUsersCopilot() {
  const queryClient = useQueryClient();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [actionHistory, setActionHistory] = useState<CopilotAgentAction[]>([]);
  const [debugModalOpen, setDebugModalOpen] = useState(false);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useAssistantStatus('users');
  const chatMutation = useSendChatMessage();

  const chatContext: AssistantChatContextDto = useMemo(
    () => ({
      agent: 'users',
    }),
    [],
  );

  const scrollToBottom = useCallback(() => {
    const container = messagesContainerRef.current;
    if (!container) return;
    container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
  }, []);

  useEffect(() => {
    if (debugModalOpen) return;
    scrollToBottom();
  }, [debugModalOpen, messages, scrollToBottom]);

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
    const debugEnabled = USERS_COPILOT_DEBUG_ACTIONS_ENABLED;

    if (debugEnabled) {
      setDebugModalOpen(true);
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
              label: 'Contexto: agent=users (solo JD)',
              kind: 'info',
            },
            {
              id: crypto.randomUUID(),
              label: 'Resolviendo intención (KEYWORD / LLM / OUT_OF_SCOPE)',
              kind: 'pending',
            },
          ],
        },
      ]);
    }

    try {
      const history = messages.map(({ role, content }) => ({ role, content }));
      const response = await chatMutation.mutateAsync({
        message: trimmed,
        history,
        context: chatContext,
      });

      if (debugEnabled) {
        recordToolTraceInAction(appendActionStep, actionId, response);
        appendActionStep(actionId, {
          label: 'Respuesta formateada entregada al panel',
          kind: 'success',
        });
        finalizeAction(actionId, {
          summary: summarizeUsersAction({
            toolId: response.toolId,
            path: response.path,
            status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
            reply: response.reply,
            stepCount: response.steps?.length ?? 0,
          }),
          toolId: response.toolId,
          path: response.path,
          sourceTables: response.sourceTables,
          llmInvoked: response.llmInvoked,
          status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
        });
      }

      setMessages((prev) => [
        ...prev,
        createMessage('assistant', response.reply, mapAssistantResponseMetadata(response)),
      ]);

      const writeTools = new Set([
        'create_user',
        'manage_user_status',
        'manage_user_assignment',
        'set_user_status',
      ]);
      if (response.toolId && writeTools.has(response.toolId) && !response.reply.includes('confirmo')) {
        await queryClient.invalidateQueries({ queryKey: getListQueryKey(undefined) });
      }
    } catch {
      if (debugEnabled) {
        appendActionStep(actionId, {
          label: 'Error al contactar al asistente o entrada rechazada',
          kind: 'error',
        });
        finalizeAction(actionId, {
          summary: 'Falló la consulta al asistente.',
          path: 'ERROR',
          status: 'error',
        });
      }
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(trimmed);
    }
  }, [appendActionStep, chatContext, chatMutation, draft, finalizeAction, messages, queryClient]);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setDraft('');
    setActionHistory([]);
    chatMutation.reset();
  }, [chatMutation]);

  return {
    messages,
    draft,
    setDraft,
    sendMessage,
    clearConversation,
    messagesContainerRef,
    sampleQuestions: statusQuery.data?.demoScenarios ?? [],
    capabilities: statusQuery.data?.capabilities ?? [],
    isAssistantEnabled: statusQuery.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isForbidden: statusQuery.isError && (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
    debugActionsEnabled: USERS_COPILOT_DEBUG_ACTIONS_ENABLED,
    actionHistory,
    debugModalOpen,
    setDebugModalOpen,
  };
}
