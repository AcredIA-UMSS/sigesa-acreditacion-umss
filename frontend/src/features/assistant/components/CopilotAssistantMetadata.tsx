import type { AssistantMessageMetadata } from '../../../api/model/assistantTypes';

interface CopilotAssistantMetadataProps {
  metadata: AssistantMessageMetadata;
  compact?: boolean;
}

export function CopilotAssistantMetadata({
  metadata,
  compact = false,
}: CopilotAssistantMetadataProps) {
  const toolLabel = compact ? 'Tool' : 'Herramienta';
  const pathLabel = compact ? 'Camino' : 'Camino';

  return (
    <div
      className={`${compact ? 'mt-2 pt-2' : 'mt-3 pt-3'} border-t border-gray-200 text-label-md text-gray-600`}
    >
      <p>
        <span className="font-medium text-gray-800">{toolLabel}:</span>{' '}
        {metadata.toolId ?? '—'}
      </p>
      {!compact && metadata.sourceTables.length > 0 && (
        <p className="mt-1">
          <span className="font-medium text-gray-800">Fuente:</span>{' '}
          {metadata.sourceTables.join(', ')}
        </p>
      )}
      <p className={compact ? 'mt-0.5' : 'mt-1'}>
        <span className="font-medium text-gray-800">{pathLabel}:</span> {metadata.path}
        {metadata.llmInvoked ? ' · LLM invocado' : ''}
      </p>
      {metadata.steps && metadata.steps.length > 1 && (
        <div className={compact ? 'mt-1.5' : 'mt-2'}>
          <p className="font-medium text-gray-800">
            Traza ({metadata.steps.length} pasos):
          </p>
          <ol className="mt-1 list-inside list-decimal space-y-0.5">
            {metadata.steps.map((step) => (
              <li key={step.step}>
                {step.toolId}
                {step.success ? '' : ' (error)'}
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  );
}
