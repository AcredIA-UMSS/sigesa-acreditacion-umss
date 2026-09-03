import { useEvidenceCopilot } from '../hooks/useEvidenceCopilot';
import { DomainCopilotFloatingChat } from '../../assistant/components/domain-copilot/DomainCopilotFloatingChat';
import { EvidenceCopilotActionDebugModal } from './EvidenceCopilotActionDebugModal';

function sanitizeSampleQuestion(question: string): string {
  return question
    .replaceAll('<indicatorId>', 'seleccionado')
    .replaceAll(
      /\b[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}\b/gi,
      'seleccionado',
    )
    .replaceAll(/\s{2,}/g, ' ')
    .trim();
}

export function EvidenceCopilotPanel({ programId }: { programId?: string }) {
  const copilot = useEvidenceCopilot(programId);

  if (copilot.isForbidden) {
    return null;
  }

  return (
    <DomainCopilotFloatingChat
      kind="evidence"
      archiveContextKey={programId ?? 'global'}
      title="Evidencias"
      subtitle="Control documental y revisión"
      placeholder="Ej.: ¿Qué evidencias están pendientes de revisión?"
      emptyStateMessage="Consulte documentación cargada por el coordinador."
      sampleQuestionSanitizer={sanitizeSampleQuestion}
      copilot={copilot}
      onOpenActionHistory={() => copilot.setActionModalOpen(true)}
      actionHistoryCount={copilot.actionHistory.length}
      debugModal={
        <EvidenceCopilotActionDebugModal
          isOpen={copilot.actionModalOpen}
          isSending={copilot.isSending}
          actions={copilot.actionHistory}
          onClose={() => copilot.setActionModalOpen(false)}
          showDevBadge={copilot.showDevBadge}
        />
      }
    />
  );
}
