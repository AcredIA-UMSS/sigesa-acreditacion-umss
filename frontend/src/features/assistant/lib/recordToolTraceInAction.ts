import type { SendChatMessageResponse } from '../../../api/model/assistantTypes';
import type { CopilotAgentActionStep } from '../types/copilotAgentAction';

type AppendActionStep = (
  actionId: string,
  step: Omit<CopilotAgentActionStep, 'id'>,
) => void;

export function recordToolTraceInAction(
  appendActionStep: AppendActionStep,
  actionId: string,
  response: SendChatMessageResponse,
): void {
  const apiSteps = response.steps ?? [];

  if (apiSteps.length > 1) {
    for (const step of apiSteps) {
      appendActionStep(actionId, {
        label: `Paso ${step.step}: ${step.toolId ?? 'tool'}${step.success ? '' : ' (error)'}`,
        kind: step.success ? 'success' : 'error',
      });
    }
  } else {
    appendActionStep(actionId, {
      label: `Tool ejecutada: ${response.toolId ?? 'ninguna'} (${response.path ?? 'LLM'})`,
      kind: 'success',
    });
  }

  if (response.llmInvoked) {
    appendActionStep(actionId, {
      label:
        apiSteps.length > 1
          ? `LLM invocado (${apiSteps.length} pasos encadenados)`
          : 'LLM invocado para selección de tool',
      kind: 'info',
    });
  }

  if (response.sourceTables && response.sourceTables.length > 0) {
    appendActionStep(actionId, {
      label: `Fuentes consultadas: ${response.sourceTables.join(', ')}`,
      kind: 'info',
    });
  }
}
