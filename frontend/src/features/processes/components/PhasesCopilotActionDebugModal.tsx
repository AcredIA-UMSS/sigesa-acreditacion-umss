import { AssistantCopilotActionDebugModal } from '../../assistant/components/AssistantCopilotActionDebugModal';
import type { CopilotAgentAction } from '../../assistant/types/copilotAgentAction';

interface PhasesCopilotActionDebugModalProps {
  isOpen: boolean;
  isSending: boolean;
  actions: CopilotAgentAction[];
  onClose: () => void;
}

export function PhasesCopilotActionDebugModal({
  isOpen,
  isSending,
  actions,
  onClose,
}: PhasesCopilotActionDebugModalProps) {
  return (
    <AssistantCopilotActionDebugModal
      isOpen={isOpen}
      isSending={isSending}
      actions={actions}
      onClose={onClose}
      title="Acciones del agente de fases"
      titleId="phases-debug-modal-title"
    />
  );
}
