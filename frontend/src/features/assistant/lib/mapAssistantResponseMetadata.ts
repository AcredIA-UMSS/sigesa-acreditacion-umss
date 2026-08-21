import type {
  AssistantMessageMetadata,
  AssistantResolutionPath,
  SendChatMessageResponse,
} from '../../../api/model/assistantTypes';

export function mapAssistantResponseMetadata(
  response: SendChatMessageResponse,
): AssistantMessageMetadata {
  return {
    toolId: response.toolId ?? null,
    sourceTables: response.sourceTables ?? [],
    path: (response.path ?? 'LLM') as AssistantResolutionPath,
    llmInvoked: response.llmInvoked ?? false,
    steps: response.steps?.map((step) => ({
      step: step.step ?? 0,
      toolId: step.toolId ?? '',
      sourceTables: step.sourceTables ?? [],
      success: step.success ?? false,
    })),
  };
}
