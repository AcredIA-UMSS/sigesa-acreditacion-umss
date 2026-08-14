import { Sidebar } from '../../components/layout/Sidebar';
import { useAuth } from '../../lib/auth/useAuth';
import { EvidenceCopilotPanel } from './components/EvidenceCopilotPanel';
import { EvidenceUploadUI } from './components/EvidenceUploadUI';
import { useEvidenceUpload } from './hooks/useEvidenceUpload';
import { useUploadableIndicators } from './hooks/useUploadableIndicators';

export function EvidenceUploadPage() {
  const upload = useEvidenceUpload();
  const uploadable = useUploadableIndicators();
  const { session } = useAuth();
  const role = session?.role;
  const showEvidenceCopilot =
    role === 'CC' || role === 'TD' || role === 'JD';

  const uploadUi = (
    <EvidenceUploadUI
      form={upload.form}
      onFieldChange={upload.updateField}
      onSelectIndicator={upload.selectIndicator}
      uploadableIndicators={uploadable.indicators}
      indicatorsLoading={uploadable.isLoading}
      indicatorsError={uploadable.errorMessage}
      indicatorsEmpty={uploadable.isEmpty}
      onReloadIndicators={uploadable.reload}
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
  );

  return (
    <div className="flex min-h-screen bg-gray-50">
      <Sidebar activeNav="evidence" />
      <div className="flex min-w-0 flex-1 flex-col">
        {showEvidenceCopilot ? (
          <div className="grid flex-1 gap-0 xl:grid-cols-[minmax(0,1fr)_340px]">
            {uploadUi}
            <div className="border-l border-gray-200 bg-gray-50 p-4 xl:p-6">
              <EvidenceCopilotPanel />
            </div>
          </div>
        ) : (
          uploadUi
        )}
      </div>
    </div>
  );
}
