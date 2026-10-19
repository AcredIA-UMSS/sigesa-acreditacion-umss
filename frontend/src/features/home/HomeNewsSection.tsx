import { ExternalLink } from 'lucide-react';
import { Link } from 'react-router-dom';
import { HOME_NEWS } from './data';

export function HomeNewsSection() {
  const featured = HOME_NEWS.find((item) => item.featured);
  const rest = HOME_NEWS.filter((item) => !item.featured);

  return (
    <section id="noticias" className="bg-gray-50 px-4 py-16 md:px-6">
      <div className="mx-auto max-w-6xl">
        <div className="mb-8 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-heading-lg text-primary-800">Noticias y Destacados</h2>
            <p className="mt-2 max-w-xl text-body-md text-gray-600">
              Mantente al día con los últimos avances en acreditación universitaria.
            </p>
          </div>
          {/* TODO: no hay listado público de noticias; el enlace entra al login. */}
          <Link
            to="/login"
            className="inline-flex items-center gap-1 text-body-md font-medium text-primary-700 hover:text-primary-500"
          >
            Ver mas Noticias
            <ExternalLink size={16} aria-hidden />
          </Link>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          {featured ? (
            <article className="home-hero-photo home-hero-photo--campus relative min-h-64 overflow-hidden rounded-2xl p-6 shadow-sm">
              <div className="absolute inset-0 bg-primary-900/55" aria-hidden />
              <div className="relative z-10 flex h-full min-h-56 flex-col justify-end">
                <span className="mb-3 inline-flex w-fit rounded bg-secondary-600 px-2 py-0.5 text-label-md font-medium uppercase text-body">
                  {featured.category}
                </span>
                <h3 className="text-heading-sm text-body">{featured.title}</h3>
                <p className="mt-2 text-body-md text-primary-50">{featured.summary}</p>
              </div>
            </article>
          ) : null}

          <ul className="flex flex-col gap-4">
            {rest.map((item) => (
              <li
                key={item.id}
                className="flex gap-4 rounded-2xl border border-gray-200 bg-body p-4 shadow-sm"
              >
                <div
                  className="home-hero-photo home-hero-photo--group h-20 w-24 shrink-0 rounded-xl"
                  aria-hidden
                />
                <div>
                  <p className="text-label-md font-medium uppercase tracking-wide text-secondary-600">
                    {item.category}
                  </p>
                  <h3 className="mt-1 text-heading-sm text-primary-800">{item.title}</h3>
                  <p className="mt-1 text-body-md text-gray-600">{item.summary}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  );
}
