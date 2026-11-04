import { CircleHelp, Menu, X } from 'lucide-react';
import { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import logoDuea from '../../assets/logo-duea.png';
import logoUmss from '../../assets/logo-umss.png';
import { useAuth } from '../../lib/auth/useAuth';

type HomeNavItem = {
  label: string;
  homeHref: string;
  to?: string;
};

const NAV_LINKS: HomeNavItem[] = [
  { label: 'Inicio', homeHref: '#inicio' },
  { label: 'Modalidades', homeHref: '#modalidades' },
  { label: 'Carreras', homeHref: '#carreras' },
  { label: 'Noticias', homeHref: '#noticias', to: '/public/news' },
];

function navClassName(current: boolean): string {
  return current
    ? 'border-b-2 border-primary-700 pb-0.5 text-body-md font-bold text-primary-800'
    : 'text-body-md text-gray-700 hover:text-primary-700';
}

export function HomeHeader() {
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const { isAuthenticated } = useAuth();
  const helpTo = isAuthenticated ? '/ayuda' : '/login';
  const isHome = location.pathname === '/';
  const isNews = location.pathname.startsWith('/public/news');

  const isCurrent = (item: HomeNavItem): boolean => {
    if (item.to != null) {
      return isNews;
    }
    if (item.homeHref === '#inicio') {
      return isHome;
    }
    return false;
  };

  const itemHref = (item: HomeNavItem): string => {
    if (item.to != null) {
      return item.to;
    }
    return isHome ? item.homeHref : `/${item.homeHref}`;
  };

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-body">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3 md:px-6">
        <Link to="/" className="flex items-center gap-3" aria-label="Ir al inicio">
          <img src={logoDuea} alt="" className="h-10 w-10 object-contain" />
          <span className="hidden text-label-md font-semibold tracking-wide text-primary-700 sm:inline">
            DUEA
          </span>
          <span className="h-8 w-px bg-gray-300" aria-hidden />
          <img src={logoUmss} alt="" className="h-10 w-auto object-contain" />
          <span className="hidden text-label-md font-semibold tracking-wide text-primary-700 sm:inline">
            UMSS
          </span>
        </Link>

        <nav className="hidden items-center gap-6 lg:flex" aria-label="Secciones">
          {NAV_LINKS.map((item) => {
            const href = itemHref(item);
            const current = isCurrent(item);
            if (item.to != null) {
              return (
                <Link
                  key={item.label}
                  to={href}
                  className={navClassName(current)}
                  aria-current={current ? 'page' : undefined}
                >
                  {item.label}
                </Link>
              );
            }
            return (
              <a
                key={item.label}
                href={href}
                className={navClassName(current)}
                aria-current={current ? 'page' : undefined}
              >
                {item.label}
              </a>
            );
          })}
        </nav>

        <div className="flex items-center gap-2">
          <Link
            to={helpTo}
            aria-label="Ayuda"
            className="inline-flex h-10 w-10 items-center justify-center rounded-full border border-gray-300 text-primary-700 hover:bg-primary-50 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600"
          >
            <CircleHelp size={20} />
          </Link>
          <Link
            to="/login"
            className="hidden rounded-lg bg-primary-700 px-4 py-2.5 text-label-md font-medium text-body hover:bg-primary-600 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-primary-600 sm:inline-flex"
          >
            Iniciar Sesión
          </Link>
          <button
            type="button"
            className="inline-flex h-10 w-10 items-center justify-center rounded-lg text-primary-800 lg:hidden"
            aria-expanded={menuOpen}
            aria-controls="home-mobile-nav"
            aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'}
            onClick={() => setMenuOpen((open) => !open)}
          >
            {menuOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>

      {menuOpen ? (
        <nav id="home-mobile-nav" className="border-t border-gray-200 bg-body px-4 py-3 lg:hidden" aria-label="Móvil">
          <ul className="flex flex-col gap-3">
            {NAV_LINKS.map((item) => {
              const href = itemHref(item);
              const className = 'block py-1 text-body-md text-primary-800';
              return (
                <li key={item.label}>
                  {item.to != null ? (
                    <Link to={href} className={className} onClick={() => setMenuOpen(false)}>
                      {item.label}
                    </Link>
                  ) : (
                    <a href={href} className={className} onClick={() => setMenuOpen(false)}>
                      {item.label}
                    </a>
                  )}
                </li>
              );
            })}
            <li>
              <Link to="/login" className="block py-1 text-body-md font-medium text-primary-700" onClick={() => setMenuOpen(false)}>
                Iniciar Sesión
              </Link>
            </li>
          </ul>
        </nav>
      ) : null}
    </header>
  );
}
