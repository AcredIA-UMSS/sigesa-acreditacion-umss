import { Sidebar } from '../../components/layout/Sidebar';
import { ExecutiveReportUI } from './components/ExecutiveReportUI';
import { useExecutiveReport } from './hooks/useExecutiveReport';

export function ExecutiveReportPage() {
  const report = useExecutiveReport();

  return (
    <div className="flex h-screen bg-body">
      <Sidebar />
      <ExecutiveReportUI
        form={report.form}
        onFieldChange={report.updateField}
        onSubmit={report.submit}
        onDownload={report.download}
        onReset={report.reset}
        activeJobId={report.activeJobId}
        jobStatus={report.jobStatus}
        validationErrors={report.validationErrors}
        submitErrorMessage={report.submitErrorMessage}
        statusErrorMessage={report.statusErrorMessage}
        downloadErrorMessage={report.downloadErrorMessage}
        isSubmitting={report.isSubmitting}
        isDownloading={report.isDownloading}
        isPolling={report.isPolling}
        isBlocked={report.isBlocked}
      />
    </div>
  );
}
