import { Link } from 'react-router-dom';
import type { PublicNewsDetailResponse } from '../../../api/model';
import { formatNewsDate, newsCategoryLabel } from '../labels';
import { NewsMarkdownBody } from './NewsMarkdownBody';

export type PublicNewsDetailViewProps = {
  article: PublicNewsDetailResponse | null;
  isLoading: boolean;
  error: string | null;
};

export function PublicNewsDetailView({ article, isLoading, error }: PublicNewsDetailViewProps) {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="border-b border-gray-200 bg-body">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 md:px-6">
          <Link to="/public/news" className="text-body-md font-medium text-primary-700">
            Volver al listado
          </Link>
          <Link to="/" className="text-body-md font-medium text-primary-700">
            Inicio
          </Link>
        </div>
      </header>

      <main className="mx-auto max-w-3xl space-y-6 px-4 py-8 md:px-6">
        {isLoading ? <p className="text-body-md text-gray-600">Cargando noticia…</p> : null}
        {error ? (
          <p role="alert" className="text-body-md text-danger">
            {error}
          </p>
        ) : null}
        {article != null ? (
          <article className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
            <p className="text-label-md font-medium uppercase tracking-wide text-secondary-600">
              {newsCategoryLabel(article.category)}
            </p>
            <h1 className="mt-2 text-heading-xl text-primary-800">{article.title}</h1>
            <p className="mt-2 text-body-md text-gray-500">{formatNewsDate(article.publishedAt)}</p>
            <p className="mt-4 text-body-lg text-gray-700">{article.summary}</p>
            <div className="mt-6">
              <NewsMarkdownBody body={article.body ?? ''} />
            </div>
          </article>
        ) : null}
      </main>
    </div>
  );
}
