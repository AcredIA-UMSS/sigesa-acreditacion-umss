export type DomainCopilotKind = 'phases' | 'evidence' | 'users';

export interface CopilotConversationArchive {
  id: string;
  kind: DomainCopilotKind;
  contextKey: string;
  title: string;
  subtitle?: string;
  startedAt: string;
  updatedAt: string;
  preview: string;
  messages: Array<{ role: 'user' | 'assistant'; content: string; createdAt: string }>;
}
