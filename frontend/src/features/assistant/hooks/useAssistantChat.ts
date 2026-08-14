import { useCallback, useEffect, useRef, useState } from 'react';
import {
  useGetStatus1,
  useChat,
} from '../../../api/endpoints/assistant/assistant';
import type {
  AssistantDemoScenario,
  AssistantMessageMetadata,
  AssistantResolutionPath,
  ChatMessage,
} from '../../../api/model/assistantTypes';
import { mapAssistantError } from './mapAssistantError';

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

export function useAssistantChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [draft, setDraft] = useState('');
  const messagesEndRef = useRef<HTMLDivElement | null>(null);

  const statusQuery = useGetStatus1();
  const chatMutation = useChat();

  const scrollToBottom = useCallback(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, []);

  useEffect(() => {
    scrollToBottom();
  }, [messages, scrollToBottom]);

  const sendMessage = useCallback(async (customMessage?: string) => {
    const messageToSend = (customMessage !== undefined ? customMessage : draft).trim();
    if (!messageToSend || chatMutation.isPending) return;

    const userMessage = createMessage('user', messageToSend);
    setMessages((prev) => [...prev, userMessage]);
    setDraft('');

    try {
      const history = messages.map(({ role, content }) => ({ role, content }));
      const response = await chatMutation.mutateAsync({
        data: {
          message: messageToSend,
          history,
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
    } catch {
      setMessages((prev) => prev.filter((message) => message.id !== userMessage.id));
      setDraft(messageToSend);
    }
  }, [chatMutation, draft, messages]);


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
    model: statusQuery.data?.data?.model ?? '—',
    llmEnabled: statusQuery.data?.data?.llmEnabled ?? false,
    capabilities: statusQuery.data?.data?.capabilities ?? [],
    demoScenarios: (statusQuery.data?.data?.demoScenarios ?? []) as AssistantDemoScenario[],
    isAssistantEnabled: statusQuery.data?.data?.enabled === true,
    isStatusError: statusQuery.isError,
    isStatusLoading: statusQuery.isLoading,
    isSending: chatMutation.isPending,
    errorMessage: mapAssistantError(chatMutation.error as Error | null),
  };
}
