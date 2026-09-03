import { useUsersCopilot } from '../hooks/useUsersCopilot';
import { DomainCopilotFloatingChat } from '../../../assistant/components/domain-copilot/DomainCopilotFloatingChat';
import { UsersCopilotActionDebugModal } from './UsersCopilotActionDebugModal';

export function UsersCopilotPanel() {
  const copilot = useUsersCopilot();

  if (copilot.isForbidden) {
    return null;
  }

  return (
    <DomainCopilotFloatingChat
      kind="users"
      archiveContextKey="admin-users"
      title="Administración [JD]"
      subtitle="Alta, detalle, estado y asignación vía chat"
      placeholder="Ej.: Lista los usuarios CC activos"
      emptyStateMessage="Gestione usuarios con lenguaje natural. Las bajas requieren confirmación explícita."
      copilot={copilot}
      debugModal={
        copilot.debugActionsEnabled ? (
          <UsersCopilotActionDebugModal
            isOpen={copilot.debugModalOpen}
            isSending={copilot.isSending}
            actions={copilot.actionHistory}
            onClose={() => copilot.setDebugModalOpen(false)}
          />
        ) : null
      }
    />
  );
}
