/**
 * Orval-compatible client — regenerar con `pnpm run generate:api` cuando el backend esté activo.
 */
import { useMutation, useQuery } from '@tanstack/react-query';
import type {
  UseMutationOptions,
  UseMutationResult,
  UseQueryOptions,
  UseQueryResult,
} from '@tanstack/react-query';

import type {
  AssistantAgentId,
  AssistantStatusResponse,
  SendChatMessageRequest,
  SendChatMessageResponse,
} from '../../model/assistantTypes';
import { customFetch } from '../../../lib/api/customFetch';

type FetchEnvelope<T> = { data: T };

export const getAssistantStatusUrl = (agent?: AssistantAgentId) =>
  agent ? `/api/v1/assistant/status?agent=${agent}` : '/api/v1/assistant/status';
export const getAssistantChatUrl = () => '/api/v1/assistant/chat';

export const fetchAssistantStatus = async (
  agent?: AssistantAgentId,
): Promise<AssistantStatusResponse> => {
  const response = await customFetch<FetchEnvelope<AssistantStatusResponse>>(
    getAssistantStatusUrl(agent),
    { method: 'GET' },
  );
  return response.data;
};

export const sendChatMessage = async (
  request: SendChatMessageRequest,
): Promise<SendChatMessageResponse> => {
  const response = await customFetch<FetchEnvelope<SendChatMessageResponse>>(
    getAssistantChatUrl(),
    {
      method: 'POST',
      body: JSON.stringify(request),
    },
  );
  return response.data;
};

export const useAssistantStatus = (
  agent?: AssistantAgentId,
  options?: Omit<UseQueryOptions<AssistantStatusResponse, Error>, 'queryKey' | 'queryFn'>,
): UseQueryResult<AssistantStatusResponse, Error> =>
  useQuery({
    queryKey: ['assistantStatus', agent ?? 'general'],
    queryFn: () => fetchAssistantStatus(agent),
    staleTime: 60_000,
    ...options,
  });

export const useSendChatMessage = (
  options?: UseMutationOptions<SendChatMessageResponse, Error, SendChatMessageRequest>,
): UseMutationResult<SendChatMessageResponse, Error, SendChatMessageRequest> =>
  useMutation({
    mutationFn: sendChatMessage,
    ...options,
  });
