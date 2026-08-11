import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Sidebar } from '../../../../components/layout/Sidebar';
import { Alert } from '../../../../components/ui/Alert';
import { ConfirmDialog } from '../../../../components/ui/ConfirmDialog';
import { getApiErrorMessage } from '../../../../lib/api/mapApiError';
import { TemplatesListTableUI } from '../components/TemplatesListTableUI';
import { useTemplateActions } from '../hooks/useTemplateActions';
import { useTemplatesList } from '../hooks/useTemplatesList';
import type { TemplateListFilters } from '../lib/templateTypes';

export function TemplatesListPage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<TemplateListFilters>({ status: '', type: '' });
  const [actionError, setActionError] = useState<string | null>(null);
  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);
  const list = useTemplatesList(filters);
  const actions = useTemplateActions();

  const handleAction = async (
    action: 'publish' | 'archive' | 'duplicate' | 'delete',
    templateId: string,
  ) => {
    setActionError(null);
    const result = await actions.runAction(action, templateId);

    if (!result.ok) {
      setActionError(result.message);
      return;
    }

    if (action === 'duplicate' && result.duplicateId) {
      navigate(`/admin/plantillas/${result.duplicateId}`);
    }
  };

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="templates" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="border-b border-gray-200 bg-body px-8 py-4">
          <div className="text-body-md text-gray-500">
            <span className="text-primary-600">Inicio</span> / Plantillas normativas
          </div>
        </header>

        <main className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-6xl space-y-6">
            <div>
              <div className="mb-4 h-1 w-12 bg-secondary" />
              <h1 className="text-heading-xl text-primary-800">Gestión de plantillas</h1>
              <p className="mt-2 text-body-lg text-gray-600">
                Administre molde normativo CEUB/ARCU-SUR antes de instanciar procesos de acreditación.
              </p>
            </div>

            {actionError && <Alert variant="error">{actionError}</Alert>}

            <TemplatesListTableUI
              templates={list.templates}
              filters={filters}
              isLoading={list.isLoading}
              isError={list.isError}
              errorMessage={list.error ? getApiErrorMessage(list.error) : undefined}
              isActionBusy={actions.isBusy}
              onFiltersChange={setFilters}
              onPublish={(templateId) => void handleAction('publish', templateId)}
              onArchive={(templateId) => void handleAction('archive', templateId)}
              onDuplicate={(templateId) => void handleAction('duplicate', templateId)}
              onDelete={(templateId) => setConfirmDeleteId(templateId)}
            />
          </div>
        </main>
      </div>

      <ConfirmDialog
        isOpen={confirmDeleteId !== null}
        title="Eliminar plantilla"
        description="¿Eliminar esta plantilla en borrador? Esta acción no se puede deshacer."
        confirmLabel="Eliminar plantilla"
        isLoading={actions.isBusy}
        onClose={() => setConfirmDeleteId(null)}
        onConfirm={() => {
          if (confirmDeleteId) {
            void handleAction('delete', confirmDeleteId).finally(() => setConfirmDeleteId(null));
          }
        }}
      />
    </div>
  );
}
