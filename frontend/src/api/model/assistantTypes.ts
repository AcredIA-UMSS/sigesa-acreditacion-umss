export type ChatMessageRole = 'user' | 'assistant';

export interface ChatMessageDto {
  role: ChatMessageRole;
  content: string;
}

export interface SendChatMessageRequest {
  message: string;
  history?: ChatMessageDto[];
}

export interface SendChatMessageResponse {
  reply: string;
}

export interface AssistantStatusResponse {
  enabled: boolean;
  model: string;
}

export interface ChatMessage {
  id: string;
  role: ChatMessageRole;
  content: string;
  createdAt: string;
}
