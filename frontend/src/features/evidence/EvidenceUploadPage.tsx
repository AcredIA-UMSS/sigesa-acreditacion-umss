import { Sidebar } from '../../components/layout/Sidebar';
import { useAuth } from '../../lib/auth/useAuth';
import { EvidenceCopilotPanel } from './components/EvidenceCopilotPanel';
import { EvidenceUploadUI } from './components/EvidenceUploadUI';
import { useEvidenceUpload } from './hooks/useEvidenceUpload';
import { useSubphaseUploadTargets } from './hooks/useSubphaseUploadTargets';

export function EvidenceUploadPage() {
  const upload = useEvidenceUpload();
  const targets = useSubphaseUploadTargets(
    upload.form.processId.trim() || undefined,
  );
  const { session } = useAuth();
  const role = session?.role;
  const showEvidenceCopilot =
    role === 'CC' || role === 'TD' || role === 'JD';

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar activeNav="evidence" />
      <div className="flex min-w-0 flex-1 flex-col">
        <EvidenceUploadUI
          form={upload.form}
          onFieldChange={upload.updateField}
          processOptions={targets.processOptions}
          subphaseOptions={targets.subphaseOptions}
          targetsLoading={targets.isLoading}
          targetsError={targets.errorMessage}
          targetsEmpty={targets.isEmpty}
          subphasesEmpty={targets.subphasesEmpty}
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
        />
        {showEvidenceCopilot ? (
          <EvidenceCopilotPanel programId={targets.selectedProcess?.careerId} />
        ) : null}
      </div>
    </div>
  );
}
