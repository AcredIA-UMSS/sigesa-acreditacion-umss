import { AssistantCopilotActionDebugModal } from '../../../assistant/components/AssistantCopilotActionDebugModal';
import type { CopilotAgentAction } from '../../../assistant/types/copilotAgentAction';

interface UsersCopilotActionDebugModalProps {
  isOpen: boolean;
  isSending: boolean;
  actions: CopilotAgentAction[];
  onClose: () => void;
}

export function UsersCopilotActionDebugModal({
  isOpen,
  isSending,
  actions,
  onClose,
}: UsersCopilotActionDebugModalProps) {
  return (
    <AssistantCopilotActionDebugModal
      isOpen={isOpen}
      isSending={isSending}
      actions={actions}
      onClose={onClose}
      title="Acciones del agente de usuarios"
      titleId="users-debug-modal-title"
    />
  );
}
