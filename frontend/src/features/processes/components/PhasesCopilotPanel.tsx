import type { PhasesCopilotProcessContext } from '../hooks/usePhasesCopilot';
import { usePhasesCopilot } from '../hooks/usePhasesCopilot';
import { DomainCopilotFloatingChat } from '../../assistant/components/domain-copilot/DomainCopilotFloatingChat';
import { PhasesCopilotActionDebugModal } from './PhasesCopilotActionDebugModal';

export interface PhasesCopilotPanelProps {
  process: PhasesCopilotProcessContext;
  readOnly?: boolean;
}

export function PhasesCopilotPanel({ process, readOnly = false }: PhasesCopilotPanelProps) {
  const copilot = usePhasesCopilot(process);

  return (
    <DomainCopilotFloatingChat
      kind="phases"
      archiveContextKey={process.processId}
      title={process.careerName}
      subtitle={`${process.careerCode} · ${process.templateType}`}
      readOnly={readOnly}
      placeholder={
        readOnly
          ? 'Ej.: Muestra la estructura con subfases'
          : 'Ej.: Lista las fases de este proceso'
      }
      emptyStateMessage={
        readOnly
          ? 'Consulte fases y subfases de su carrera asignada.'
          : 'Pregunte sobre las fases de este proceso. No necesita indicar la carrera.'
      }
      copilot={copilot}
      debugModal={
        copilot.debugActionsEnabled ? (
          <PhasesCopilotActionDebugModal
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
