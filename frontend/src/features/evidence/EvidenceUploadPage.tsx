import { Sidebar } from '../../components/layout/Sidebar';
import { EvidenceUploadUI } from './components/EvidenceUploadUI';
import { useEvidenceUpload } from './hooks/useEvidenceUpload';

export function EvidenceUploadPage() {
  const upload = useEvidenceUpload();

  return (
    <div className="flex h-screen bg-body">
      <Sidebar />
      <EvidenceUploadUI
        form={upload.form}
        onFieldChange={upload.updateField}
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
    </div>
  );
}
