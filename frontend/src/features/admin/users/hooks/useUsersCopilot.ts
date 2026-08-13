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

  const statusQuery = useAssistantStatus('users');
  const chatMutation = useSendChatMessage();

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
        message: trimmed,
        history,
        context: chatContext,
      });
      setMessages((prev) => [
        ...prev,
        createMessage('assistant', response.reply, {
          toolId: response.toolId,
          sourceTables: response.sourceTables,
          path: response.path,
          llmInvoked: response.llmInvoked,
        }),
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
    sampleQuestions: statusQuery.data?.demoScenarios ?? [],
    capabilities: statusQuery.data?.capabilities ?? [],
    isAssistantEnabled: statusQuery.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isForbidden: statusQuery.isError && (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
  };
}
