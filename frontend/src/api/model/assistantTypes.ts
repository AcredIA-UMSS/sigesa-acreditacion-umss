export type ChatMessageRole = 'user' | 'assistant';

export type AssistantResolutionPath = 'KEYWORD' | 'LLM' | 'OUT_OF_SCOPE';

export interface ChatMessageDto {
  role: ChatMessageRole;
  content: string;
}

export type AssistantAgentId = 'general' | 'phases' | 'users' | 'evidence';

export interface AssistantChatContextDto {
  agent: AssistantAgentId;
  processId?: string;
  careerName?: string;
  careerCode?: string;
  templateType?: string;
  userId?: string;
  programId?: string;
}

export interface SendChatMessageRequest {
  message: string;
  history?: ChatMessageDto[];
  context?: AssistantChatContextDto;
}

export interface SendChatMessageResponse {
  reply: string;
  toolId: string | null;
  sourceTables: string[];
  path: AssistantResolutionPath;
  llmInvoked: boolean;
}

export interface AssistantDemoScenario {
  number: number;
  title: string;
  sampleQuestion: string;
  expectedPath: AssistantResolutionPath;
}

export interface AssistantStatusResponse {
  enabled: boolean;
  llmEnabled: boolean;
  model: string;
  capabilities: string[];
  demoScenarios: AssistantDemoScenario[];
  agent: AssistantAgentId;
}

export interface AssistantMessageMetadata {
  toolId: string | null;
  sourceTables: string[];
  path: AssistantResolutionPath;
  llmInvoked: boolean;
}

export interface ChatMessage {
  id: string;
  role: ChatMessageRole;
  content: string;
  createdAt: string;
  metadata?: AssistantMessageMetadata;
}
