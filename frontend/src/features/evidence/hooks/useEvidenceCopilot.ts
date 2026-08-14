import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  useAssistantStatus,
  useSendChatMessage,
} from '../../../api/endpoints/assistant-controller/assistant-controller';
import type {
  AssistantChatContextDto,
  AssistantMessageMetadata,
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

export function useEvidenceCopilot(programId?: string) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

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
    } catch {
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(trimmed);
    }
  }, [chatContext, chatMutation, draft, messages]);

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
    isForbidden:
      statusQuery.isError &&
      (statusQuery.error as { status?: number } | null)?.status === 403,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error),
  };
}
