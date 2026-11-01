import { Globe, MapPin, Share2, University } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../lib/auth/useAuth';

export function HomeFooter() {
  const { isAuthenticated } = useAuth();
  const faqTo = isAuthenticated ? '/ayuda' : '/login';
  return (
    <footer className="bg-primary-800 text-primary-100">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 md:grid-cols-2 md:px-6 lg:grid-cols-4">
        <div>
          <div className="mb-3 flex items-center gap-2 text-body">
            <University size={28} aria-hidden />
            <span className="text-heading-sm font-semibold">Universidad Mayor de San Simón</span>
          </div>
          <p className="text-body-md text-primary-200">
            Dirección Universitaria de Evaluación y Acreditación
          </p>
          <div className="mt-4 flex gap-3">
            <Globe size={18} aria-hidden />
            <Share2 size={18} aria-hidden />
          </div>
        </div>

        <div>
          <h2 className="text-label-md font-medium uppercase tracking-wide text-body">Explorar</h2>
          <ul className="mt-3 space-y-2 text-body-md">
            <li>
              <a href="#carreras" className="hover:text-body">
                Carreras Acreditadas
              </a>
            </li>
            <li>
              <a href="#modalidades" className="hover:text-body">
                Modalidades
              </a>
            </li>
            <li>
              <Link to={faqTo} className="hover:text-body">
                Preguntas Frecuentes
              </Link>
            </li>
          </ul>
        </div>

        <div>
          <h2 className="text-label-md font-medium uppercase tracking-wide text-body">Siguenos</h2>
          <ul className="mt-3 space-y-2 text-body-md">
            {/* TODO: URLs institucionales reales de redes DUEA/UMSS. */}
            <li>
              <a href="#" className="inline-flex items-center gap-2 hover:text-body">
                <Share2 size={16} aria-hidden /> Facebook
              </a>
            </li>
            <li>
              <a href="#" className="inline-flex items-center gap-2 hover:text-body">
                <Share2 size={16} aria-hidden /> WhatsApp
              </a>
            </li>
            <li>
              <a href="#" className="inline-flex items-center gap-2 hover:text-body">
                <Globe size={16} aria-hidden /> LinkedIn
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h2 className="text-label-md font-medium uppercase tracking-wide text-body">Sede central</h2>
          <p className="mt-3 flex items-start gap-2 text-body-md">
            <MapPin size={16} className="mt-0.5 shrink-0" aria-hidden />
            Jordan y Oquendo. Edificio de Rectorado - 3er piso.
          </p>
          <div
            className="home-map-placeholder mt-4 h-24 rounded-xl border border-primary-600"
            role="img"
            aria-label="Mapa de la sede central en Rectorado"
          />
        </div>
      </div>
      <p className="border-t border-primary-700 py-4 text-center text-label-md text-primary-200">
        © 2026 - AcredIA
      </p>
    </footer>
  );
}
