import { Settings } from 'lucide-react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { NotificationBellPopover } from '../../notifications';
import { NewsAdminView } from '../components/NewsAdminView';
import { useNewsAdmin } from '../hooks/useNewsAdmin';

export function NewsAdminPage() {
  const admin = useNewsAdmin();

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeNav="news" />
      <div className="flex flex-1 flex-col overflow-hidden">
        <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
          <div className="text-body-md text-gray-500">
            <span className="text-primary-600">Inicio</span> / Noticias
          </div>
          <div className="flex items-center gap-4 text-gray-600">
            <NotificationBellPopover />
            <button type="button" className="hover:text-primary-600" aria-label="Configuración">
              <Settings size={24} />
            </button>
          </div>
        </header>
        <main data-app-scroll className="flex-1 overflow-y-auto p-8">
          <div className="mx-auto max-w-7xl">
            <NewsAdminView
              items={admin.items}
              page={admin.page}
              totalPages={admin.totalPages}
              onPageChange={admin.setPage}
              isLoading={admin.isLoading}
              listError={admin.listError}
              actionError={admin.actionError}
              form={admin.form}
              onFormChange={admin.setForm}
              isFormOpen={admin.isFormOpen}
              editingId={admin.editingId}
              isMutating={admin.isMutating}
              onOpenCreate={admin.openCreate}
              onOpenEdit={admin.openEdit}
              onCloseForm={admin.closeForm}
              onSave={() => {
                void admin.saveForm();
              }}
              onPublish={(id) => {
                void admin.publish(id);
              }}
              onUnpublish={(id) => {
                void admin.unpublish(id);
              }}
              onFeature={(id) => {
                void admin.feature(id);
              }}
              onDelete={(id) => {
                void admin.remove(id);
              }}
            />
          </div>
        </main>
      </div>
    </div>
  );
}
