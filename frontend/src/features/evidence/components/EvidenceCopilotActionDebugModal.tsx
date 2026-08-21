import { AssistantCopilotActionDebugModal } from '../../assistant/components/AssistantCopilotActionDebugModal';
import type { CopilotAgentAction } from '../../assistant/types/copilotAgentAction';

interface EvidenceCopilotActionDebugModalProps {
  isOpen: boolean;
  isSending: boolean;
  actions: CopilotAgentAction[];
  onClose: () => void;
  showDevBadge?: boolean;
}

export function EvidenceCopilotActionDebugModal({
  isOpen,
  isSending,
  actions,
  onClose,
  showDevBadge = false,
}: EvidenceCopilotActionDebugModalProps) {
  return (
    <AssistantCopilotActionDebugModal
      isOpen={isOpen}
      isSending={isSending}
      actions={actions}
      onClose={onClose}
      title="Historial de acciones del agente"
      titleId="evidence-action-modal-title"
      description="Tools, camino y fuentes consultadas en esta sesión."
      showDevBadge={showDevBadge}
    />
  );
}
