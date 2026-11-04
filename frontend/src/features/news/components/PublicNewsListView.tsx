import { Link } from 'react-router-dom';
import type { PublicNewsSummaryResponse } from '../../../api/model';
import { formatNewsDate, newsCategoryLabel } from '../labels';

export type PublicNewsListViewProps = {
  items: PublicNewsSummaryResponse[];
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  isLoading: boolean;
  error: string | null;
};

export function PublicNewsListView({
  items,
  page,
  totalPages,
  onPageChange,
  isLoading,
  error,
}: PublicNewsListViewProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-body">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 md:px-6">
          <Link to="/" className="text-body-md font-medium text-primary-700">
            Volver al inicio
          </Link>
          <Link to="/login" className="text-body-md font-medium text-primary-700">
            Iniciar sesión
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-6xl space-y-6 px-4 py-8 md:px-6">
        <div>
          <h1 className="text-heading-xl text-primary-800">Noticias y destacados</h1>
          <p className="mt-2 text-body-md text-gray-700">
            Comunicados publicados por la DUEA. Solo se muestra lo autorizado.
          </p>
        </div>

        {error ? (
          <p role="alert" className="text-body-md text-danger">
            {error}
          </p>
        ) : null}

        {isLoading ? (
          <p className="text-body-md text-gray-600">Cargando noticias…</p>
        ) : null}

        {!isLoading && items.length === 0 && error == null ? (
          <p className="text-body-md text-gray-600">No hay noticias publicadas por el momento.</p>
        ) : null}

        <ul className="space-y-4">
          {items.map((item) => (
            <li key={item.slug ?? item.title}>
              <Link
                to={`/public/news/${item.slug ?? ''}`}
                className="block rounded-2xl border border-gray-200 bg-body p-5 shadow-sm hover:border-primary-300"
              >
                <p className="text-label-md font-medium uppercase tracking-wide text-secondary-600">
                  {newsCategoryLabel(item.category)}
                </p>
                <h2 className="mt-1 text-heading-sm text-primary-800">{item.title}</h2>
                <p className="mt-2 text-body-md text-gray-600">{item.summary}</p>
                <p className="mt-2 text-label-md text-gray-500">{formatNewsDate(item.publishedAt)}</p>
              </Link>
            </li>
          ))}
        </ul>

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
      </main>
    </div>
  );
}
