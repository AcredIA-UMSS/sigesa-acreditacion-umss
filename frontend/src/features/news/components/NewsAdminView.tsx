import type { AdminNewsResponse } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';
import type { NewsFormState } from '../hooks/useNewsAdmin';
import { NEWS_CATEGORIES, formatNewsDate, newsCategoryLabel, newsStatusLabel } from '../labels';

export type NewsAdminViewProps = {
  items: AdminNewsResponse[];
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  isLoading: boolean;
  listError: string | null;
  actionError: string | null;
  form: NewsFormState;
  onFormChange: (form: NewsFormState) => void;
  isFormOpen: boolean;
  editingId: string | null;
  isMutating: boolean;
  onOpenCreate: () => void;
  onOpenEdit: (article: AdminNewsResponse) => void;
  onCloseForm: () => void;
  onSave: () => void;
  onPublish: (id: string) => void;
  onUnpublish: (id: string) => void;
  onFeature: (id: string) => void;
  onDelete: (id: string) => void;
};

export function NewsAdminView({
  items,
  page,
  totalPages,
  onPageChange,
  isLoading,
  listError,
  actionError,
  form,
  onFormChange,
  isFormOpen,
  editingId,
  isMutating,
  onOpenCreate,
  onOpenEdit,
  onCloseForm,
  onSave,
  onPublish,
  onUnpublish,
  onFeature,
  onDelete,
}: NewsAdminViewProps) {
  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="mb-4 h-1 w-12 bg-secondary" />
          <h1 className="text-heading-xl text-primary-800">Noticias y destacados</h1>
          <p className="mt-2 text-body-lg text-gray-600">
            Cree borradores, publique y marque una sola noticia destacada para la home.
          </p>
        </div>
        <Button variant="primary" onClick={onOpenCreate}>
          Nueva noticia
        </Button>
      </div>

      {actionError ? (
        <div className="rounded-lg border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
          {actionError}
        </div>
      ) : null}
      {listError ? (
        <p role="alert" className="text-body-md text-danger">
          {listError}
        </p>
      ) : null}

      {isFormOpen ? (
        <form
          className="space-y-4 rounded-2xl border border-gray-200 bg-body p-6 shadow-sm"
          onSubmit={(event) => {
            event.preventDefault();
            onSave();
          }}
        >
          <h2 className="text-heading-md text-primary-800">
            {editingId == null ? 'Crear borrador' : 'Editar noticia'}
          </h2>
          <TextInput
            id="news-title"
            label="Título"
            requiredMark
            value={form.title}
            onChange={(event) => onFormChange({ ...form, title: event.target.value })}
          />
          <label className="block space-y-1" htmlFor="news-category">
            <span className="block text-label-md text-gray-700">
              Categoría <span className="text-secondary">*</span>
            </span>
            <select
              id="news-category"
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900"
              value={form.category}
              onChange={(event) => onFormChange({ ...form, category: event.target.value })}
            >
              {NEWS_CATEGORIES.map((category) => (
                <option key={category} value={category}>
                  {newsCategoryLabel(category)}
                </option>
              ))}
            </select>
          </label>
          <TextInput
            id="news-summary"
            label="Resumen"
            requiredMark
            value={form.summary}
            onChange={(event) => onFormChange({ ...form, summary: event.target.value })}
          />
          <label className="block space-y-1" htmlFor="news-body">
            <span className="block text-label-md text-gray-700">Cuerpo (texto o Markdown)</span>
            <textarea
              id="news-body"
              className="min-h-40 w-full rounded-lg border border-gray-300 bg-body px-3 py-3 text-body-md text-gray-900"
              value={form.body}
              onChange={(event) => onFormChange({ ...form, body: event.target.value })}
            />
          </label>
          <div className="flex gap-3">
            <Button type="submit" variant="primary" isLoading={isMutating}>
              Guardar
            </Button>
            <Button type="button" variant="ghost" onClick={onCloseForm}>
              Cancelar
            </Button>
          </div>
        </form>
      ) : null}

      {isLoading ? <p className="text-body-md text-gray-600">Cargando noticias…</p> : null}

      <div className="overflow-x-auto rounded-2xl border border-gray-200 bg-body shadow-sm">
        <table className="min-w-full text-left">
          <thead className="bg-gray-50 text-label-md uppercase text-gray-600">
            <tr>
              <th className="px-4 py-3">Título</th>
              <th className="px-4 py-3">Estado</th>
              <th className="px-4 py-3">Categoría</th>
              <th className="px-4 py-3">Publicación</th>
              <th className="px-4 py-3">Acciones</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item) => (
              <tr key={item.id} className="border-t border-gray-100">
                <td className="px-4 py-3 text-body-md text-primary-800">
                  {item.title}
                  {item.featured ? (
                    <span className="ml-2 rounded bg-secondary-100 px-2 py-0.5 text-label-md text-secondary-700">
                      Destacada
                    </span>
                  ) : null}
                </td>
                <td className="px-4 py-3 text-body-md text-gray-700">{newsStatusLabel(item.status)}</td>
                <td className="px-4 py-3 text-body-md text-gray-700">{newsCategoryLabel(item.category)}</td>
                <td className="px-4 py-3 text-body-md text-gray-600">{formatNewsDate(item.publishedAt)}</td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-2">
                    <Button variant="ghost" className="px-3 py-2" onClick={() => onOpenEdit(item)}>
                      Editar
                    </Button>
                    {item.status === 'BORRADOR' ? (
                      <>
                        <Button
                          variant="primary"
                          className="px-3 py-2"
                          disabled={item.id == null || isMutating}
                          onClick={() => item.id != null && onPublish(item.id)}
                        >
                          Publicar
                        </Button>
                        <Button
                          variant="danger"
                          className="px-3 py-2"
                          disabled={item.id == null || isMutating}
                          onClick={() => item.id != null && onDelete(item.id)}
                        >
                          Eliminar
                        </Button>
                      </>
                    ) : (
                      <>
                        <Button
                          variant="ghost"
                          className="px-3 py-2"
                          disabled={item.id == null || isMutating || item.featured}
                          onClick={() => item.id != null && onFeature(item.id)}
                        >
                          Destacar
                        </Button>
                        <Button
                          variant="secondary"
                          className="px-3 py-2"
                          disabled={item.id == null || isMutating}
                          onClick={() => item.id != null && onUnpublish(item.id)}
                        >
                          Despublicar
                        </Button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {totalPages > 1 ? (
        <div className="flex items-center gap-3">
          <button
            type="button"
            className="text-body-md font-medium text-primary-700 disabled:text-gray-400"
            disabled={page === 0}
            onClick={() => onPageChange(page - 1)}
          >
            Anterior
          </button>
          <span className="text-body-md text-gray-600">
            Página {page + 1} de {totalPages}
          </span>
          <button
            type="button"
            className="text-body-md font-medium text-primary-700 disabled:text-gray-400"
            disabled={page + 1 >= totalPages}
            onClick={() => onPageChange(page + 1)}
          >
            Siguiente
          </button>
        </div>
      ) : null}
    </div>
  );
}
