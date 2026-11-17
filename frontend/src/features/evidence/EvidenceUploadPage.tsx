import { useListStages } from '../../api/endpoints/stage-workflow/stage-workflow';
import { Sidebar } from '../../components/layout/Sidebar';
import { useAuth } from '../../lib/auth/useAuth';
import { currentStage } from '../reports/phase/arcuPhaseGuide';
import { EvidenceCopilotPanel } from './components/EvidenceCopilotPanel';
import { ProcessEvidenceTrace } from '../observations/components/ProcessEvidenceTrace';
import { EvidenceUploadUI } from './components/EvidenceUploadUI';
import { useEvidenceUpload } from './hooks/useEvidenceUpload';
import { useNormativeIndicatorUploadTargets } from './hooks/useNormativeIndicatorUploadTargets';

export function EvidenceUploadPage() {
  const upload = useEvidenceUpload();
  const processId = upload.form.processId.trim();
  const targets = useNormativeIndicatorUploadTargets(processId || undefined);
  const stagesQuery = useListStages(processId, {
    query: { enabled: Boolean(processId) },
  });
  const stages = stagesQuery.data?.status === 200 ? stagesQuery.data.data : [];
  const phaseName = currentStage(stages)?.displayName ?? null;
  const { session } = useAuth();
  const role = session?.role;
  const showEvidenceCopilot =
    role === 'CC' || role === 'TD' || role === 'JD';

  return (
    <div className="flex h-full min-h-0 overflow-hidden bg-gray-50">
      <Sidebar activeNav="evidence" />
      <div className="flex min-h-0 min-w-0 flex-1 flex-col overflow-hidden">
        <EvidenceUploadUI
          form={upload.form}
          onFieldChange={upload.updateField}
          processOptions={targets.processOptions}
          indicatorOptions={targets.indicatorOptions}
          targetsLoading={targets.isLoading}
          targetsError={targets.errorMessage}
          targetsEmpty={targets.isEmpty}
          indicatorsEmpty={targets.indicatorsEmpty}
          onReloadTargets={targets.reload}
          onSubmit={upload.submit}
          onReset={upload.reset}
          progress={upload.progress}
          isLargeFile={upload.isLargeFile}
          isSubmitting={upload.isSubmitting}
          isBlocked={upload.isBlocked}
          result={upload.result}
          errorMessage={upload.errorMessage}
          validationErrors={upload.validationErrors}
          phaseName={phaseName}
          trace={processId
            ? (
              <section className="mt-6 rounded-2xl border border-gray-100 bg-body p-8 shadow-sm">
                <h2 className="text-heading-md text-primary-800">Trazabilidad con Seguimiento de procesos</h2>
                <p className="mt-1 mb-4 text-body-md text-gray-600">
                  Las observaciones que el técnico envía desde Seguimiento de procesos quedan sobre la evidencia cargada aquí.
                </p>
                <ProcessEvidenceTrace processId={processId} mode="coordinator" />
              </section>
            )
            : null}
        />
        {showEvidenceCopilot ? (
          <EvidenceCopilotPanel programId={targets.selectedProcess?.careerId} />
        ) : null}
      </div>
    </div>
  );
}
