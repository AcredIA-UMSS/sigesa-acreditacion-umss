import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  useAssistantStatus,
  useSendChatMessage,
} from '../../../api/endpoints/assistant-controller/assistant-controller';
import type {
  AssistantChatContextDto,
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import type {
  CopilotAgentAction,
  CopilotAgentActionStatus,
  CopilotAgentActionStep,
} from '../../assistant/types/copilotAgentAction';
import { PHASES_COPILOT_DEBUG_ACTIONS_ENABLED } from '../../../lib/config/phasesCopilotDebug';
import { mapAssistantError } from '../../assistant/hooks/mapAssistantError';
import { mapAssistantResponseMetadata } from '../../assistant/lib/mapAssistantResponseMetadata';
import { recordToolTraceInAction } from '../../assistant/lib/recordToolTraceInAction';

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

export type PhasesAgentActionStatus = CopilotAgentActionStatus;
export type PhasesAgentActionStep = CopilotAgentActionStep;
export type PhasesAgentAction = CopilotAgentAction;

function summarizePhasesAction(input: {
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
    return 'Consulta fuera de alcance del copiloto de fases.';
  }
  if ((input.stepCount ?? 0) > 1) {
    return `Encadenó ${input.stepCount} tools (${input.toolId ?? 'multi-tool'}).`;
  }
  switch (input.toolId) {
    case 'list_process_phases':
      return 'Listó las fases del proceso en contexto.';
    case 'list_process_structure':
      return 'Consultó la estructura completa (fases y subfases).';
    case 'manage_process_phase':
      return 'Operación sobre fase del proceso (preview o confirmación).';
    case 'manage_process_subphase':
      return 'Operación sobre subfase (preview o confirmación).';
    case 'search_normative_docs':
      return 'Consultó fragmentos normativos indexados.';
    default:
      if (input.reply && input.reply.length > 0) {
        return input.reply.length > 90
          ? `${input.reply.slice(0, 90).trim()}…`
          : input.reply;
      }
      return 'Respondió sin tool específica.';
  }
}

export interface PhasesCopilotProcessContext {
  processId: string;
  careerName: string;
  careerCode: string;
  templateType: string;
}

export function usePhasesCopilot(process: PhasesCopilotProcessContext) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const [actionHistory, setActionHistory] = useState<CopilotAgentAction[]>([]);
  const [debugModalOpen, setDebugModalOpen] = useState(false);
  const messagesContainerRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useAssistantStatus('phases');
  const chatMutation = useSendChatMessage();

  const chatContext: AssistantChatContextDto = useMemo(
    () => ({
      agent: 'phases',
      processId: process.processId,
      careerName: process.careerName,
      careerCode: process.careerCode,
      templateType: process.templateType,
    }),
    [process.processId, process.careerName, process.careerCode, process.templateType],
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
    const debugEnabled = PHASES_COPILOT_DEBUG_ACTIONS_ENABLED;

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
              label: `Contexto: agent=phases, processId=${process.processId}`,
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
          summary: summarizePhasesAction({
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
  }, [
    appendActionStep,
    chatContext,
    chatMutation,
    draft,
    finalizeAction,
    messages,
    process.processId,
  ]);

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
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
    debugActionsEnabled: PHASES_COPILOT_DEBUG_ACTIONS_ENABLED,
    actionHistory,
    debugModalOpen,
    setDebugModalOpen,
  };
}
