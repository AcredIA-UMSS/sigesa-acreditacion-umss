import { AssistantCopilotActionDebugModal } from './AssistantCopilotActionDebugModal';
import type { CopilotAgentAction } from '../types/copilotAgentAction';

interface AssistantActionDebugModalProps {
  isOpen: boolean;
  isSending: boolean;
  actions: CopilotAgentAction[];
  onClose: () => void;
  showDevBadge: boolean;
}

export function AssistantActionDebugModal({
  isOpen,
  isSending,
  actions,
  onClose,
  showDevBadge,
}: AssistantActionDebugModalProps) {
  return (
    <AssistantCopilotActionDebugModal
      isOpen={isOpen}
      isSending={isSending}
      actions={actions}
      onClose={onClose}
      title="Historial de acciones del asistente"
      titleId="assistant-debug-modal-title"
      description="Trazabilidad de tools, camino, fuentes y pasos encadenados (Nivel 4)."
      showDevBadge={showDevBadge}
    />
  );
}
