import { useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Sidebar } from '../../../../components/layout/Sidebar';
import { Alert } from '../../../../components/ui/Alert';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import { TemplateEditorFormUI } from '../components/TemplateEditorFormUI';
import { useTemplateActions } from '../hooks/useTemplateActions';
import { useTemplateEditor } from '../hooks/useTemplateEditor';

export function TemplateEditorPage() {
  const navigate = useNavigate();
  const { templateId } = useParams<{ templateId: string }>();
  const [actionError, setActionError] = useState<string | null>(null);
  const editor = useTemplateEditor(templateId);
  const actions = useTemplateActions(templateId);

  const handleSave = async () => {
    setActionError(null);
    const saved = await editor.saveTemplate();
    if (!saved && !editor.submitError) {
      setActionError('No fue posible guardar la plantilla.');
    }
  };

  const handleAction = async (action: 'publish' | 'archive' | 'duplicate' | 'delete') => {
    if (!templateId) {
      setActionError('Guarde la plantilla antes de ejecutar esta acción.');
      return;
    }

    if (action !== 'duplicate') {
      const saved = await editor.saveTemplate();
      if (!saved) {
        return;
      }
    }

    setActionError(null);
    const result = await actions.runAction(action, templateId);

    if (!result.ok) {
      setActionError(result.message);
      return;
    }

    if (action === 'delete') {
      navigate('/admin/plantillas');
      return;
    }

    if (action === 'duplicate' && result.duplicateId) {
      navigate(`/admin/plantillas/${result.duplicateId}`);
    }
  };

  const pageTitle = editor.isEditMode ? 'Editar plantilla' : 'Nueva plantilla';

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="templates" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="border-b border-gray-200 bg-body px-8 py-4">
          <div className="text-body-md text-gray-500">
            <span className="text-primary-600">Inicio</span> / Plantillas / {pageTitle}
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-5xl space-y-6">
            <div>
              <div className="mb-4 h-1 w-12 bg-secondary" />
              <h1 className="text-heading-xl text-primary-800">{pageTitle}</h1>
              <p className="mt-2 text-body-lg text-gray-600">
                Configure fases y subfases con enlaces HTTPS. Publicar habilita la plantilla en
                «Nuevo proceso».
              </p>
            </div>

            {(editor.submitError || actionError) && (
              <Alert variant="error">{editor.submitError ?? actionError}</Alert>
            )}

            {editor.isError && (
              <Alert variant="error">
                {editor.loadError ? getApiErrorMessage(editor.loadError) : 'No fue posible cargar la plantilla.'}
              </Alert>
            )}

            {editor.isLoading && (
              <p className="text-body-md text-gray-500">Cargando plantilla…</p>
            )}

            {!editor.isLoading && editor.isHydrated && (
              <TemplateEditorFormUI
                form={editor.form}
                fieldErrors={editor.fieldErrors}
                status={editor.status}
                isSaving={editor.isSaving}
                onFormChange={editor.setForm}
                onSave={() => void handleSave()}
                onPublish={() => void handleAction('publish')}
                onArchive={() => void handleAction('archive')}
                onDuplicate={() => void handleAction('duplicate')}
                onDelete={() => void handleAction('delete')}
                onCancel={() => navigate('/admin/plantillas')}
              />
            )}
          </div>
        </main>
      </div>
    </div>
  );
}
