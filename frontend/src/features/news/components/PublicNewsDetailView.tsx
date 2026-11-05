import { ChevronLeft } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { PublicNewsDetailResponse } from '../../../api/model';
import { campusPhotoAt } from '../../home/campusPhotos';
import { formatNewsDate, newsCategoryLabel } from '../labels';
import { NewsMarkdownBody } from './NewsMarkdownBody';

function photoIndexFromSlug(slug: string | undefined): number {
  if (slug == null || slug.length === 0) {
    return 0;
  }
  let hash = 0;
  for (const char of slug) {
    hash = (hash + char.charCodeAt(0)) % 3;
  }
  return hash;
}

export type PublicNewsDetailViewProps = {
  article: PublicNewsDetailResponse | null;
  isLoading: boolean;
  error: string | null;
};

export function PublicNewsDetailView({ article, isLoading, error }: PublicNewsDetailViewProps) {
  return (
    <main className="bg-gray-50">
      <div className="mx-auto max-w-3xl space-y-6 px-4 py-10 md:px-6">
        <p className="text-body-md text-gray-500">
          <Link to="/" className="text-primary-700 hover:text-primary-500">
            Inicio
          </Link>
          <span className="mx-2 text-gray-400">/</span>
          <Link to="/public/news" className="text-primary-700 hover:text-primary-500">
            Noticias
          </Link>
          {article?.title ? (
            <>
              <span className="mx-2 text-gray-400">/</span>
              <span>{article.title}</span>
            </>
          ) : null}
        </p>

        <Link
          to="/public/news"
          className="inline-flex items-center gap-1 text-body-md font-medium text-primary-700 hover:text-primary-500"
        >
          <ChevronLeft size={16} aria-hidden />
          Todas las noticias
        </Link>

        {isLoading ? <p className="text-body-md text-gray-600">Cargando noticia…</p> : null}
        {error ? (
          <p role="alert" className="text-body-md text-danger">
            {error}
          </p>
        ) : null}
        {article != null ? (
          <article className="overflow-hidden rounded-2xl border border-gray-200 bg-body shadow-sm">
            <img
              src={campusPhotoAt(photoIndexFromSlug(article.slug)).src}
              alt=""
              className="h-56 w-full object-cover md:h-72"
            />
            <div className="p-6 md:p-8">
              <p className="text-label-md font-medium uppercase tracking-wide text-secondary-600">
                {newsCategoryLabel(article.category)}
              </p>
              <h1 className="mt-2 text-heading-xl text-primary-800">{article.title}</h1>
              <p className="mt-2 text-body-md text-gray-500">{formatNewsDate(article.publishedAt)}</p>
              <p className="mt-4 text-body-lg text-gray-700">{article.summary}</p>
              <div className="mt-6">
                <NewsMarkdownBody body={article.body ?? ''} />
              </div>
            </div>
          </article>
        ) : null}
      </div>
    </main>
  );
}
