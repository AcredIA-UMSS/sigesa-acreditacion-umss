import { MapPin } from 'lucide-react';
import { Link } from 'react-router-dom';
import logoDuea from '../../assets/logo-duea.png';
import logoUmss from '../../assets/logo-umss.png';
import { useAuth } from '../../lib/auth/useAuth';
import { FacebookIcon, LinkedInIcon, WhatsAppIcon } from './SocialBrandIcons';

export function HomeFooter() {
  const { isAuthenticated } = useAuth();
  const faqTo = isAuthenticated ? '/ayuda' : '/login';
  return (
    <footer className="bg-primary-800 text-primary-100">
      <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 md:grid-cols-2 md:px-6 lg:grid-cols-4">
        <div>
          <div className="mb-3 flex items-center gap-3 text-body">
            <img src={logoDuea} alt="" className="h-12 w-12 rounded-full object-contain" />
            <img src={logoUmss} alt="" className="h-12 w-auto object-contain" />
            <span className="text-heading-sm font-semibold">Universidad Mayor de San Simón</span>
          </div>
          <p className="text-body-md text-primary-200">
            Dirección Universitaria de Evaluación y Acreditación
          </p>
        </div>

        <div>
          <h2 className="text-label-md font-medium uppercase tracking-wide text-body">Explorar</h2>
          <ul className="mt-3 space-y-2 text-body-md">
            <li>
              <Link to="/public/status" className="hover:text-body">
                Estado de Acreditación
              </Link>
            </li>
            <li>
              <a href="/#carreras" className="hover:text-body">
                Carreras Acreditadas
              </a>
            </li>
            <li>
              <a href="/#modalidades" className="hover:text-body">
                Modalidades
              </a>
            </li>
            <li>
              <Link to="/public/news" className="hover:text-body">
                Noticias
              </Link>
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
                <FacebookIcon size={16} /> Facebook
              </a>
            </li>
            <li>
              <a href="#" className="inline-flex items-center gap-2 hover:text-body">
                <WhatsAppIcon size={16} /> WhatsApp
              </a>
            </li>
            <li>
              <a href="#" className="inline-flex items-center gap-2 hover:text-body">
                <LinkedInIcon size={16} /> LinkedIn
              </a>
            </li>
          </ul>
        </div>

        <div>
          <h2 className="text-label-md font-medium uppercase tracking-wide text-body">Sede central</h2>
          <a
            href="https://maps.app.goo.gl/ztYzQc2Ks7XuEnc88"
            target="_blank"
            rel="noopener noreferrer"
            className="mt-3 flex items-start gap-2 text-body-md hover:text-body"
          >
            <MapPin size={16} className="mt-0.5 shrink-0" aria-hidden />
            Jordan y Oquendo. Edificio de Rectorado - 3er piso.
          </a>
          <a
            href="https://maps.app.goo.gl/ztYzQc2Ks7XuEnc88"
            target="_blank"
            rel="noopener noreferrer"
            className="home-map-placeholder mt-4 block h-24 overflow-hidden rounded-xl border border-primary-600"
            aria-label="Abrir la sede central en Google Maps"
          />
        </div>
      </div>
      <p className="border-t border-primary-700 py-4 text-center text-label-md text-primary-200">
        © 2026 - AcredIA
      </p>
    </footer>
  );
}
