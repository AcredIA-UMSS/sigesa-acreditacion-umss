import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  useGetStatus1,
  useChat,
} from '../../../api/endpoints/assistant/assistant';
import type {
  AssistantChatContextDto,
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import { mapAssistantError } from '../../assistant/hooks/mapAssistantError';

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

export type EvidenceAgentActionStatus = 'ok' | 'error' | 'out_of_scope';

/** Entrada del historial de acciones del agente (sesión actual). */
export type EvidenceAgentAction = {
  id: string;
  at: string;
  userPrompt: string;
  summary: string;
  toolId: string | null;
  path: AssistantResolutionPath | 'ERROR';
  sourceTables: string[];
  llmInvoked: boolean;
  status: EvidenceAgentActionStatus;
};

function summarizeAction(input: {
  toolId: string | null;
  path: AssistantResolutionPath | 'ERROR';
  status: EvidenceAgentActionStatus;
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
  const [actionHistory, setActionHistory] = useState<EvidenceAgentAction[]>([]);
  const [draft, setDraft] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useGetStatus1({ agent: 'evidence' });
  const chatMutation = useChat();

  const chatContext: AssistantChatContextDto = useMemo(
    () => ({
      agent: 'evidence',
      ...(programId ? { programId } : {}),
    }),
    [programId],
  );

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const appendAction = useCallback((entry: Omit<EvidenceAgentAction, 'id' | 'at'>) => {
    setActionHistory((prev) => [
      ...prev,
      {
        ...entry,
        id: crypto.randomUUID(),
        at: new Date().toISOString(),
      },
    ]);
  }, []);

  const sendMessage = useCallback(async () => {
    const trimmed = draft.trim();
    if (!trimmed || chatMutation.isPending) return;

    const userMessage = createMessage('user', trimmed);
    setMessages((prev) => [...prev, userMessage]);
    setDraft('');

    try {
      const history = messages.map(({ role, content }) => ({ role, content }));
      const response = await chatMutation.mutateAsync({
        data: {
          message: trimmed,
          history,
          context: chatContext,
        }
      });
      const payload = response.data;
      const metadata: AssistantMessageMetadata = {
        toolId: payload.toolId ?? null,
        sourceTables: payload.sourceTables ?? [],
        path: (payload.path ?? 'LLM') as AssistantResolutionPath,
        llmInvoked: payload.llmInvoked ?? false,
      };
      setMessages((prev) => [
        ...prev,
        createMessage('assistant', payload.reply ?? '', metadata),
      ]);
      appendAction({
        userPrompt: trimmed,
        summary: summarizeAction({
          toolId: payload.toolId ?? null,
          path: (payload.path ?? 'LLM') as AssistantResolutionPath,
          status: payload.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
          reply: payload.reply ?? '',
        }),
        toolId: payload.toolId ?? null,
        path: (payload.path ?? 'LLM') as AssistantResolutionPath,
        sourceTables: payload.sourceTables ?? [],
        llmInvoked: payload.llmInvoked ?? false,
        status: payload.path === 'OUT_OF_SCOPE' ? 'out_of_scope' : 'ok',
      });
    } catch {
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(trimmed);
      appendAction({
        userPrompt: trimmed,
        summary: 'Falló la consulta al asistente.',
        toolId: null,
        path: 'ERROR',
        sourceTables: [],
        llmInvoked: false,
        status: 'error',
      });
    }
  }, [appendAction, chatContext, chatMutation, draft, messages]);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setActionHistory([]);
    setDraft('');
    chatMutation.reset();
  }, [chatMutation]);

  return {
    messages,
    actionHistory,
    draft,
    setDraft,
    sendMessage,
    clearConversation,
    messagesEndRef,
    sampleQuestions: (statusQuery.data?.data?.demoScenarios ?? []).map((scenario) => ({
      number: scenario.number ?? 0,
      title: scenario.title ?? '',
      sampleQuestion: scenario.sampleQuestion ?? '',
      expectedPath: (scenario.expectedPath ?? 'LLM') as AssistantResolutionPath,
    })),
    capabilities: statusQuery.data?.data?.capabilities ?? [],
    isAssistantEnabled: statusQuery.data?.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isForbidden:
      statusQuery.isError &&
      (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error as Error | null),
  };
}
