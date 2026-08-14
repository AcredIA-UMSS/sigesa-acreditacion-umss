import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import {
  useGetStatus1,
  useChat,
} from '../../../../api/endpoints/assistant/assistant';
import { getListQueryKey } from '../../../../api/endpoints/user-admin-controller/user-admin-controller';
import type {
  AssistantChatContextDto,
  AssistantDemoScenario,
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../../api/model/assistantTypes';
import { mapAssistantError } from '../../../assistant/hooks/mapAssistantError';

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

export function useUsersCopilot() {
  const queryClient = useQueryClient();
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useGetStatus1({ agent: 'users' });
  const chatMutation = useChat();

  const chatContext: AssistantChatContextDto = useMemo(
    () => ({
      agent: 'users',
    }),
    [],
  );

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

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
      setMessages((prev) => [
        ...prev,
        createMessage('assistant', response.data.reply ?? '', {
          toolId: response.data.toolId ?? null,
          sourceTables: response.data.sourceTables ?? [],
          path: (response.data.path ?? 'LLM') as AssistantResolutionPath,
          llmInvoked: response.data.llmInvoked ?? false,
        }),
      ]);

      const writeTools = new Set([
        'create_user',
        'manage_user_status',
        'manage_user_assignment',
        'set_user_status',
      ]);
      if (response.data.toolId && writeTools.has(response.data.toolId) && !(response.data.reply ?? '').includes('confirmo')) {
        await queryClient.invalidateQueries({ queryKey: getListQueryKey(undefined) });
      }
    } catch {
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(trimmed);
    }
  }, [chatContext, chatMutation, draft, messages, queryClient]);

  const clearConversation = useCallback(() => {
    setMessages([]);
    setDraft('');
    chatMutation.reset();
  }, [chatMutation]);

  return {
    messages,
    draft,
    setDraft,
    sendMessage,
    clearConversation,
    messagesEndRef,
    sampleQuestions: (statusQuery.data?.data?.demoScenarios ?? []) as AssistantDemoScenario[],
    capabilities: statusQuery.data?.data?.capabilities ?? [],
    isAssistantEnabled: statusQuery.data?.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isForbidden: statusQuery.isError && (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error as Error | null),
  };
}
