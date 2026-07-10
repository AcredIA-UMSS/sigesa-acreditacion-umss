import { Sidebar } from '../../components/layout/Sidebar';
import { CreateProcessUI } from './components/CreateProcessUI';
import { useCreateProcessForm } from './hooks/useCreateProcessForm';

export const CreateProcessPage = () => {
  const form = useCreateProcessForm();

  return (
    <div className="flex h-screen bg-body">
      <Sidebar activeNav="processes" />
      <CreateProcessUI
        careerId={form.form.careerId}
        templateId={form.form.templateId}
        period={form.form.period}
        fieldErrors={form.fieldErrors}
        submitError={form.submitError}
        successMessage={form.successMessage}
        isPending={form.isPending}
        isProgramsLoading={form.isProgramsLoading}
        isProgramsError={form.isProgramsError}
        isTemplatesLoading={form.isTemplatesLoading}
        isTemplatesError={form.isTemplatesError}
        programOptions={form.programOptions}
        templateOptions={form.templateOptions}
        periodOptions={form.periodOptions}
        selectedTemplate={form.selectedTemplate}
        onCareerIdChange={form.setCareerId}
        onTemplateIdChange={form.setTemplateId}
        onPeriodChange={form.setPeriod}
        onSubmit={form.handleSubmit}
        onCancel={form.handleCancel}
        onBack={form.handleCancel}
      />
    </div>
  );
};
