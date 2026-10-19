import { CircleHelp, GraduationCap, Menu, University, X } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../lib/auth/useAuth';

const NAV_LINKS = [
  { href: '#inicio', label: 'Inicio', current: true },
  { href: '#modalidades', label: 'Modalidades', current: false },
  { href: '#carreras', label: 'Carreras', current: false },
  { href: '#noticias', label: 'Noticias', current: false },
] as const;

export function HomeHeader() {
  const [menuOpen, setMenuOpen] = useState(false);
  const { isAuthenticated } = useAuth();
  const helpTo = isAuthenticated ? '/ayuda' : '/login';

  return (
    <header className="sticky top-0 z-40 border-b border-gray-200 bg-body">
      <div className="mx-auto flex max-w-6xl items-center justify-between gap-3 px-4 py-3 md:px-6">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 text-primary-700">
            <University size={28} aria-hidden />
            <span className="text-label-md font-semibold tracking-wide">DUEA</span>
          </div>
          <span className="h-8 w-px bg-gray-300" aria-hidden />
          <div className="flex items-center gap-2 text-primary-700">
            <GraduationCap size={28} aria-hidden />
            <span className="text-label-md font-semibold tracking-wide">UMSS</span>
          </div>
        </div>

        <nav className="hidden items-center gap-6 lg:flex" aria-label="Secciones">
          {NAV_LINKS.map((item) => (
            <a
              key={item.href}
              href={item.href}
              className={
                item.current
                  ? 'border-b-2 border-primary-700 pb-0.5 text-body-md font-bold text-primary-800'
                  : 'text-body-md text-gray-700 hover:text-primary-700'
              }
              aria-current={item.current ? 'page' : undefined}
            >
              {item.label}
            </a>
          ))}
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
            {NAV_LINKS.map((item) => (
              <li key={item.href}>
                <a
                  href={item.href}
                  className="block py-1 text-body-md text-primary-800"
                  onClick={() => setMenuOpen(false)}
                >
                  {item.label}
                </a>
              </li>
            ))}
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
