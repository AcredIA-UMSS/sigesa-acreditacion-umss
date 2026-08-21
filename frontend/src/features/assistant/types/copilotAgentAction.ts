import type { AssistantResolutionPath } from '../../../api/model/assistantTypes';

export type CopilotAgentActionStatus = 'pending' | 'ok' | 'error' | 'out_of_scope';

export type CopilotAgentActionStep = {
  id: string;
  label: string;
  kind: 'info' | 'pending' | 'success' | 'error';
};

export type CopilotAgentAction = {
  id: string;
  at: string;
  userPrompt: string;
  summary: string;
  toolId: string | null;
  path: AssistantResolutionPath | 'ERROR' | 'PENDING';
  sourceTables: string[];
  llmInvoked: boolean;
  status: CopilotAgentActionStatus;
  steps: CopilotAgentActionStep[];
};
