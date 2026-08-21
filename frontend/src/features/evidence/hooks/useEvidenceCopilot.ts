import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  useAssistantStatus,
  useSendChatMessage,
} from '../../../api/endpoints/assistant-controller/assistant-controller';
import type {
  AssistantChatContextDto,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import type {
  CopilotAgentAction,
  CopilotAgentActionStatus,
  CopilotAgentActionStep,
} from '../../assistant/types/copilotAgentAction';
import { EVIDENCE_COPILOT_DEBUG_ACTIONS_ENABLED } from '../../../lib/config/evidenceCopilotDebug';
import { mapAssistantError } from '../../assistant/hooks/mapAssistantError';

function createMessage(role: ChatMessage['role'], content: string): ChatMessage {
  return {
    id: crypto.randomUUID(),
    role,
    content,
    createdAt: new Date().toISOString(),
  };
}

export type EvidenceAgentActionStatus = CopilotAgentActionStatus;
export type EvidenceAgentAction = CopilotAgentAction;

function summarizeEvidenceAction(input: {
  toolId: string | null;
  path: AssistantResolutionPath | 'ERROR';
  status: CopilotAgentActionStatus;
  reply?: string;
}): string {
  if (input.status === 'error') {
    return 'Falló la consulta al asistente.';
  }
  if (input.path === 'OUT_OF_SCOPE') {
    return 'Consulta fuera de alcance del agente de evidencias.';
  }
  switch (input.toolId) {
    case 'list_pending_evidences':
      return 'Listó evidencias pendientes de revisión (SUBIDO).';
    case 'get_evidence_detail':
      return 'Consultó el detalle de una evidencia.';
    case 'check_evidence_completeness':
      return 'Verificó la completitud de una evidencia.';
    default:
      if (input.reply && input.reply.length > 0) {
        return input.reply.length > 90
          ? `${input.reply.slice(0, 90).trim()}…`
          : input.reply;
      }
      return 'Respondió sin tool específica.';
  }
}

export function useEvidenceCopilot(programId?: string) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [actionHistory, setActionHistory] = useState<CopilotAgentAction[]>([]);
  const [draft, setDraft] = useState('');
  const [actionModalOpen, setActionModalOpen] = useState(false);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useAssistantStatus('evidence');
  const chatMutation = useSendChatMessage();

  const chatContext: AssistantChatContextDto = useMemo(
    () => ({
      agent: 'evidence',
      ...(programId ? { programId } : {}),
    }),
    [programId],
  );

  const scrollToBottom = useCallback(() => {
    const container = messagesContainerRef.current;
    if (!container) return;
    container.scrollTo({ top: container.scrollHeight, behavior: 'smooth' });
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
            label: `Contexto: agent=evidence${programId ? `, programId=${programId}` : ''}`,
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

    try {
      const history = messages.map(({ role, content }) => ({ role, content }));
      const response = await chatMutation.mutateAsync({
        message: trimmed,
        history,
        context: chatContext,
      });

      appendActionStep(actionId, {
        label: `Tool ejecutada: ${response.toolId ?? 'ninguna'} (${response.path})`,
        kind: 'success',
      });
      if (response.llmInvoked) {
        appendActionStep(actionId, {
          label: 'LLM invocado para selección de tool',
          kind: 'info',
        });
      }
      if (response.sourceTables.length > 0) {
        appendActionStep(actionId, {
          label: `Fuentes consultadas: ${response.sourceTables.join(', ')}`,
          kind: 'info',
        });
      }
      appendActionStep(actionId, {
        label: 'Respuesta entregada al chat',
        kind: 'success',
      });
      finalizeAction(actionId, {
        summary: summarizeEvidenceAction({
          toolId: response.toolId,
          path: response.path,
          status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
          reply: response.reply,
        }),
        toolId: response.toolId,
        path: response.path,
        sourceTables: response.sourceTables ?? [],
        llmInvoked: response.llmInvoked,
        status: response.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
      });

      setMessages((prev) => [...prev, createMessage('assistant', response.reply)]);
    } catch {
      appendActionStep(actionId, {
        label: 'Error al contactar al asistente o entrada rechazada',
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
  }, [appendActionStep, chatContext, chatMutation, draft, finalizeAction, messages, programId]);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setActionHistory([]);
    setDraft('');
    setActionModalOpen(false);
    chatMutation.reset();
  }, [chatMutation]);

  return {
    messages,
    actionHistory,
    draft,
    setDraft,
    sendMessage,
    clearConversation,
    messagesContainerRef,
    sampleQuestions: statusQuery.data?.demoScenarios ?? [],
    isAssistantEnabled: statusQuery.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isForbidden:
      statusQuery.isError &&
      (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
    actionModalOpen,
    setActionModalOpen,
    showDevBadge: EVIDENCE_COPILOT_DEBUG_ACTIONS_ENABLED,
  };
}
